import re, json
from fastapi import APIRouter, HTTPException, Depends, Request
from .code_review_schema import FeatureInferenceRequest
from .state import CodeReviewState
from .code_review_graph import code_review_graph
from fastapi.encoders import jsonable_encoder

router = APIRouter()

# \t(\x09), \n(\x0A), \r(\x0D) 제외하고 제거
CONTROL_CHARS = re.compile(rb"[\x00-\x08\x0B-\x0C\x0E-\x1F\x7F]")

def strip_omission_marker_lines(data: bytes) -> bytes:
    """
    raw JSON 바이트를 줄단위로 보고,
    diff patch 안에 끼어든 '+      ... (중략)' 같은 마커 줄을 제거한다.
    """
    out = []
    for line in data.splitlines(keepends=True):
        s = line.strip()
        # '+ ... (중략)' 또는 '+ ... (omitted/snip/cut)' 같은 줄 제거
        if s.startswith(b'+') and (
            b'(중략)' in s or b'(omitted)' in s or b'(snip)' in s or b'(cut)' in s
        ):
            continue
        out.append(line)
    return b''.join(out)

def escape_lf_cr_tab_inside_strings(data: bytes) -> bytes:
    """문자열 내부의 실제 \n,\r,\t 를 \\n, \\r, \\t 로 바꿈"""
    out = bytearray()
    in_string = False
    escaped = False
    for b in data:
        if not in_string:
            if b == 0x22:  # "
                in_string = True
                out.append(b)
            else:
                out.append(b)
            continue
        if escaped:
            out.append(b)
            escaped = False
            continue
        if b == 0x5C:      # '\'
            out.append(b)
            escaped = True
            continue
        if b == 0x22:      # '"'
            in_string = False
            out.append(b)
            continue
        if b == 0x0A: out.extend(b"\\n")
        elif b == 0x0D: out.extend(b"\\r")
        elif b == 0x09: out.extend(b"\\t")
        else: out.append(b)
    return bytes(out)

async def sanitize_feature_inference_request(request: Request) -> FeatureInferenceRequest:
    raw = await request.body()

    # (A) 중략 마커 라인 제거 (가장 먼저)
    raw = strip_omission_marker_lines(raw)

    # (B) 제어문자 제거 (\t,\n,\r 제외)
    cleaned = CONTROL_CHARS.sub(b"", raw)

    # (C) 문자열 내부 개행/탭 이스케이프
    cleaned = escape_lf_cr_tab_inside_strings(cleaned)

    # (D) JSON 파싱
    try:
        payload = json.loads(cleaned.decode("utf-8"))
    except json.JSONDecodeError as e:
        pos = getattr(e, "pos", None)
        if pos is not None:
            start = max(0, pos - 80); end = min(len(cleaned), pos + 80)
            window = cleaned[start:end]
            # 필요 시 잠깐 찍어서 확인
            print("\n[JSON DEBUG] pos:", pos)
            print("[JSON DEBUG] raw slice repr:\n", repr(window))
        raise HTTPException(status_code=400, detail=f"Invalid JSON after sanitize: {e}")

    # (E) 모델 검증
    try:
        return FeatureInferenceRequest(**payload)
    except Exception as e:
        raise HTTPException(status_code=422, detail=f"Invalid FeatureInferenceRequest: {e}")

@router.post("/feature-inference")
async def start_feature_inference(
    req: FeatureInferenceRequest = Depends(sanitize_feature_inference_request)
):
    print("Received from Java:", req.dict())

    state: CodeReviewState = {
        "project_id": req.project_id,
        "commit_id": req.commit_id,
        "commit_hash": req.commit_hash,
        "commit_message": req.commit_message,
        "available_features": req.available_features,
        "jwt_token": req.jwt_token,
        "diff_files": [df.dict() for df in req.diff_files],
        "commit_info": req.commit_info.dict() if req.commit_info else None,
        "commit_branch": req.commit_branch,
        "force_done": bool(req.force_done),
    }

    initial_state = jsonable_encoder(state)
    final_state = await code_review_graph.ainvoke(initial_state)

    return {
        "status": "ok",
        "diff_files": jsonable_encoder(final_state.get("diff_files", [])),
    }
