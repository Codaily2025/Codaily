import httpx
import json
import asyncio

from copy import deepcopy

from ..state import CodeReviewState
from ..prompts import feature_inference_prompt, checklist_evaluation_prompt, code_review_prompt, review_summary_prompt, commit_message_completion_prompt, commit_message_prompt
from ..subgraph import create_feature_graph

# 기능명 추론
async def run_feature_inference(state: CodeReviewState) -> CodeReviewState:
    diff_files = state["diff_files"]
    available_features = state["available_features"]

    # 📌 diff 합치기
    diff_text = ""
    for file in diff_files:
        file_path = file.get("file_path")
        patch = file.get("patch", "")
        change_type = file.get("change_type", "MODIFIED")

        if change_type == "REMOVED":
            diff_text += f"\n📄 {file_path} (삭제됨):\n- 삭제된 파일\n"
        elif change_type == "ADDED":
            diff_text += f"\n📄 {file_path} (새 파일):\n{patch or '- (패치 정보 없음)'}\n"
        else:
            diff_text += f"\n📄 {file_path}:\n{patch}\n"

    formatted_features = ", ".join(available_features)

    # 🔍 GPT 호출
    response = await feature_inference_prompt.invoke({
        "diff_text": diff_text,
        "available_features": formatted_features
    })

    raw_result = response.content.strip()
    print("🧠 기능 추론 결과 (raw):", raw_result)

    # 📤 결과 파싱
    if "기능 없음" in raw_result:
        feature_names = []
    else:
        feature_names = [fn.strip() for fn in raw_result.split(",") if fn.strip()]

    state["feature_names"] = feature_names
    return state



# Java에 기능명 기반 checklist 요청
async def run_checklist_fetch(state: CodeReviewState) -> CodeReviewState:
    project_id = state["project_id"]
    feature_name = state["feature_name"]

    print(f"\n📡 checklist 요청: {project_id=} / {feature_name=}")

    url = f"http://localhost:8080/api/java/project/{project_id}/feature/{feature_name}/checklist"

    async with httpx.AsyncClient() as client:
        response = await client.get(url)

    if response.status_code != 200:
        raise Exception(f"Checklist 조회 실패: {response.status_code} / {response.text}")

    checklist_items = response.json()

    print(f"📥 checklist 수신 완료 ({len(checklist_items)}개): {checklist_items}")

    state["checklist"] = checklist_items
    return state


async def run_commit_message_completion_check(state: CodeReviewState) -> CodeReviewState:
    message = state.get("commit_message")
    if not message:
        return state  # 메시지 없으면 판단 불가

    print(f"\n🧠 GPT에게 커밋 메시지 판단 요청: {message}")

    prompt_input = {"commit_message": message}
    result = await commit_message_completion_prompt.ainvoke(prompt_input)
    result_text = result.content.strip()

    if result_text == "완료":
        print("✅ GPT 판단: 구현 완료된 커밋")
        state["force_done_by_commit_message"] = True
    else:
        print("❌ GPT 판단: 아직 구현 미완료")

    return state


# 체크리스트 기반 구현 여부 확인
async def run_feature_implementation_check(state: CodeReviewState) -> CodeReviewState:
    feature_name = state["feature_name"]
    checklist = state["checklist"]
    full_files = state["full_files"]
    commit_message = state.get("commit_message", "")

    checklist_items = [item["item"] for item in checklist if not item.get("done", False)]

    # GPT에게 커밋 메시지 분석 요청
    message_result = await commit_message_prompt.ainvoke({"commit_message": commit_message})
    force_done = "완료" in message_result.content.strip()
    state["force_done"] = force_done
    print(f"📝 커밋 메시지 분석 결과 → force_done: {force_done}")

    print(f"\n🔍 기능 구현 평가 시작: {feature_name=}")
    print(f"📋 체크리스트 항목 수: {len(checklist_items)}개")
    print(f"📦 파일 수: {len(full_files)}개")

    prompt_input = {
        "feature_name": feature_name,
        "checklist_items": checklist_items,
        "full_files": full_files,
    }

    result = await checklist_evaluation_prompt.invoke(prompt_input)
    print(f"\n✅ GPT 평가 결과 수신 완료")

    parsed = json.loads(result.content)

    implements = parsed["implements"]
    checklist_evaluation = parsed["checklist_evaluation"]
    extra_implemented = parsed.get("extra_implemented", [])
    checklist_file_map = parsed.get("checklist_file_map", {})

    print(f"📌 implements: {implements}")
    print(f"📌 checklist_evaluation: {checklist_evaluation}")
    print(f"📌 extra_implemented: {extra_implemented}")
    print(f"📌 checklist_file_map: {checklist_file_map}")

    state["implements"] = implements
    state["checklist_evaluation"] = checklist_evaluation
    state["extra_implemented"] = extra_implemented
    state["checklist_file_map"] = checklist_file_map

    return state

# 체크리스트 항목 구현 여부 state["checklist"] 에 반영
async def apply_checklist_evaluation(state: CodeReviewState) -> CodeReviewState:
    checklist = state["checklist"]
    checklist_eval = state.get("checklist_evaluation", {})

    updated_checklist = []
    for item in checklist:
        item_name = item["item"]
        item["done"] = checklist_eval.get(item_name, item.get("done", False))  # 기본값은 원래 값 유지
        updated_checklist.append(item)

    print(f"\n✅ checklist 평가 결과 반영 완료:")
    for c in updated_checklist:
        print(f" - {c['item']} → {'✅' if c['done'] else '❌'}")

    state["checklist"] = updated_checklist
    return state


# checklist_file_map 에 있는 파일 java 에서 가져오기 (java 에서 github api 연동해서 가져옴)
async def run_code_review_file_fetch(state: CodeReviewState) -> CodeReviewState:
    project_id = state["project_id"]
    commit_hash = state["commit_hash"]
    checklist_file_map = state.get("checklist_file_map", {})
    commit_info = state.get("commit_info", {})

    # 파일 경로 목록만 뽑아서 중복 제거
    file_paths = sorted(set(path for paths in checklist_file_map.values() for path in paths))

    print(f"\n📡 코드리뷰용 파일 요청 시작")
    print(f"📦 요청 대상 파일 수: {len(file_paths)}개")

    url = f"http://localhost:8080/api/java/project/{project_id}/commit/{commit_hash}/files"

    payload = {
        "file_paths": file_paths,
        "repoName": commit_info.get("repo_name"),
        "repoOwner": commit_info.get("repo_owner")
    }

    headers = {}
    if state.jwt_token:
        headers["Authorization"] = f"Bearer {state.jwt_token}"

    async with httpx.AsyncClient() as client:
        response = await client.post(url, json==payload, headers=headers)

    if response.status_code != 200:
        raise Exception(f"CodeReview 파일 조회 실패: {response.status_code} / {response.text}")

    full_files = response.json()
    print(f"📥 코드리뷰용 파일 수신 완료: {len(full_files)}개")

    state["review_files"] = full_files
    return state


# 체크리스트 true 인 항목들 코드 리뷰
async def run_feature_code_review(state: CodeReviewState) -> CodeReviewState:
    file_map = state.get("checklist_file_map", {})
    review_files = state.get("review_files", [])

    all_review_items = list(file_map.keys())


    print(f"\n🧪 코드리뷰 시작 (총 {len(all_review_items)}개 항목)")

    code_review_items = []
    summaries = []

    for item in all_review_items:
        related_files = file_map.get(item, [])
        files_to_review = [f for f in review_files if f["file_path"] in related_files]

        if not files_to_review:
            print(f"⚠️ 관련 파일 없음: {item}")
            continue

        print(f"\n📌 리뷰 항목: {item}")
        print(f"📂 파일 수: {len(files_to_review)}개")

        prompt_input = {
            "feature_name": state["feature_name"],
            "item": item,
            "files": files_to_review 
        }

        result = await code_review_prompt.invoke(prompt_input)
        parsed = json.loads(result.content)

        # ✅ 각 리뷰 항목에 checklist item 정보 추가
        for category_review in parsed.get("code_reviews", []):

            # 📌 "해당 없음" → 빈 리스트로 변환
            if category_review.get("items") == "해당 없음":
                category_review["items"] = []
            
            category_review["checklist_item"] = item
            code_review_items.append(category_review)

        summary = parsed.get("summary")
        if summary:
            summaries.append(f"✅ {item}: {summary}")

    state["code_review_items"] = code_review_items
    state["review_summaries"] = summaries

    print(f"\n🎉 코드리뷰 완료: 총 {len(code_review_items)}개 항목")

    return state


# 기능 코드리뷰 요약
async def run_code_review_summary(state: CodeReviewState) -> CodeReviewState:
    feature_name = state.get("feature_name")
    code_review_items = state.get("code_review_items", [])

    # ✅ severity + message 만 뽑아서 정리
    categorized_reviews = build_categorized_reviews(code_review_items)

    print(f"\n🧠 코드리뷰 요약 생성 시작: {feature_name=}")
    print(f"📂 포함된 카테고리: {list(categorized_reviews.keys())}")

    prompt_input = {
        "feature_name": feature_name,
        "categorized_reviews": categorized_reviews
    }

    result = await review_summary_prompt.invoke(prompt_input)
    summary = result.content.strip()

    print(f"\n📋 코드리뷰 요약 결과:\n{summary}")

    state["review_summary"] = summary
    return state


# 코드리뷰 항목별 카테고라이징
def build_categorized_reviews(code_review_items: list[dict]) -> dict:
    categorized = {}

    for item in code_review_items:
        category = item["category"]
        items = item["items"]

        if items == "해당 없음":
            categorized[category] = "해당 없음"
            continue

        simplified_items = [
            {"severity": i["severity"], "message": i["message"]}
            for i in items
            if isinstance(i, dict) and "severity" in i and "message" in i
        ]

        if category not in categorized:
            categorized[category] = simplified_items
        else:
            categorized[category].extend(simplified_items)

    return categorized


# java 로 결과 전달
async def send_result_to_java(state: CodeReviewState) -> CodeReviewState:
    url = "http://localhost:8080/api/java/code-review/result"

    payload = {
        "project_id": state["project_id"],
        "commit_hash": state["commit_hash"],
        "feature_name": state.get("feature_name"),
        "feature_id": state.get("feature_id"),
        "checklist_evaluation": state.get("checklist_evaluation", {}),
        "checklist_file_map": state.get("checklist_file_map", {}),
        "extra_implemented": state.get("extra_implemented", []),
        "code_review_items": state.get("code_review_items", []),
        "force_done": state.get("force_done", "")
    }

    if "review_summary" in state:
        payload["review_summary"] = state["review_summary"]
        payload["review_summaries"] = state.get("review_summaries", [])

    print("\n🚀 Java로 코드리뷰 결과 전송 시작")
    async with httpx.AsyncClient() as client:
        response = await client.post(url, json=payload)

    if response.status_code != 200:
        raise Exception(f"Java 응답 실패: {response.status_code} / {response.text}")

    print(f"✅ Java 전송 완료: {response.status_code}")
    return state


async def run_parallel_feature_graphs(state: CodeReviewState) -> CodeReviewState:
    feature_names = state.feature_names or []

    if len(feature_names) == 1:
        # 기능 하나일 때는 그냥 서브그래프 실행 + 자바 전송
        feature_state = deepcopy(state)
        feature_state.feature_name = feature_names[0]

        graph = create_feature_graph()
        result = await graph.ainvoke(feature_state)

        await send_result_to_java(result)
        return state

    # 기능 여러 개일 때 병렬 실행
    async def run_feature(feature_name: str):
        from copy import deepcopy
        feature_state = deepcopy(state)
        feature_state.feature_name = feature_name

        graph = create_feature_graph()
        result = await graph.ainvoke(feature_state)

        await send_result_to_java(result)

    await asyncio.gather(*[run_feature(name) for name in feature_names])
    return state
