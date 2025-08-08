from pydantic import BaseModel
from typing import List, Dict, Optional
from .diff_file import DiffFile
from .full_file import FullFile

class FeatureInferenceRequest(BaseModel):
    projectId: int
    userId: int
    commitId: int
    commitHash: str
    diffFiles: List[DiffFile]
    availableFeatures: List[str]

class FeatureInferenceResponse(BaseModel):
    projectId: int
    userId: int
    commitId: int
    commitHash: str
    featureName: str

class ChecklistItem(BaseModel):
    item: str
    done: bool

class ChecklistEvaluationRequest(BaseModel):
    projectId: int
    featureId: int
    commitHash: str
    featureName: str
    fullFiles: List[FullFile]
    checklist: List[ChecklistItem]

class ChecklistEvaluationResponse(BaseModel):
    featureId: int
    featureName: str
    commitHash: str
    implementsFeature: bool
    checklistEvaluation: Dict[str, bool]
    extraImplemented: List[str]
    checklistFileMap: Dict[str, List[str]]


class CodeReviewItemRequest(BaseModel):
    featureId: int
    featureName: str
    implementsFeature: bool
    checklistFileMap: Dict[str, List[str]]
    checklistEvaluation: Dict[str, bool]
    fullFiles: List[FullFile]

class CodeReviewItem(BaseModel):
    category: str
    filePath: str
    lineRange: str
    severity: str
    message: str

class ChecklistReviewResult(BaseModel):
    checklistItem: str
    summary: str
    codeReviews: List[CodeReviewItem]

class FeatureReviewResult(BaseModel):
    featureId: int
    featureName: str
    codeReviewItems: List[ChecklistReviewResult]
    implementation: bool

class FeatureReviewSummaryRequest(BaseModel):
    featureId: int
    featureName: str
    categorizedReviews: Dict[str, List[str]]

class FeatureReviewSummary(BaseModel):
    featureId: int
    featureName: str
    overallScore: float
    summary: str
    convention: str
    bugRisk: str
    securityRisk: str
    performance: str
    refactorSuggestion: str
    complexity: str

class CodeReviewState(BaseModel):
    # 입력값
    projectId: Optional[int] = None
    commitId: Optional[int] = None
    commitHash: Optional[str] = None
    diffFiles: Optional[List[DiffFile]] = []
    availableFeatures: Optional[List[str]] = []

    # 추론 결과
    feature_name: Optional[str] = None
    feature_id: Optional[int] = None

    # 체크리스트 관련
    fullFiles: Optional[List[FullFile]] = []
    checklist: Optional[List[str]] = []
    checklist_evaluation: Optional[Dict[str, bool]] = {}
    extra_implemented: Optional[List[str]] = []
    checklist_file_map: Optional[Dict[str, List[str]]] = {}

    # 코드리뷰
    code_review_items: Optional[List[CodeReviewItem]] = []
    overall_review_summary: Optional[str] = None
    implements: Optional[bool] = None
    summary_result: Optional[Dict]

    feature_review_summary: Optional[FeatureReviewSummary] = None


# feature checklist 생성
class FeatureChecklistFeature(BaseModel):
    featureId: int
    title: str
    description: str

class FeatureChecklistRequest(BaseModel):
    features: List[FeatureChecklistFeature]

class FeatureChecklistResponse(BaseModel):
    checklistMap: Dict[int, List[str]]