from enum import Enum
from pydantic import BaseModel
from typing import List


class DiffChangeType(str, Enum):
    ADD = "ADD"
    MODIFY = "MODIFY"
    DELETE = "DELETE"


class DiffFile(BaseModel):
    file_path: str
    patch: str
    change_type: DiffChangeType

class FullFile(BaseModel):
    fil_path: str
    content: str

class ChecklistItem(BaseModel):
    item: str
    done: bool

class CommitInfo(BaseModel):
    repo_name: str
    repo_owner: str

class FeatureInferenceRequest(BaseModel):
    project_id: int
    commit_id: int
    commit_hash: str
    commit_message: str
    diff_files: List[DiffFile]
    available_features: List[str]
    jwt_token: str
    commit_info: CommitInfo
