import asyncio
import re
import httpx, hashlib, json

from typing import cast, Dict, Any

from langchain_openai import ChatOpenAI
from langchain_core.output_parsers import StrOutputParser
from ..code_review_schema import DiffFile
from fastapi.encoders import jsonable_encoder


from copy import deepcopy

from ..state import CodeReviewState
from ..prompts import (
    feature_inference_prompt,
    checklist_evaluation_prompt,
    code_review_prompt,
    review_summary_prompt,
    commit_message_prompt,
)

llm = ChatOpenAI(model="gpt-3.5-turbo", temperature=0)
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
            diff_text_parts.append(f"\n📄 {file_path} (삭제됨):\n- 삭제된 파일\n")
        elif change_type == "ADDED":
            diff_text_parts.append(f"\n📄 {file_path} (새 파일):\n{patch or '- (패치 정보 없음)'}\n")
        else:
            diff_text_parts.append(f"\n📄 {file_path}:\n{patch}\n")

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

    print(f"\n📡 checklist 요청: project_id={project_id}, feature_name={feature_name}")
    url = f"http://localhost:8081/api/code-review/project/{project_id}/feature/checklist"

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
        print(f"⚠️ 구 포맷 감지(리스트만): 항목수={len(data)}")

    else:
        print(f"⚠️ 알 수 없는 checklist 응답 형식: {type(data)} {data}")
        state["checklist"] = []
    print(f"[NODE:run_checklist_fetch] force_done={state.get('force_done')} ({type(state.get('force_done')).__name__})")

    return state


# 체크리스트 기반 구현 여부 확인
async def run_feature_implementation_check(state: CodeReviewState) -> CodeReviewState:
    feature_name = state["feature_name"]
    checklist = state["checklist"]
    diff_files = state["diff_files"]
    commit_message = state.get("commit_message", "")

    checklist_items = [item["item"] for item in checklist if not item.get("done", False)]

    result_text = (await ask_str(commit_message_prompt, commit_message=commit_message)).strip()

    # 결과 문자열 정규화
    norm = result_text.strip().lower()
    force_done = norm in {"완료", "done", "true", "완료됨", "complete"}

    # 상태에 반영
    state["force_done"] = force_done

    print(f"📝 커밋 메시지 분석 결과 → force_done: {force_done} (raw={result_text!r})")

    print(f"\n🔍 기능 구현 평가 시작: {feature_name=}")
    print(f"📋 체크리스트 항목 수: {len(checklist_items)}개")
    print(f"📦 파일 수: {len(diff_files)}개")

    prompt_input = {
        "feature_name": feature_name,
        "checklist_items": checklist_items,
        "diff_files": diff_files,
    }

    raw = await ask_str(
        checklist_evaluation_prompt,
        feature_name=feature_name,
        checklist_items=checklist_items,
        diff_files=diff_files,
    )
    parsed = json.loads(raw)

    state["implemented"] = parsed["implemented"]
    state["checklist_evaluation"] = parsed["checklist_evaluation"]
    state["extra_implemented"] = parsed.get("extra_implemented", [])
    state["checklist_file_map"] = parsed.get("checklist_file_map", {})

    print("implemented :", parsed.get("implemented"))
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


    print(f"\n📡 코드리뷰용 파일 요청 시작 (총 {len(file_paths)}개)")
    print(f"file_paths : {file_paths}")
    print(f"repo_name : " , commit_info.get("repo_name", ""))

    url = f"http://localhost:8081/api/code-review/project/{project_id}/commit/{commit_hash}/files"

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


    # payload = {
    #     "filePaths": file_paths,
    #     "repoName": commit_info.get("repo_name"),
    #     "repoOwner": commit_info.get("repo_owner"),
    # }

    # timeout = httpx.Timeout(connect=10.0, read=60.0, write=10.0, pool=60.0)

    # async with httpx.AsyncClient(timeout=timeout, follow_redirects=True) as client:
    #     try:
    #         resp = await client.post(url, json=payload)
    #         resp.raise_for_status()
    #     except httpx.ReadTimeout:
    #         raise RuntimeError(f"Java 응답 지연(ReadTimeout): {url}")
    #     except httpx.ConnectError as e:
    #         raise RuntimeError(f"Java 서버 연결 실패: {url} / {e}")
    #     except httpx.HTTPStatusError as e:
    #         # 에러 본문을 같이 출력해서 서버쪽 문제 파악
    #         raise RuntimeError(f"Java 오류 {resp.status_code}: {resp.text}") from e
    # review_files = resp.json()
    # print(f"review_files : {review_files}")
    # state["review_files"] = resp.json()
    # return state

def _norm_path(p: str) -> str:
    # 슬래시統一, 앞 슬래시 제거, 공백 제거
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
    r'^\s*[-•]?\s*(summary|quality_score|convention|bug_risk|security_risk|performance|refactoring_suggestion)\s*:\s*(.+)\s*$',
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
async def run_checklist_fetch(state: CodeReviewState) -> CodeReviewState:
    project_id = state["project_id"]
    feature_id = state["feature_id"]
    feature_name = state["feature_name"]

    print(f"\n 코드리뷰아이템 요청: project_id={project_id}, feature_name={feature_name}")
    url = f"http://localhost:8081/api/code-review-item/project/{project_id}/feature/{feature_id}"

    params = {"featureName": feature_name}

    async with httpx.AsyncClient() as client:
        response = await client.get(url, params=params)

    if response.status_code != 200:
        raise RuntimeError(f"코드리뷰아이템 조회 실패: {response.status_code} / {response.text}")

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
        print(f"⚠️ 구 포맷 감지(리스트만): 항목수={len(data)}")

    else:
        print(f"⚠️ 알 수 없는 checklist 응답 형식: {type(data)} {data}")
        state["checklist"] = []
    print(f"[NODE:run_checklist_fetch] force_done={state.get('force_done')} ({type(state.get('force_done')).__name__})")

    return state

# 코드리뷰 요약
async def run_code_review_summary(state: CodeReviewState) -> CodeReviewState:
    categorized_reviews = build_categorized_reviews(state.get("code_review_items", []))
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
    url = "http://localhost:8081/api/code-review/result"

    # feature_names를 항상 리스트로 맞춰주기
    feature_name = state.get("feature_name")
    # feature_names = state.get("feature_names")
    # if feature_names is None:
    #     if feature_name is None:
    #         feature_names = None
    #     elif isinstance(feature_name, list):
    #         feature_names = feature_name
    #     else:
    #         feature_names = [feature_name]

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
    feature_names = state.get("feature_names") or []
    JAVA_API_URL = "http://localhost:8081/api/code-review/result"

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

# async def run_parallel_feature_graphs(state: CodeReviewState) -> CodeReviewState:
#     from ..subgraph import create_feature_graph
#     feature_names = state.get("feature_names") or []

#     async def run_feature(feature_name: str):
#         feature_state = deepcopy(state)
#         feature_state["feature_name"] = feature_name
#         graph = create_feature_graph()
#         result = await graph.ainvoke(feature_state)
#         await send_result_to_java(result)

#     if len(feature_names) == 1:
#         await run_feature(feature_names[0])
#     else:
#         await asyncio.gather(*(run_feature(name) for name in feature_names))

#     return state


# import httpx
# import json
# import asyncio

# from langchain_openai import ChatOpenAI
# from langchain_core.output_parsers import StrOutputParser

# from copy import deepcopy

# from ..state import CodeReviewState
# from ..prompts import feature_inference_prompt, checklist_evaluation_prompt, code_review_prompt, review_summary_prompt, commit_message_prompt

# llm = ChatOpenAI(model="gpt-3.5-turbo", temperature=0)
# parser = StrOutputParser()

# # 기능명 추론
# async def run_feature_inference(state: CodeReviewState) -> CodeReviewState:
#     diff_files = state["diff_files"]
#     available_features = state["available_features"]

#     # 📌 diff 합치기
#     diff_text = ""
#     for file in diff_files:
#         file_path = file.get("file_path")
#         patch = file.get("patch", "")
#         change_type = file.get("change_type", "MODIFIED")

#         if change_type == "REMOVED":
#             diff_text += f"\n📄 {file_path} (삭제됨):\n- 삭제된 파일\n"
#         elif change_type == "ADDED":
#             diff_text += f"\n📄 {file_path} (새 파일):\n{patch or '- (패치 정보 없음)'}\n"
#         else:
#             diff_text += f"\n📄 {file_path}:\n{patch}\n"

#     formatted_features = ", ".join(available_features)


#     chain = feature_inference_prompt | llm

#     # 🔍 GPT 호출
#     response = await chain.ainvoke({
#         "diff_text": diff_text,
#         "available_features": formatted_features
#     })
    
#     raw_result = response.content.strip()
#     print(f"기능들 : {available_features}")
#     print(f"diffFiles: {diff_text}")
#     print("🧠 기능 추론 결과 (raw):", raw_result)
    
#     # 📤 결과 파싱
#     if "기능 없음" in raw_result:
#         feature_names = []
#     else:
#         feature_names = [fn.strip() for fn in raw_result.split(",") if fn.strip()]

#     state["feature_names"] = feature_names
#     return state



# # Java에 기능명 기반 checklist 요청
# async def run_checklist_fetch(state: CodeReviewState) -> CodeReviewState:
#     project_id = state["project_id"]
#     feature_name = state["feature_name"]

#     print(f"\n📡 checklist 요청: {project_id=} / {feature_name=}")

#     url = f"http://localhost:8080/api/java/project/{project_id}/feature/{feature_name}/checklist"

#     async with httpx.AsyncClient() as client:
#         response = await client.get(url)

#     if response.status_code != 200:
#         raise Exception(f"Checklist 조회 실패: {response.status_code} / {response.text}")

#     checklist_items = response.json()

#     print(f"checklist 수신 완료 ({len(checklist_items)}개): {checklist_items}")

#     state["checklist"] = checklist_items
#     return state


# async def run_commit_message_completion_check(state: CodeReviewState) -> CodeReviewState:
#     message = state.get("commit_message")
#     if not message:
#         return state  # 메시지 없으면 판단 불가

#     print(f"\n GPT에게 커밋 메시지 판단 요청: {message}")

#     prompt_input = {"commit_message": message}
#     result = await commit_message_prompt.ainvoke(prompt_input)
#     result_text = result.content.strip()

#     if result_text == "완료":
#         print("GPT 판단: 구현 완료된 커밋")
#         state["force_done_by_commit_message"] = True
#     else:
#         print("GPT 판단: 아직 구현 미완료")

#     return state


# # 체크리스트 기반 구현 여부 확인
# async def run_feature_implementation_check(state: CodeReviewState) -> CodeReviewState:
#     feature_name = state["feature_name"]
#     checklist = state["checklist"]
#     full_files = state["full_files"]
#     commit_message = state.get("commit_message", "")

#     checklist_items = [item["item"] for item in checklist if not item.get("done", False)]

#     # GPT에게 커밋 메시지 분석 요청
#     message_result = await commit_message_prompt.ainvoke({"commit_message": commit_message})
#     force_done = "완료" in message_result.content.strip()
#     state["force_done"] = force_done
#     print(f"📝 커밋 메시지 분석 결과 → force_done: {force_done}")

#     print(f"\n🔍 기능 구현 평가 시작: {feature_name=}")
#     print(f"📋 체크리스트 항목 수: {len(checklist_items)}개")
#     print(f"📦 파일 수: {len(full_files)}개")

#     prompt_input = {
#         "feature_name": feature_name,
#         "checklist_items": checklist_items,
#         "full_files": full_files,
#     }

#     result = await checklist_evaluation_prompt.invoke(prompt_input)
#     print(f"\n✅ GPT 평가 결과 수신 완료")

#     parsed = json.loads(result.content)

#     implements = parsed["implements"]
#     checklist_evaluation = parsed["checklist_evaluation"]
#     extra_implemented = parsed.get("extra_implemented", [])
#     checklist_file_map = parsed.get("checklist_file_map", {})

#     print(f"📌 implements: {implements}")
#     print(f"📌 checklist_evaluation: {checklist_evaluation}")
#     print(f"📌 extra_implemented: {extra_implemented}")
#     print(f"📌 checklist_file_map: {checklist_file_map}")

#     state["implements"] = implements
#     state["checklist_evaluation"] = checklist_evaluation
#     state["extra_implemented"] = extra_implemented
#     state["checklist_file_map"] = checklist_file_map

#     return state

# # 체크리스트 항목 구현 여부 state["checklist"] 에 반영
# async def apply_checklist_evaluation(state: CodeReviewState) -> CodeReviewState:
#     checklist = state["checklist"]
#     checklist_eval = state.get("checklist_evaluation", {})

#     updated_checklist = []
#     for item in checklist:
#         item_name = item["item"]
#         item["done"] = checklist_eval.get(item_name, item.get("done", False))  # 기본값은 원래 값 유지
#         updated_checklist.append(item)

#     print(f"\n✅ checklist 평가 결과 반영 완료:")
#     for c in updated_checklist:
#         print(f" - {c['item']} → {'✅' if c['done'] else '❌'}")

#     state["checklist"] = updated_checklist
#     return state


# # checklist_file_map 에 있는 파일 java 에서 가져오기 (java 에서 github api 연동해서 가져옴)
# async def run_code_review_file_fetch(state: CodeReviewState) -> CodeReviewState:
#     project_id = state["project_id"]
#     commit_hash = state["commit_hash"]
#     checklist_file_map = state.get("checklist_file_map", {})
#     commit_info = state.get("commit_info", {})

#     # 파일 경로 목록만 뽑아서 중복 제거
#     file_paths = sorted(set(path for paths in checklist_file_map.values() for path in paths))

#     print(f"\n📡 코드리뷰용 파일 요청 시작")
#     print(f"📦 요청 대상 파일 수: {len(file_paths)}개")

#     url = f"http://localhost:8080/api/java/project/{project_id}/commit/{commit_hash}/files"

#     payload = {
#         "file_paths": file_paths,
#         "repoName": commit_info.get("repo_name"),
#         "repoOwner": commit_info.get("repo_owner")
#     }

#     headers = {}
#     if state.jwt_token:
#         headers["Authorization"] = f"Bearer {state.jwt_token}"

#     async with httpx.AsyncClient() as client:
#         response = await client.post(url, json==payload, headers=headers)

#     if response.status_code != 200:
#         raise Exception(f"CodeReview 파일 조회 실패: {response.status_code} / {response.text}")

#     full_files = response.json()
#     print(f"📥 코드리뷰용 파일 수신 완료: {len(full_files)}개")

#     state["review_files"] = full_files
#     return state


# # 체크리스트 true 인 항목들 코드 리뷰
# async def run_feature_code_review(state: CodeReviewState) -> CodeReviewState:
#     file_map = state.get("checklist_file_map", {})
#     review_files = state.get("review_files", [])

#     all_review_items = list(file_map.keys())


#     print(f"\n🧪 코드리뷰 시작 (총 {len(all_review_items)}개 항목)")

#     code_review_items = []
#     summaries = []

#     for item in all_review_items:
#         related_files = file_map.get(item, [])
#         files_to_review = [f for f in review_files if f["file_path"] in related_files]

#         if not files_to_review:
#             print(f"⚠️ 관련 파일 없음: {item}")
#             continue

#         print(f"\n📌 리뷰 항목: {item}")
#         print(f"📂 파일 수: {len(files_to_review)}개")

#         prompt_input = {
#             "feature_name": state["feature_name"],
#             "item": item,
#             "files": files_to_review 
#         }

#         result = await code_review_prompt.invoke(prompt_input)
#         parsed = json.loads(result.content)

#         # ✅ 각 리뷰 항목에 checklist item 정보 추가
#         for category_review in parsed.get("code_reviews", []):

#             # 📌 "해당 없음" → 빈 리스트로 변환
#             if category_review.get("items") == "해당 없음":
#                 category_review["items"] = []
            
#             category_review["checklist_item"] = item
#             code_review_items.append(category_review)

#         summary = parsed.get("summary")
#         if summary:
#             summaries.append(f"✅ {item}: {summary}")

#     state["code_review_items"] = code_review_items
#     state["review_summaries"] = summaries

#     print(f"\n🎉 코드리뷰 완료: 총 {len(code_review_items)}개 항목")

#     return state


# # 기능 코드리뷰 요약
# async def run_code_review_summary(state: CodeReviewState) -> CodeReviewState:
#     feature_name = state.get("feature_name")
#     code_review_items = state.get("code_review_items", [])

#     # ✅ severity + message 만 뽑아서 정리
#     categorized_reviews = build_categorized_reviews(code_review_items)

#     print(f"\n🧠 코드리뷰 요약 생성 시작: {feature_name=}")
#     print(f"📂 포함된 카테고리: {list(categorized_reviews.keys())}")

#     prompt_input = {
#         "feature_name": feature_name,
#         "categorized_reviews": categorized_reviews
#     }

#     result = await review_summary_prompt.invoke(prompt_input)
#     summary = result.content.strip()

#     print(f"\n📋 코드리뷰 요약 결과:\n{summary}")

#     state["review_summary"] = summary
#     return state


# # 코드리뷰 항목별 카테고라이징
# def build_categorized_reviews(code_review_items: list[dict]) -> dict:
#     categorized = {}

#     for item in code_review_items:
#         category = item["category"]
#         items = item["items"]

#         if items == "해당 없음":
#             categorized[category] = "해당 없음"
#             continue

#         simplified_items = [
#             {"severity": i["severity"], "message": i["message"]}
#             for i in items
#             if isinstance(i, dict) and "severity" in i and "message" in i
#         ]

#         if category not in categorized:
#             categorized[category] = simplified_items
#         else:
#             categorized[category].extend(simplified_items)

#     return categorized


# # java 로 결과 전달
# async def send_result_to_java(state: CodeReviewState) -> CodeReviewState:
#     url = "http://localhost:8080/api/java/code-review/result"

#     payload = {
#         "project_id": state["project_id"],
#         "commit_hash": state["commit_hash"],
#         "feature_name": state.get("feature_name"),
#         "feature_id": state.get("feature_id"),
#         "checklist_evaluation": state.get("checklist_evaluation", {}),
#         "checklist_file_map": state.get("checklist_file_map", {}),
#         "extra_implemented": state.get("extra_implemented", []),
#         "code_review_items": state.get("code_review_items", []),
#         "force_done": state.get("force_done", "")
#     }

#     if "review_summary" in state:
#         payload["review_summary"] = state["review_summary"]
#         payload["review_summaries"] = state.get("review_summaries", [])

#     print("\n🚀 Java로 코드리뷰 결과 전송 시작")
#     async with httpx.AsyncClient() as client:
#         response = await client.post(url, json=payload)

#     if response.status_code != 200:
#         raise Exception(f"Java 응답 실패: {response.status_code} / {response.text}")

#     print(f"✅ Java 전송 완료: {response.status_code}")
#     return state


# async def run_parallel_feature_graphs(state: CodeReviewState) -> CodeReviewState:
#     from ..subgraph import create_feature_graph

#     feature_names = state.get("feature_names") or []

#     if len(feature_names) == 1:
#         # 기능 하나일 때는 그냥 서브그래프 실행 + 자바 전송
#         feature_state = deepcopy(state)
#         feature_state["feature_name"] = feature_names[0]

#         graph = create_feature_graph()
#         result = await graph.ainvoke(feature_state)

#         await send_result_to_java(result)
#         return state

#     # 기능 여러 개일 때 병렬 실행
#     async def run_feature(feature_name: str):
#         from copy import deepcopy
#         feature_state = deepcopy(state)
#         feature_state["feature_name"] = feature_name

#         graph = create_feature_graph()
#         result = await graph.ainvoke(feature_state)

#         await send_result_to_java(result)

#     await asyncio.gather(*[run_feature(name) for name in feature_names])
#     return state
