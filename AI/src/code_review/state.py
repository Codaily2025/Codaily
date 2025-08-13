# src/code_review/state_schema.py
from typing import TypedDict, List, Dict, Optional, Any, Annotated
from pydantic import BaseModel


class CommitInfoDict(TypedDict, total=False):
    repo_name: str
    repo_owner: str

class ChecklistItem(TypedDict, total= False):
    item: str
    done: bool

class ChecklistItemResult(TypedDict, total=False):
    feature_id: int
    checklist_items: List[ChecklistItem]


# 참고:
# - total=False: 모든 키가 Optional 취급 (기본값 지정 X)
# - LangGraph 머지 편하게 하려고 dict 기반으로 표준화함

def prefer_non_empty(old, new):
    # 새 값이 비어있지 않으면 새 값, 비어있으면 기존 값 유지
    return new if new else old

def prefer_non_null(old, new):
    return new if new is not None else old

class CodeReviewState(TypedDict, total=False):
    # 인증
    jwt_token: Optional[str]

    # 자바 → 파이썬 입력
    project_id: Optional[int]
    commit_id: Optional[int]
    commit_hash: Optional[str]
    commit_message: Optional[str]
    force_done: Optional[bool]
    commit_branch: Optional[str]
    force_done: bool

    # diff/전체코드 (dict로 표준화)
    # 예: {"file_path": str, "patch": str, "change_type": "ADDED|MODIFIED|REMOVED"}
    diff_files: List[Dict[str, Any]]

    # 전체 파일은 필요 시만
    # 예: {"path": str, "content": str} 등 프로젝트 정의에 맞춤
    full_files: List[Dict[str, Any]]

    # 기타 입력
    available_features: List[str]
    # CommitInfo도 dict로 정규화 권장 (Pydantic 객체면 .model_dump 사용)
    commit_info: Optional[CommitInfoDict]
    

    # 기능 추론 결과 (복수/단일)
    feature_names: List[str]   # 여러 개 추론 시
    feature_name: Optional[str]  # 단일 추론 시 선택

    # 체크리스트/평가
    feature_id: Annotated[Optional[int], prefer_non_null]
    checklist: List[Dict[str, Any]]              # {id, title,...} 등 자유
    implements: Optional[bool]
    checklist_evaluation: Dict[str, bool]        # {"항목명": true/false}
    extra_implemented: List[str]
    checklist_file_map: Dict[str, List[str]]     # {"항목명": ["a.java","b.java"]}

    # 코드리뷰 결과
    review_files: Annotated[List[Dict[str, Any]], prefer_non_empty]
    review_summary: Optional[str]
    review_summaries: Dict[str, str]
    code_review_items: List[Dict[str, Any]]
