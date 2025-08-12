from enum import Enum
from pydantic import BaseModel, Field, AliasChoices, ValidationError
from typing import List, Literal, Optional


class DiffChangeType(str, Enum):
    ADD = "ADD"
    MODIFY = "MODIFY"
    DELETE = "DELETE"


class DiffFile(BaseModel):
    file_path: str = Field(alias="filePath")
    patch: str
    change_type: Literal["ADDED", "MODIFIED", "REMOVED"] = Field(alias="changeType")

    class Config:
        populate_by_name = True

class FullFile(BaseModel):
    fil_path: str
    content: str

class ChecklistItem(BaseModel):
    item: str
    done: bool

class ChecklistItemResult(BaseModel):
    feature_id: int
    checklist_items: List[ChecklistItem]

class CommitInfo(BaseModel):
    repo_name: str = Field(validation_alias=AliasChoices("repo_name", "repoName"))
    repo_owner: str = Field(validation_alias=AliasChoices("repo_owner", "repoOwner"))

    model_config = {
        "populate_by_name": True,
        "extra": "ignore",
    }

class FeatureInferenceRequest(BaseModel):
    project_id: int
    commit_id: int
    commit_hash: str
    commit_message: str
    diff_files: List[DiffFile]
    available_features: List[str]
    jwt_token: str
    commit_info: Optional[CommitInfo] = Field(default=None,
                                              validation_alias=AliasChoices("commit_info", "commitInfo"))
    commit_branch: str
