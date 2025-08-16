import asyncio
import re
import httpx, hashlib, json

from typing import cast, Dict, Any

from langchain_openai import ChatOpenAI
from langchain_core.output_parsers import StrOutputParser
from ..code_review_schema import DiffFile
from fastapi.encoders import jsonable_encoder
from pydantic import BaseModel

from copy import deepcopy

from ..state import CodeReviewState
from ..prompts import (
    feature_inference_prompt,
    checklist_evaluation_prompt,
    code_review_prompt,
    review_summary_prompt,
    commit_message_prompt,
)

llm = ChatOpenAI(model="gpt-4o-mini", temperature=0)
_to_str = StrOutputParser()

def _to_diff_dict(file: Any) -> Dict[str, Any]:
    # DiffFile 이면 alias 적용해서 dict로 변환
    if isinstance(file, DiffFile):
        return file.model_dump(by_alias=True)
    # 다른 BaseModel일 수도 있으니 안전망
    if isinstance(file, BaseModel):
        return file.model_dump(by_alias=True)
    # 이미 dict면 그대로
    if isinstance(file, dict):
        return file
    raise TypeError(f"Unsupported diff file type: {type(file)}")

async def ask_str(prompt, **vars) -> str:
    # ChatPromptTemplate -> str
    return await (prompt | llm | _to_str).ainvoke(vars)

# 기능명 추론
async def run_feature_inference(state: CodeReviewState) -> CodeReviewState:
    diff_files = state["diff_files"]
    available_features = state["available_features"]

    diff_text_parts = []
    for file in diff_files:
        file_path = file.get("file_path") or file.get("filePath")
        patch = file.get("patch", "")
        change_type = file.get("change_type", "MODIFIED")

        if change_type == "REMOVED":
            diff_text_parts.append(f"\n {file_path} (삭제됨):\n- 삭제된 파일\n")
        elif change_type == "ADDED":
            diff_text_parts.append(f"\n {file_path} (새 파일):\n{patch or '- (패치 정보 없음)'}\n")
        else:
            diff_text_parts.append(f"\n {file_path}:\n{patch}\n")

    diff_text = "".join(diff_text_parts)
    formatted_features = ", ".join(available_features)

    chain = feature_inference_prompt | llm
    raw_result = await ask_str(
        feature_inference_prompt,
        diff_text=diff_text,
        available_features=formatted_features
    )
    print(f"기능들 : {available_features}")
    print(f"diffFiles: {diff_text}")
    print(" 기능 추론 결과 (raw):", raw_result)
    
    feature_names = []
    if "기능 없음" not in raw_result:
        feature_names = [fn.strip() for fn in raw_result.split(",") if fn.strip()]

    state["feature_names"] = feature_names
    return state


# checklist 요청
async def run_checklist_fetch(state: CodeReviewState) -> CodeReviewState:
    project_id = state["project_id"]
    feature_name = state["feature_name"]

    print(f"\n checklist 요청: project_id={project_id}, feature_name={feature_name}")
    url = f"https://i13a601.p.ssafy.io/api/code-review/project/{project_id}/feature/checklist"

    params = {"featureName": feature_name}
    async with httpx.AsyncClient() as client:
        response = await client.get(url, params=params)

    if response.status_code != 200:
        raise RuntimeError(f"Checklist 조회 실패: {response.status_code} / {response.text}")

    data = response.json()

    # 새 포맷 (snake_case) + 구 포맷 (camelCase) 모두 허용
    if isinstance(data, dict):
        feature_id = data.get("feature_id") or data.get("feature_id")
        items = data.get("checklist_items") or data.get("checklist_items") or []
        # 항목 정규화
        norm_items = []
        for it in items:
            if not isinstance(it, dict):
                continue
            norm_items.append({
                "item": it.get("item"),
                "done": bool(it.get("done")),
            })

        state["feature_id"] = feature_id   # ★ 중요
        state["checklist"] = norm_items
        print(f"checklist 수신 완료: feature_id={feature_id}, 항목수={len(norm_items)}")

    elif isinstance(data, list):
        # 완전 구포맷: 리스트만 오는 경우
        state["checklist"] = data
        print(f"구 포맷 감지(리스트만): 항목수={len(data)}")

    else:
        print(f"알 수 없는 checklist 응답 형식: {type(data)} {data}")
        state["checklist"] = []
    print(f"[NODE:run_checklist_fetch] force_done={state.get('force_done')} ({type(state.get('force_done')).__name__})")

    return state


async def run_feature_implementation_check(state: "CodeReviewState") -> "CodeReviewState":
    feature_name   = state.get("feature_name", "")
    checklist      = state.get("checklist") or []                 # [{item: str, done: bool}, ...]
    diff_files     = state.get("diff_files") or []
    commit_message = state.get("commit_message", "")

    # 1) 요청에서 온 force_done 값을 최우선으로 반영
    force_done_req = bool(state.get("force_done", False))

    # 2) 커밋 메시지에서 force_done 신호 추출 (예: "완료", "done", "complete"...)
    try:
        msg_pred = (await ask_str(commit_message_prompt, commit_message=commit_message)).strip()
        force_done_msg = msg_pred.strip().lower() in {"완료", "done", "true", "완료됨", "complete"}
    except Exception:
        msg_pred = ""
        force_done_msg = False

    # 3) 체크리스트 미완료 항목 수집
    # 체크리스트가 있는데 미구현한 항목이 없으면 기능 구현 완료로 간주
    has_checklist = len(checklist) > 0
    undone_items  = [it.get("item") for it in checklist if not it.get("done", False)]
    all_done      = has_checklist and len(undone_items) == 0

    print(f"\n 기능 구현 평가 시작: {feature_name=}")
    print(f" 체크리스트: total={len(checklist)}, undone={len(undone_items)} (all_done={all_done})")
    print(f" commit_message force_done? req={force_done_req} msg={force_done_msg}")
    print(f" 변경 파일 수: {len(diff_files)}")

    # 4) GPT 평가(항상 실행: extra_implemented 탐지 목적)
    try:
        raw = await ask_str(
            checklist_evaluation_prompt,
            feature_name=feature_name,
            checklist_items=undone_items,   # 미완료 항목만 평가 요청
            diff_files=diff_files,
        )
        parsed = json.loads(raw)
    except Exception as e:
        print(f" checklist_evaluation 파싱 실패: {e}")
        parsed = {"implemented": False, "checklist_evaluation": {}, "extra_implemented": [], "checklist_file_map": {}}


    # 5) 최종 구현 여부 판단
    implemented_final = bool(
        force_done_req or
        force_done_msg or
        all_done
    )

    state["force_done"] = bool(force_done_req or force_done_msg)

    # 6) 상태 반영 (호환 위해 implemented/implements 모두 세팅)
    state["implemented"]            = implemented_final
    state["checklist_evaluation"]   = parsed.get("checklist_evaluation", {})
    state["extra_implemented"]      = parsed.get("extra_implemented", [])
    state["checklist_file_map"]     = parsed.get("checklist_file_map", {})

    # 7) 요약 직행 여부(go_summary): 구현 완료 && 추가 구현 없음
    state["go_summary"] = bool(implemented_final and len(state["extra_implemented"]) == 0)

    print(" implemented_final:", implemented_final)
    print(" extra_implemented:", state["extra_implemented"])
    print(" 다음 경로:", "run_code_review_summary (직행)" if state["go_summary"] else "세부 리뷰 경로")

    print("checklist_evaluation :", json.dumps(parsed.get("checklist_evaluation", {}), ensure_ascii=False, indent=2))
    print("checklist_file_map :", json.dumps(parsed.get("checklist_file_map", {}), ensure_ascii=False, indent=2))
    print("extra_implemented :", parsed.get("extra_implemented", []))
    print(f"[NODE:run_feature_implementation_check] force_done={state.get('force_done')} ({type(state.get('force_done')).__name__})")

    return state

# checklist 평가 반영
async def apply_checklist_evaluation(state: CodeReviewState) -> CodeReviewState:
    checklist_eval = state.get("checklist_evaluation", {})
    updated_checklist = []

    for item in state["checklist"]:
        item_name = item["item"]
        item["done"] = checklist_eval.get(item_name, item.get("done", False))
        updated_checklist.append(item)

    print("\nchecklist 평가 결과 반영 완료:")
    for c in updated_checklist:
        print(f" - {c['item']} → {'ㅇ' if c['done'] else 'x'}")

    state["checklist"] = updated_checklist
    after_check_undone_items  = [it.get("item") for it in updated_checklist if not it.get("done", False)]
    implemented_now = len(after_check_undone_items) == 0
    state["implemented"] = implemented_now
    state["no_code_review_file_patch"] = len(state["checklist_file_map"]) == 0
    print(f"[NODE:run_xxx] force_done={state.get('force_done')} ({type(state.get('force_done')).__name__})")

    return state


# 코드리뷰 파일 요청
async def run_code_review_file_fetch(state: CodeReviewState) -> CodeReviewState:
    project_id = state["project_id"]
    commit_hash = state["commit_hash"]
    commit_info = state.get("commit_info", {})
    checklist_file_map = state.get("checklist_file_map", {})
    commit_branch = state.get("commit_branch", "")

    repo_name = commit_info.get("repo_name")
    repo_owner = commit_info.get("repo_owner")

    file_paths = sorted({path for paths in checklist_file_map.values() for path in paths})


    print(f"\n 코드리뷰용 파일 요청 시작 (총 {len(file_paths)}개)")
    print(f"file_paths : {file_paths}")
    print(f"repo_name : " , commit_info.get("repo_name", ""))

    url = f"https://i13a601.p.ssafy.io/api/code-review/project/{project_id}/commit/{commit_hash}/files"

    # (A) ★ snake_case 로 전송
    payload = {
        "file_paths": file_paths,   # was filePaths
        "repo_name": repo_name,     # was repoName
        "repo_owner": repo_owner,   # was repoOwner
        "commit_branch" : commit_branch
    }

    timeout = httpx.Timeout(connect=10.0, read=60.0, write=10.0, pool=60.0)

    async with httpx.AsyncClient(timeout=timeout, follow_redirects=True) as client:
        try:
            resp = await client.post(url, json=payload)
            resp.raise_for_status()
            review_files = resp.json() or []
        except httpx.ReadTimeout:
            raise RuntimeError(f"Java 응답 지연(ReadTimeout): {url}")
        except httpx.ConnectError as e:
            raise RuntimeError(f"Java 서버 연결 실패: {url} / {e}")
        except httpx.HTTPStatusError as e:
            raise RuntimeError(f"Java 오류 {e.response.status_code}: {e.response.text}") from e

    print(f"review_files 1차: {len(review_files)}개")

    # (C) 파일명만 들어왔을 가능성 → 폴백: 전체 파일 받아서 파일명 기준 필터링
    only_basenames = file_paths and all("/" not in p for p in file_paths)
    if not review_files and only_basenames:
        print(" 경로 없이 파일명만 전달된 것으로 판단 → 전체 파일 요청 후 파일명으로 필터링")
        # 전체 파일 요청: file_paths 비워서 요청 (서버가 지원한다는 가정)
        payload_all = {
            "file_paths": [],
            "repo_name": repo_name,
            "repo_owner": repo_owner,
        }
        async with httpx.AsyncClient(timeout=timeout, follow_redirects=True) as client:
            resp2 = await client.post(url, json=payload_all)
            resp2.raise_for_status()
            all_files = resp2.json() or []

        # 파일명만 추출해서 필터링
        wanted = set(file_paths)
        def basename(path: str) -> str:
            return (path or "").split("/")[-1]

        filtered = [f for f in all_files if basename(f.get("file_path", "")) in wanted]
        print(f"review_files 폴백: 전체 {len(all_files)}개 중 {len(filtered)}개 매칭")
        review_files = filtered

    # --- 응답 수신 직후 ---
    print(f"review_files : {review_files}")

    # filePath -> file_path로 표준화해서 state에 저장
    normalized_files = []
    for f in review_files:
        path = (f.get("file_path") or f.get("filePath") or "")
        path = path.replace("\\", "/").lstrip("/").strip()
        normalized_files.append({
            "file_path": path,
            "content": f.get("content", "")
        })

    state["review_files"] = normalized_files
    print("정규화된 리뷰 파일 경로들:", [x["file_path"] for x in normalized_files])
    print(f"최종 review_files: {len(normalized_files)}개")

    print(f"[NODE:run_xxx] force_done={state.get('force_done')} ({type(state.get('force_done')).__name__})")

    return state


def _norm_path(p: str) -> str:
    # 슬래시, 앞 슬래시 제거, 공백 제거
    return (p or "").replace("\\", "/").lstrip("/").strip()

# 코드리뷰 수행
async def run_feature_code_review(state: CodeReviewState) -> CodeReviewState:
    file_map = state.get("checklist_file_map", {}) or {}
    review_files = state.get("review_files", []) or []
    print("state.review_files 샘플:", (review_files[:1] if review_files else "EMPTY"))

    # 1) review_files 경로 표준화
    norm_review_files = []
    for f in review_files:
        path = _norm_path(f.get("file_path") or f.get("filePath"))
        norm_review_files.append({
            "file_path": path,
            "content": f.get("content", "")
        })

    print("리뷰 파일 경로들:", [f["file_path"] for f in norm_review_files])

    code_review_items = []
    final_summary_text = ""   # 문자열로 초기화

    for item, related_files in file_map.items():
        related_norm = {_norm_path(p) for p in (related_files or [])}
        related_basenames = {p.split("/")[-1] for p in related_norm}

        all_paths = [f["file_path"] for f in norm_review_files]
        all_basenames = [p.split("/")[-1] for p in all_paths]

        # 디버그 로그로 교집합 확인
        print(f"\n[{item}]")
        print("· related_norm      :", related_norm)
        print("· all_paths         :", all_paths)
        print("· 교집합(풀경로)    :", related_norm.intersection(set(all_paths)))
        print("· 교집합(파일명)    :", related_basenames.intersection(set(all_basenames)))

        # 2) 풀경로 우선 매칭, 없으면 파일명으로 보조 매칭
        files_to_review = [
            f for f in norm_review_files
            if f["file_path"] in related_norm
               or f["file_path"].split("/")[-1] in related_basenames
        ]

        if not files_to_review:
            print(f"관련 파일 없음: {item}")
            continue

        prompt_input = {
            "feature_name": state.get("feature_name"),
            "item": item,
            "files": files_to_review,
        }

        raw = await ask_str(code_review_prompt, **prompt_input)
        try:
            parsed = json.loads(raw)
        except Exception:
            parsed = {"code_reviews": [], "summary": str(raw).strip()}

        for category_review in parsed.get("code_reviews", []) or []:
            if isinstance(category_review.get("items"), str) and category_review["items"].strip() == "해당 없음":
                category_review["items"] = []
            category_review["checklist_item"] = item
            code_review_items.append(category_review)

        if parsed.get("summary"):
            final_summary_text = parsed["summary"]

    # 자바로 넘길 필드
    state["code_review_items"] = code_review_items
    state["review_summary"] = final_summary_text

    print(f"[NODE:run_xxx] force_done={state.get('force_done')} ({type(state.get('force_done')).__name__})")
    print(f"code_review_items : {code_review_items}")
    return state


KOREAN_KEY_MAP = {
    "summary": "요약",
    "quality_score": "점수",
    "convention": "코딩 컨벤션",
    "bug_risk": "버그 가능성",
    "security_risk": "보안 위험",
    "performance": "성능 최적화",
    "complexity": "복잡도",
    "refactoring_suggestion": "리팩터링 제안",
}

KEY_PATTERN = re.compile(
    r'^\s*[-•]?\s*(summary|quality_score|convention|complexity|bug_risk|security_risk|performance|refactoring_suggestion)\s*:\s*(.+)\s*$',
    re.IGNORECASE
)

def normalize_summary_text_to_map(text: str) -> Dict[str, str]:
    lines = [ln for ln in str(text).splitlines() if ln.strip()]
    result: Dict[str, str] = {}

    for ln in lines:
        m = KEY_PATTERN.match(ln)
        if not m:
            continue
        key_en = m.group(1).lower()
        val = re.sub(r'\s+', ' ', m.group(2)).strip()  # 한 줄화

        if key_en == "quality_score":
            m2 = re.search(r'\d{1,3}', val)
            score = int(m2.group(0)) if m2 else 0
            score = max(0, min(100, score))            # 0~100 클램프
            result[KOREAN_KEY_MAP[key_en]] = str(score)
        else:
            result[KOREAN_KEY_MAP[key_en]] = val

    # 누락 키 기본값 채우기
    for en, ko in KOREAN_KEY_MAP.items():
        if ko not in result:
            result[ko] = "0" if en == "quality_score" else ""

    return result


# 코드리뷰아이템 가져오기
async def run_code_review_item_fetch(state: CodeReviewState) -> CodeReviewState:
    project_id = state["project_id"]
    feature_name = state["feature_name"]

    print(f"\n 코드리뷰아이템 요청: project_id={project_id}, feature_name={feature_name}")
    url = f"https://i13a601.p.ssafy.io/api/code-review/project/{project_id}/feature"

    params = {"featureName": feature_name}

    async with httpx.AsyncClient() as client:
        response = await client.get(url, params=params)
    if response.status_code != 200:
        raise RuntimeError(f"코드리뷰아이템 조회 실패: {response.status_code} / {response.text}")
    
    data = response.json()

     # 1) 원본 그대로 저장
    state["code_review_items_java"] = data

    # 2) 평탄화해서 저장
    # flat_items = []
    # for category_block in data:
    #     category = category_block.get("category")
    #     checklist_item = category_block.get("checklist_item")
    #     for item in category_block.get("items", []):
    #         flat_items.append({
    #             "category": category,
    #             "checklist_item": checklist_item,
    #             "file_path": item.get("file_path") or item.get("filePath"),
    #             "line_range": item.get("line_range") or item.get("lineRange"),
    #             "severity": item.get("severity"),
    #             "message": item.get("message"),ㄴ
    #         })

    # state["code_review_items_java"] = flat_items

    print(f"기존 코드리뷰아이템 {len(code_review_items_java)}개 수집 완료.")

    return state

# 코드리뷰 요약
async def run_code_review_summary(state: CodeReviewState) -> CodeReviewState:
    items = state.get("code_review_items", []) + state.get("code_review_items_java", [])

    # set은 순서가 깨지니까, dict.fromkeys로 순서 유지 중복 제거
    items = list({tuple(sorted(d.items())): d for d in items}.values())

    categorized_reviews = build_categorized_reviews(items)
    prompt_input = {
        "feature_name": state["feature_name"],
        "categorized_reviews": categorized_reviews,
    }
    raw = await ask_str(review_summary_prompt, **prompt_input)

    # LLM 출력(문자열)을 Map[String,String]으로 변환
    summary_map = normalize_summary_text_to_map(raw)

    # 자바가 기대하는 필드
    state["review_summaries"] = summary_map  # Map<String,String>
    
    try:
        state["quality_score"] = int(summary_map.get("점수", "0") or 0)
    except Exception:
        state["quality_score"] = 0

    return state


def build_categorized_reviews(code_review_items: list[dict]) -> dict:
    categorized: dict[str, list] = {}
    for item in code_review_items:
        category = item["category"]
        items = item["items"]

        if items == "해당 없음":
            categorized[category] = "해당 없음"
            continue

        simplified_items = [
            {"severity": i["severity"], "message": i["message"]}
            for i in items
            if isinstance(i, dict)
        ]
        categorized.setdefault(category, []).extend(simplified_items)

    return categorized


def _preview_payload(payload: dict) -> str:
    """자바로 보내기 전에 너무 큰 필드는 줄이고 핵심만 보이게 요약"""
    p = deepcopy(payload)

    # code_review_items: 개수 + 첫 1개만 샘플
    cri = p.get("code_review_items") or []
    p["code_review_items_count"] = len(cri)
    if cri:
        sample = cri[0].copy()
        # items 길면 2개까지만
        items = sample.get("items") or []
        sample["items_count"] = len(items)
        sample["items"] = items[:2]
        p["code_review_items_sample"] = sample
    # 원본 리스트는 로그에서 제거(너무 큼)
    p.pop("code_review_items", None)

    # review_summaries(Map)과 review_summary(String)은 그대로 두되 길이만 추가
    rs = p.get("review_summary")
    if isinstance(rs, str):
        p["review_summary_length"] = len(rs)

    # checklist_file_map 도 크면 키 개수만
    cfm = p.get("checklist_file_map") or {}
    p["checklist_file_map_keys"] = list(cfm.keys())[:5]
    p["checklist_file_map_count"] = len(cfm)

    return json.dumps(p, ensure_ascii=False, indent=2)


def to_bool(v) -> bool:
    if isinstance(v, bool): return v
    if isinstance(v, (int, float)): return v != 0
    if isinstance(v, str): return v.strip().lower() in ("true","1","yes","y","t")
    return False


# Java로 결과 전송
async def send_result_to_java(state: CodeReviewState) -> CodeReviewState:
    url = "https://i13a601.p.ssafy.io/api/code-review/result"

    # feature_names를 항상 리스트로 맞춰주기
    feature_name = state.get("feature_name")

    review_summaries = state.get("review_summaries") or {}
    raw_force_done = state.get("force_done")
    force_done = to_bool(raw_force_done)

    print(f"[force_done] raw={repr(raw_force_done)} -> normalized={force_done}")

    if not isinstance(review_summaries, dict):
        # 혹시 문자열/리스트가 들어오면 기존 파서로 강제 변환
        review_summaries = normalize_summary_text_to_map(str(review_summaries))


    payload = {
        "project_id": state.get("project_id"),
        "commit_id" : state.get("commit_id"),
        "commit_hash": state.get("commit_hash"),
        "feature_name": feature_name,
        "feature_id": state.get("feature_id"),
        "checklist_evaluation": state.get("checklist_evaluation") or {},
        "checklist_file_map": state.get("checklist_file_map") or {},
        "extra_implemented": state.get("extra_implemented") or [],
        "code_review_items": state.get("code_review_items") or [],
        "force_done": force_done,
        "review_summary": state.get("review_summary") or "",
        "review_summaries": review_summaries,
    }

    print("\n[send_result_to_java] ▶ payload preview")
    print(_preview_payload(payload))
    
    async with httpx.AsyncClient() as client:
        response = await client.post(url, json=payload)

    print(f"[send_result_to_java] ◀ status={response.status_code}")

    if response.status_code != 200:
        raise RuntimeError(f"Java 응답 실패: {response.status_code} / {response.text}")

    return state


# 병렬 기능 처리
def make_idem_key(s):
    base = f'{s.get("project_id")}:{s.get("commit_id")}:{s.get("feature_id")}'
    return hashlib.sha256(base.encode()).hexdigest()

async def run_parallel_feature_graphs(state: CodeReviewState) -> CodeReviewState:
    from ..subgraph import create_feature_graph
    print(f"병렬 그래프 실행")
    feature_names = state.get("feature_names") or []
    JAVA_API_URL = "https://i13a601.p.ssafy.io/api/code-review/result"

    async def run_one(name: str):
        s = deepcopy(state)
        s["feature_name"] = name
        graph = create_feature_graph()
        return await graph.ainvoke(s)  # ← feature별 결과(res)

    results = await asyncio.gather(*(run_one(n) for n in feature_names)) if feature_names else []

    limits = httpx.Limits(max_connections=5, max_keepalive_connections=5)
    async with httpx.AsyncClient(timeout=10, limits=limits) as client:
        for res in results:  # 순차 전송
            feature_name = res.get("feature_name")
            feature_id = res.get("feature_id")
            if not feature_name or not feature_id:
                continue  # 가드

            force_done = bool(res.get("force_done"))
            review_summaries = res.get("review_summaries") or {}

            payload = {
                "project_id": res.get("project_id"),
                "commit_id": res.get("commit_id"),
                "commit_hash": res.get("commit_hash"),
                "feature_name": feature_name,
                "feature_id": feature_id,
                "checklist_evaluation": res.get("checklist_evaluation") or {},
                "checklist_file_map": res.get("checklist_file_map") or {},
                "extra_implemented": res.get("extra_implemented") or [],
                "code_review_items": res.get("code_review_items") or [],
                "force_done": force_done,
                "review_summary": res.get("review_summary") or "",
                "review_summaries": review_summaries,
            }

            # 동결(중간에 상태가 변해도 안전)
            payload = jsonable_encoder(payload)

            # (선택) 멱등 키
            headers = {"Idempotency-Key": make_idem_key(res)}

            print("[send_result_to_java] ▶ payload preview")
            print(json.dumps(payload, ensure_ascii=False, indent=2))

            resp = await client.post(JAVA_API_URL, json=payload, headers=headers)
            print(f"[send_result_to_java] ◀ status={resp.status_code}")

    return state
