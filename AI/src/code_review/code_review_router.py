import re, json
from fastapi import APIRouter, Request, Depends, HTTPException
from .code_review_schema import FeatureInferenceRequest
from .state import CodeReviewState
from .state_init import init_state_from_request
from .code_review_graph import code_review_graph
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
        # 여기서 dict 변환 (patch 포함)
        "diff_files": [df.dict() for df in req.diff_files],
        "commit_info": req.commit_info.dict() if req.commit_info else None,
        "commit_branch": req.commit_branch,
        "force_done": bool(req.force_done)
    }

    initial_state = jsonable_encoder(state)  # dict 상태로 인코딩
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


# CONTROL_CHARS = re.compile(rb"[\x00-\x08\x0B-\x0C\x0E-\x1F\x7F]")  # \r,\n,\t 제외 + DEL(0x7F)

# async def sanitize_and_parse_request(request: Request) -> FeatureInferenceRequest:
#     raw = await request.body()
#     clean = CONTROL_CHARS.sub(b"", raw)

#     try:
#         payload = json.loads(clean.decode("utf-8"))
#     except json.JSONDecodeError as e:
#         # 여기까지 와도 실패면, 진짜 JSON 자체가 잘못된 케이스
#         raise HTTPException(status_code=400, detail=f"Invalid JSON after sanitize: {e}")

#     try:
#         return FeatureInferenceRequest(**payload)
#     except Exception as e:
#         # 필드 검증 실패 등
#         raise HTTPException(status_code=422, detail=f"Invalid request payload: {e}")

# @router.post("/feature-inference")
# async def start_feature_inference(
#     req: FeatureInferenceRequest = Depends(sanitize_and_parse_request)
# ):
#     state: CodeReviewState = init_state_from_request(req)
#     result: CodeReviewState = await code_review_graph.ainvoke(state)
#     return {"ok": True, "state_keys": list(result.keys())}
