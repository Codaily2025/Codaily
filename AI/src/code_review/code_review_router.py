import re, json
from fastapi import APIRouter, HTTPException, Depends, Request
from .code_review_schema import FeatureInferenceRequest, ManualCodeReviewRequest
from .state import CodeReviewState, ManualSummaryState
from .graph.nodes import run_code_review_summary
from .code_review_graph import code_review_graph
from fastapi.encoders import jsonable_encoder

router = APIRouter()

# \t(\x09), \n(\x0A), \r(\x0D) 제외하고 제거
CONTROL_CHARS = re.compile(rb"[\x00-\x08\x0B-\x0C\x0E-\x1F\x7F]")

def escape_lf_cr_tab_inside_strings(data: bytes) -> bytes:
    out = bytearray()
    in_string = False
    escaped = False
    for b in data:
        if not in_string:
            if b == 0x22:
                in_string = True
                out.append(b)
            else:
                out.append(b)
            continue
        if escaped:
            out.append(b)
            escaped = False
            continue
        if b == 0x5C:
            out.append(b)
            escaped = True
            continue
        if b == 0x22:
            in_string = False
            out.append(b)
            continue
        if b == 0x0A:
            out.extend(b"\\n")
        elif b == 0x0D:
            out.extend(b"\\r")
        elif b == 0x09:
            out.extend(b"\\t")
        else:
            out.append(b)
    return bytes(out)

import base64

async def sanitize_feature_inference_request(request: Request) -> FeatureInferenceRequest:
    raw = await request.body()

    # 1) 제어문자 제거 (\t,\n,\r 제외)
    cleaned = CONTROL_CHARS.sub(b"", raw)
    # 2) 문자열 내부의 실제 \n,\r,\t 를 이스케이프
    cleaned = escape_lf_cr_tab_inside_strings(cleaned)

    try:
        payload = json.loads(cleaned.decode("utf-8"))
    except json.JSONDecodeError as e:
        pos = getattr(e, "pos", None)
        if pos is not None:
            start = max(0, pos - 80)
            end = min(len(cleaned), pos + 80)
            window = cleaned[start:end]

            # 보기 좋게 두 종류로 출력: (1) raw bytes repr, (2) base64
            print("\n[JSON DEBUG] error pos:", pos)
            print("[JSON DEBUG] raw slice repr:\n", repr(window))
            print("[JSON DEBUG] base64 slice:\n", base64.b64encode(window).decode("ascii"))

        # 에러 원문도 함께 전달
        raise HTTPException(status_code=400, detail=f"Invalid JSON after sanitize: {e}")
    
    try:
        return FeatureInferenceRequest(**payload)
    except Exception as e:
        raise HTTPException(status_code=422, detail=f"Invalid payload schema: {e}")


@router.post("/feature-inference")
async def start_feature_inference(
    req: FeatureInferenceRequest = Depends(sanitize_feature_inference_request)
):
    if req is None:
        raise HTTPException(status_code=400, detail="Sanitizer returned None")  # 가드

    print("Received from Java:", req.dict())

    state: CodeReviewState = {
        "project_id": req.project_id,
        "commit_id": req.commit_id,
        "commit_hash": req.commit_hash,
        "commit_message": req.commit_message,
        "available_features": req.available_features,
        "access_token": req.access_token,
        "diff_files": [df.dict() for df in req.diff_files],
        "commit_info": req.commit_info.dict() if req.commit_info else {},
        "commit_branch": req.commit_branch,
        "force_done": bool(req.force_done)
    }

    initial_state = jsonable_encoder(state)
    final_state = await code_review_graph.ainvoke(initial_state)

    return {
        "status": "ok",
        "diff_files": jsonable_encoder(final_state.get("diff_files", [])),
    }


@router.post("/code-review/manual")
async def run_manual_summary(req: ManualCodeReviewRequest):
    # run_code_review_summary에서 쓰는 필드 맞춰서 state 구성
    state: CodeReviewState = {
        "feature_name": req.feature_name,
        "code_review_items": [i.model_dump() for i in req.items],  # ★ dict로 변환
        "code_review_items_java": [],    # 없으니 빈 리스트
    }

    final_state = await run_code_review_summary(state)
    return {
        "summary": final_state.get("review_summaries"),
        "quality_score": final_state.get("quality_score", 0)
    }
