# state_init.py
from typing import Dict, Any
from .code_review_schema import FeatureInferenceRequest

def init_state_from_request(req: FeatureInferenceRequest) -> Dict[str, Any]:
    # Diff 표준 dict로 변환
    diffs = [
        {"file_path": f.file_path, "patch": f.patch, "change_type": f.change_type}
        for f in req.diff_files
    ]

    state: Dict[str, Any] = {
        "project_id": req.project_id,
        "commit_id": req.commit_id or "",
        "commit_hash": req.commit_hash or "",
        "commit_message": req.commit_message or "",
        "commit_branch": req.commit_branch or "",
        "available_features": req.available_features or [],
        "diff_files": diffs or [],
        "access_token": req.access_token,
        "commit_info": req.commit_info.model_dump() if req.commit_info else None,

        # 파이프라인 공통 키(초기값)
        "feature_name": "",               # 이후 inference에서 채움
        "checklist": [],                  # run_checklist_fetch에서 채움
        "checklist_evaluation": {},       # 평가 결과
        "extra_implemented": [],          # 추가 구현 항목
        "checklist_file_map": {},         # 항목→파일 매핑
        "code_review_items": [],          # 이번 파이프라인에서 생성된 아이템
        "code_review_items_existing": [], # 자바에서 가져온 기존 아이템
        "implemented": False,
        "go_summary": False,              # 구현판단 후 분기에서 설정
        "force_done": bool(req.force_done),
        "review_summaries": [],

        # (선택) 이전 상태 대비 증분 판단용: 이후 DB에서 로드
        "prev_checklist_eval": {},
        "prev_extra_implemented": [],
        "risky_change": False,            # 이후 감지 로직에서 설정
    }
    return state