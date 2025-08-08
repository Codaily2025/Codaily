from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Dict
import httpx
import logging


# LangGraph 노드 함수 임포트
from src.code_review.code_review_graph import run_feature_inference_test, run_feature_implementation_check_test

JAVA_CALLBACK = "http://localhost:8080/api/python"
logger = logging.getLogger("uvicorn.error")

app = FastAPI()

class DiffFile(BaseModel):
    file_path: str
    patch: str

class FeatureInferenceRequestDto(BaseModel):
    project_id: int
    commit_id: int
    diff_files: List[DiffFile]
    available_features: List[str]


class ChecklistEvaluationRequestDto(BaseModel):
    project_id: int
    commit_id: int
    feature_names: List[str]
    full_files: List[Dict[str, str]]     # {"file_path": ..., "content": ...}
    checklist: List[Dict[str, str]]    

@app.post("/api/python/feature-inference")
async def feature_inference_endpoint(request: FeatureInferenceRequestDto):
    # 1) state 초기화
    state = {
        "projectId": request.project_id,
        "commitId": request.commit_id,
        "diffFiles": [ {"file_path": f.file_path, "patch": f.patch} for f in request.diff_files ],
        "availableFeatures": request.available_features
    }

    # 2) LangGraph 노드 실행
    result_state = await run_feature_inference_test(state)

    # 3) 결과에서 feature_names 추출
    feature_names = result_state.get("feature_name")
    if not isinstance(feature_names, list):
        feature_names = [feature_names] if feature_names else []

    # 4) Java 콜백 전송 (예외 처리 포함)
    try:
        async with httpx.AsyncClient() as client:
            resp = await client.post(
                f"{JAVA_CALLBACK}/feature-inference/result",
                json={
                    "commit_id": request.commit_id,
                    "feature_names": feature_names
                },
                timeout=5.0
            )
            resp.raise_for_status()
            logger.info(f"✅ Java 콜백 전송 성공: {resp.status_code}")
    except httpx.RequestError as e:
        logger.warning(f"⚠️ Java 콜백 전송 실패: {e}")
    except httpx.HTTPStatusError as e:
        logger.error(f"❌ Java 콜백 오류 상태 코드: {e.response.status_code}")

    return {"status": "accepted"}


@app.post("/api/python/checklist-evaluation")
async def checklist_evaluation_endpoint(request: ChecklistEvaluationRequestDto):
    # state 초기화 or update
    state = {
        "projectId": request.project_id,
        "commitId": request.commit_id,
        "featureNames": request.feature_names,
        "fullFiles": request.full_files,
        "checklist": request.checklist
    }
    result_state = await run_feature_implementation_check_test(state)

    eval_map = result_state.get("checklist_evaluation")
    extras   = result_state.get("extra_implemented")

    # Java 콜백
    async with httpx.AsyncClient() as client:
        try:
            resp = await client.post(
                f"{JAVA_CALLBACK}/checklist-evaluation/result",
                json={
                    "project_id": request.project_id,
                    "commit_id": request.commit_id,
                    "checklist_evaluation": eval_map,
                    "extra_implemented": extras
                },
                timeout=5.0
            )
            resp.raise_for_status()
            logger.info(f"✅ Java checklist-evaluation callback: {resp.status_code}")
        except Exception as e:
            logger.error(f"❌ checklist-evaluation callback failed: {e}")

    return {"status": "accepted"}