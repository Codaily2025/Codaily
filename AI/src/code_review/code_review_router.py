from fastapi import APIRouter
from fastapi.encoders import jsonable_encoder

from .state import CodeReviewState
from .code_review_graph import code_review_graph
from .code_review_schema import FeatureInferenceRequest
from fastapi.encoders import jsonable_encoder

router = APIRouter()

@router.post("/feature-inference")
async def start_feature_inference(req: FeatureInferenceRequest):
    print("Received from Java:", req.dict())

    state: CodeReviewState = {
        "project_id": req.project_id,
        "commit_id": req.commit_id,
        "commit_hash": req.commit_hash,
        "commit_message": req.commit_message,
        "available_features": req.available_features,
        "jwt_token": req.jwt_token,
        "diff_files": req.diff_files,  # BaseModel 그대로
        "commit_info": req.commit_info.dict() if req.commit_info else None,
        "commit_branch": req.commit_branch
    }
    initial_state = jsonable_encoder(state)  # ← DiffFile까지 전부 dict로 정규화
    final_state = await code_review_graph.ainvoke(initial_state)
    return {
        "status": "ok",
        "diff_files": jsonable_encoder(final_state.get("diff_files", []))
    }

def escape_json_string(text: str) -> str:
    return (
        text.replace("\\", "\\\\")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\"", "\\\"")  # 큰따옴표 escape도 포함
    )
