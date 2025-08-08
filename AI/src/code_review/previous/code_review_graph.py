from .schema import FeatureInferenceRequest, ChecklistEvaluationRequest, CodeReviewState, FeatureReviewSummaryRequest, FeatureChecklistRequest, FeatureChecklistResponse, CodeReviewItemRequest
from .code_review_prompts import feature_inference_prompt, checklist_evaluation_prompt, code_review_prompt, review_summary_prompt
from .feature_checklist_prompt import feature_checklist_prompt
from typing import Dict
from langchain_community.chat_models import ChatOpenAI
from langgraph.graph import StateGraph, END

import os
import httpx
import json
import asyncpg
from dotenv import load_dotenv

load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
POSTGRES_URI = os.getenv("POSTGRES_URI")

JAVA_API_URL = "http://localhost:8080/api"  # 포트 번호와 context path는 너 환경에 맞게 수정


llm = ChatOpenAI(
    model="gpt-3.5-turbo",
    temperature=0,
    openai_api_key=os.getenv("OPENAI_API_KEY")
)


async def run_feature_inference(state: CodeReviewState) -> CodeReviewState:
    diff_files = state["diff_files"]
    available_features = state["available_features"]

    # diff 정리
    diff_text = ""
    for file in diff_files:
        diff_text += f"\n📄 {file['file_path']}:\n{file['patch']}\n"

    formatted_features = ", ".join(available_features)

    # GPT 호출
    response = await feature_inference_prompt.invoke({
        "diff_text": diff_text,
        "available_features": formatted_features
    })

    raw_result = response.content.strip()
    print("🧠 기능 추론 결과:", raw_result)

    # 기능 파싱
    if "기능 없음" in raw_result:
        feature_names = []
    else:
        feature_names = [fn.strip() for fn in raw_result.split(",") if fn.strip()]

    # state에 저장
    state["feature_names"] = feature_names
    return state


async def run_feature_implementation_check_test(state: Dict) -> Dict:
    # 1) state에서 기존 값 추출
    feature_name = state.get("feature_name")
    checklist_items = state.get("checklist", [])  # List of item strings
    full_files = state.get("fullFiles", [])        # List of dicts with 'file_path' & 'content'

    # 2) 전체 코드 합치기
    merged_code = "\n".join([
        f"# {f['file_path']}\n{f.get('content','')}" for f in full_files
    ])

    # 3) 프롬프트 생성
    prompt = checklist_evaluation_prompt.format_messages(
        featureName=feature_name,
        code=merged_code,
        checklist="\n".join([f"- {item}" for item in checklist_items])
    )

    # 4) LLM 호출 및 JSON 파싱
    response = await llm.ainvoke(prompt)
    parsed = json.loads(response.content)

    # 5) state 업데이트
    state["featureId"] = state.get("featureId")
    state["commitHash"] = state.get("commitHash")
    state["featureName"] = parsed.get("feature_name")
    state["implementsFeature"] = parsed.get("implements")
    state["checklistEvaluation"] = parsed.get("checklist_evaluation")
    state["extraImplemented"] = parsed.get("extra_implemented")
    state["checklistFileMap"] = parsed.get("checklist_file_map")

    # 6) 결과 state 반환
    return state


# 기능명 추론
# async def run_feature_inference(request: FeatureInferenceRequest) -> Dict:

#     # diff 전체를 하나로 합침
#     diff_text = "\n".join(
#         [f"# {f.filePath}\n{f.diff}" for f in request.diffFiles]
#     )

#     # 기능 목록도 문자열로
#     feature_list = "\n".join([f"- {f}" for f in request.availableFeatures])

#     prompt = feature_inference_prompt.format_messages(
#         diff_text=diff_text,
#         available_features=feature_list
#     )

#     response = await llm.ainvoke(prompt)
#     content = response.content.strip()

#     return {
#         "project_id": request.projectId,
#         "user_id": request.userId,
#         "commit_id": request.commitId,
#         "commit_hash": request.commitHash,
#         "feature_name": content  # 예: "JWT 발급 기능" 또는 "기능 없음"
#     }

# 기능 체크리스트 구현 확인 및 추가 구현 항목 확인
# 구현 완료된 체크리스트의 파일경로 저장 (코드리뷰에서 활용)
async def run_feature_implementation_check(request: ChecklistEvaluationRequest) -> Dict:
    featureName = request.featureName
    checklist = request.checklistEvaluation
    files = request.fullFiles

    # 전체 코드 합치기
    merged_code = "\n".join([f"# {f.filePath}\n{f.content}" for f in files])

    # checklist 평가용 프롬프트 만들기
    prompt = checklist_evaluation_prompt.format_messages(
        featureName=featureName,
        code=merged_code,
        checklist="\n".join([f"- {item}" for item in checklist])
    )

    response = await llm.ainvoke(prompt)
    parsed = json.loads(response.content)
    
    return {
        "featureId": request.featureId,
        "commitHash": request.commitHash,
        "featureName": parsed["feature_name"],
        "implementsFeature": parsed["implements"],
        "checklistEvaluation": parsed["checklist_evaluation"],
        "extraImplemented": parsed["extra_implemented"],
        "checklistFileMap": parsed["checklist_file_map"]
    }

# 체크리스트 True인 항목만 코드리뷰 실행
# True인 checklist 항목에 대한 코드리뷰 실행 (전제: Java에서 true인 항목의 파일만 전달됨)
async def run_feature_code_review(request: CodeReviewItemRequest) -> Dict:
    feature_id         = request.featureId
    feature_name       = request.featureName
    implements_feature = request.implementsFeature
    checklist_eval     = request.checklistEvaluation
    checklist_file_map = request.checklistFileMap   # ✅ 새로 추가됨
    full_files         = request.fullFiles

    checklist_reviews = []

    for checklist_item, passed in checklist_eval.items():
        if not passed:
            continue

        # checklistItem에 해당하는 파일 목록 추출
        file_paths = checklist_file_map.get(checklist_item, [])
        related_files = [f for f in full_files if f.filePath in file_paths]
        if not related_files:
            continue

        # 파일 내용 합치기
        merged_code = "\n".join([f"# {f.filePath}\n{f.content}" for f in related_files])

        review = await review_code_with_gpt(
            feature_name=feature_name,
            checklist_item=checklist_item,
            merged_code=merged_code
        )

        print(review)

        checklist_reviews.append({
            "checklistItem": review["checklist_item"],
            "summary":       review["summary"],
            "codeReviews":   review["code_reviews"]
        })

    return {
        "featureId":       feature_id,
        "featureName":     feature_name,
        "codeReviewItems": checklist_reviews,
        "implementation": request.implementsFeature
    }



# async def run_feature_code_review(request: CodeReviewItemRequest) -> Dict:
#     # 1) CamelCase로 key 조회
#     checklist_eval   = request.checklistEvaluation,
#     implements_feature = request.implementsFeature,
#     full_files       = request.fullFiles,             # 이제 반드시 들어와야 함
#     feature_name     = request.featureName,
#     feature_id       = request.featureId

#     # 2) 실제 리뷰 대상 체크리스트 항목 리스트
#     targets = [item for item, passed in checklist_eval.items() if passed] + extra

#     checklist_reviews = []
#     for item in targets:
#         paths = file_map.get(item, [])
#         files = [f for f in full_files if f.filePath in paths]
#         if not files:
#             continue

#         merged_code = "\n".join([f"# {f.filePath}\n{f.content}" for f in files])
#         review = await review_code_with_gpt(
#             feature_name=feature_name,
#             checklist_item=item,
#             merged_code=merged_code
#         )

#         # 리뷰 결과도 CamelCase로 매핑
#         checklist_reviews.append({
#             "checklistItem": review["checklist_item"],
#             "summary":       review["summary"],
#             "codeReviews":   review["code_reviews"]
#         })

#     # 3) FeatureReviewResult 스키마에 맞게 반환
#     return {
#         "featureId":       feature_id,
#         "featureName":     feature_name,
#         "codeReviewItems": checklist_reviews
#     }


# 코드리뷰 GPT 호출 및 JSON 응답 파싱
async def review_code_with_gpt(feature_name: str, checklist_item: str, merged_code: str) -> Dict:
    prompt = code_review_prompt.format_messages(
        feature_name=feature_name,
        item=checklist_item,
        merged_code=merged_code
    )

    response = await llm.ainvoke(prompt)
    print(response.content)

    try:
        parsed = json.loads(response.content)

        code_reviews = []
        for r in parsed.get("code_reviews", []):
            code_reviews.append({
                "category":  r.get("category", "기타"),
                "filePath":  r.get("filePath", "Unknown.java"),
                "lineRange": r.get("lineRange", "0-0"),
                "severity":  r.get("severity", "LOW"),
                "message":   r.get("message", "(내용 없음)")
            })

        return {
            "checklist_item": checklist_item,
            "summary": parsed.get("summary", ""),
            "code_reviews": code_reviews
        }

    except json.JSONDecodeError as e:
        return {
            "checklist_item": checklist_item,
            "summary": f"⚠️ JSON 파싱 실패: {str(e)}",
            "code_reviews": []
        }


# 코드 리뷰 요약
async def run_feature_review_summary(request: FeatureReviewSummaryRequest) -> dict:

    prompt = review_summary_prompt.format_messages(
        categorized_reviews=request.categorizedReviews
    )

    response = await llm.ainvoke(prompt)
    lines = response.content.strip().split("\n")

    print(response.content)

    def extract(prefix: str) -> str:
        for line in lines:
            if line.startswith(prefix):
                return line.replace(prefix, "").strip()
        return ""
    
    def extract_float(prefix: str) -> float:
        for line in lines:
            if prefix in line:
                try:
                    return float(line.split(prefix)[-1].strip())
                except ValueError:
                    return 0.0
        return 0.0


    def extract(prefix: str) -> str:
        for line in lines:
            if prefix in line:
                return line.split(prefix, 1)[-1].strip()
        return ""


    return {
        "summary_result": {
            "featureId": request.featureId,
            "featureName": request.featureName,
            "overallScore": extract_float("- 점수: "),
            "summary": extract("- 요약: "),
            "convention": extract("- 코딩 컨벤션: "),
            "refactorSuggestion": extract("- 리팩토링 제안: "),
            "complexity": extract("- 복잡도: "),
            "performance": extract("- 성능 최적화: "),
            "bugRisk": extract("- 버그 가능성: "),
            "securityRisk": extract("- 보안 위험: ")
        }
    }



# 유추한 기능명 java 로 전달
async def send_feature_inference_result_to_java(feature_name: str, commit_hash: str, commit_id: int):
    payload = {
        "featureName": feature_name,
        "commitHash": commit_hash,
        "commitId": commit_id
    }

    async with httpx.AsyncClient() as client:
        response = await client.post(f"{JAVA_API_URL}/feature-inference/result", json=payload)

    if response.status_code == 200:
        print("✅ 기능명 유추 결과 전송 성공")
    else:
        print("❌ 기능명 전송 실패:", response.status_code, response.text)
        

# 체크리스트 구현 확인 여부 및 추가 구현 항목 전달
async def send_checklist_evaluation_result_to_java(state: Dict):
    payload = {
        "featureId": state.get("feature_id"),
        "featureName": state["feature_name"],
        "checklistEvaluation": state["checklist_evaluation"],
        "extraImplemented": state["extra_implemented"]
    }

    async with httpx.AsyncClient() as client:
        response = await client.post(f"{JAVA_API_URL}/checklist-evaluation/result", json=payload)

    if response.status_code == 200:
        print("✅ checklist 평가 결과 전송 성공")
    else:
        print("❌ checklist 평가 전송 실패:", response.status_code, response.text)

    return state


# 코드 리뷰 결과를 Java에 전달
async def send_code_review_items_to_java(state: Dict):
    review_data = state["code_reviews"]  # ✅ 기능 단위로 감싼 리뷰 결과

    payload = {
        "featureId": review_data["feature_id"],
        "featureName": review_data["feature_name"],
        "checklistReviews": review_data["checklist_reviews"]  # ✅ key 이름 변경!
    }

    async with httpx.AsyncClient() as client:
        response = await client.post(f"{JAVA_API_URL}/code-review/items", json=payload)

    if response.status_code == 200:
        print("✅ 코드리뷰 항목 전송 성공")
    else:
        print("❌ 코드리뷰 항목 전송 실패:", response.status_code, response.text)

    return state



# 코드 리뷰 요약 java 전달
async def send_code_review_summary_to_java(state: Dict):
    payload = {
        "featureId": state.get("feature_id"),
        "featureName": state["feature_name"],
        "overallReviewSummary": state.get("overall_review_summary")
    }

    async with httpx.AsyncClient() as client:
        response = await client.post(f"{JAVA_API_URL}/code-review/summary", json=payload)

    if response.status_code == 200:
        print("✅ 전체 리뷰 요약 전송 성공")
    else:
        print("❌ 전체 요약 전송 실패:", response.status_code, response.text)

    return state


# 그래프 연결
builder = StateGraph(CodeReviewState)

# 노드 생성
builder.add_node("run_feature_inference", run_feature_inference)
builder.add_node("send_feature_inference_result_to_java", send_feature_inference_result_to_java)
builder.add_node("run_feature_implementation_check", run_feature_implementation_check)
builder.add_node("send_checklist_evaluation_result_to_java", send_checklist_evaluation_result_to_java)
builder.add_node("run_feature_code_review", run_feature_code_review)
builder.add_node("send_code_review_items_to_java", send_code_review_items_to_java)
builder.add_node("run_feature_review_summary", run_feature_review_summary)
builder.add_node("send_code_review_summary_to_java", send_code_review_summary_to_java)


# 노드 연결
builder.set_entry_point("run_feature_inference")

builder.add_edge("run_feature_inference", "send_feature_inference_result_to_java")
builder.add_edge("send_feature_inference_result_to_java", "run_feature_implementation_check")

builder.add_edge("run_feature_implementation_check", "send_checklist_evaluation_result_to_java")
builder.add_edge("send_checklist_evaluation_result_to_java", "run_feature_code_review")
builder.add_edge("run_feature_code_review", "send_code_review_items_to_java")
builder.add_edge("send_code_review_items_to_java", "run_feature_review_summary")


# 조건 분기
def should_summarize(state: Dict) -> str:
    return run_feature_review_summary if state.get("implements") else END


builder.add_conditional_edges(
    "send_code_review_items_to_java",
    should_summarize,  # 조건 함수 (True/False 반환)
    {
        True: "run_feature_review_summary",  # 조건 결과에 따른 다음 노드 이름 (문자열)
        False: END
    }
)


builder.add_edge("run_feature_review_summary", "send_code_review_summary_to_java")
builder.add_edge("send_code_review_summary_to_java", END)

graph = builder.compile()

