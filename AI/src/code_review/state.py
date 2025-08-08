from typing import List, Dict, Optional
from pydantic import BaseModel
from .code_review_schema import DiffFile, FullFile, ChecklistItem, CommitInfo


class CodeReviewState(BaseModel):
    jwt_token: Optional[str] = None

    # 자바에서 받아오는 입력값
    project_id: Optional[int] = None
    commit_id: Optional[int] = None
    commit_hash: Optional[str]
    diff_files: Optional[List[DiffFile]] = None
    available_features: Optional[List[str]] = None
    commit_info: Optional[CommitInfo] = None

    commit_message: Optional[str] = None
    force_done: Optional[bool] = False

    # 기능 추론 결과
    feature_names: Optional[List[str]] = None
    
    # 기능별 전체 코드
    full_files: Optional[List[FullFile]] = None

    # 기능 단일 추론 결과 (1개씩 돌릴 때)
    feature_name: Optional[str] = None
    checklist: Optional[List[ChecklistItem]] = None

    # checklist 평가 결과
    implements: Optional[bool] = None
    checklist_evaluation: Optional[Dict[str, bool]] = None
    extra_implemented: Optional[List[str]] = None
    checklist_file_map: Optional[Dict[str, List[str]]] = None

    # 코드리뷰 결과
    code_review_items: Optional[List[dict]] = None   # 각 항목별 리뷰 리스트
    review_summary: Optional[dict] = None            # 요약 정보
