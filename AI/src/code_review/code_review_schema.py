from enum import Enum
from pydantic import BaseModel, Field, AliasChoices, model_validator
from typing import List, Literal, Optional, Dict


class DiffChangeType(str, Enum):
    ADD = "ADD"
    MODIFY = "MODIFY"
    DELETE = "DELETE"


# class DiffFile(BaseModel):
#     file_path: str = Field(alias="filePath")
#     patch: str
#     change_type: Literal["ADDED", "MODIFIED", "REMOVED"] = Field(alias="changeType")

#     class Config:
#         populate_by_name = True

class FullFile(BaseModel):
    fil_path: str
    content: str

class ChecklistItem(BaseModel):
    item: str
    done: bool

class ChecklistItemResult(BaseModel):
    feature_id: int
    checklist_items: List[ChecklistItem]


class DiffFile(BaseModel):
    file_path: str = Field(validation_alias=AliasChoices("file_path", "filePath"))
    patch: str = ""
    change_type: Literal["ADDED", "MODIFIED", "REMOVED"] = Field(
        default="MODIFIED",
        validation_alias=AliasChoices("change_type", "changeType", "status")
    )

    @model_validator(mode="after")
    def _normalize(self):
        s = (self.change_type or "").upper()
        if s in {"ADD", "ADDED", "A"}:
            self.change_type = "ADDED"
        elif s in {"DEL", "DELETE", "DELETED", "REMOVED", "R"}:
            self.change_type = "REMOVED"
        else:
            self.change_type = "MODIFIED"
        return self

class CommitInfo(BaseModel):
    repo_name: str = Field(validation_alias=AliasChoices("repo_name", "repoName"))
    repo_owner: str = Field(validation_alias=AliasChoices("repo_owner", "repoOwner"))

class FeatureInferenceRequest(BaseModel):
    project_id: int
    commit_id: Optional[int] = None
    commit_hash: Optional[str] = None
    commit_message: str = ""
    diff_files: List[DiffFile] = []
    available_features: List[str]
    access_token: str
    commit_info: Optional[CommitInfo] = Field(
        default=None,
        validation_alias=AliasChoices("commit_info", "commitInfo"),
    )
    commit_branch: Optional[str] = None
    force_done: bool = False  # 프론트/백에서 넘겨줄 수 있게 허용


class ReviewItem(BaseModel):
    file_path: str
    line_range: str
    severity: str
    message: str

class CodeReviewItem(BaseModel):
    category: str
    checklist_item: str
    items: List[ReviewItem]

class ManualCodeReviewRequest(BaseModel):
    project_id: int
    feature_name: str
    items: List[CodeReviewItem]

class ChecklistEvaluation(BaseModel):
    feature_name: str = Field(...)
    checklist_evaluation: Dict[str, bool] = Field(default_factory=dict)
    implemented: bool = Field(...)
    extra_implemented: List[str] = Field(default_factory=list)
    checklist_file_map: Dict[str, List[str]] = Field(default_factory=dict)