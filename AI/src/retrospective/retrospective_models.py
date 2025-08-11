# src/retrospective/models.py
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import date

class RetrospectiveIssueSummary(BaseModel):
    featureTitle: Optional[str] = None
    checklistItem: Optional[str] = None
    category: Optional[str] = None
    severity: Optional[str] = None
    message: Optional[str] = None
    filePath: Optional[str] = None
    model_config = {"extra": "ignore"}          # ← 예상치 못한 필드 무시

class RetrospectiveFeatureSummary(BaseModel):
    featureId: Optional[int] = None
    title: Optional[str] = None
    field: Optional[str] = None
    checklistCount: Optional[int] = 0
    checklistDoneCount: Optional[int] = 0
    codeQualityScore: Optional[float] = 0.0
    summary: Optional[str] = ""
    reviewIssues: List[RetrospectiveIssueSummary] = Field(default_factory=list)
    model_config = {"extra": "ignore"}

class RetrospectiveProductivityMetrics(BaseModel):
    codeQuality: float
    productivityScore: float
    completedFeatures: int
    totalCommits: int
    model_config = {"extra": "ignore"}

class RetrospectiveSummary(BaseModel):
    overall: str
    strengths: str
    improvements: str
    risks: str

class RetrospectiveGenerateRequest(BaseModel):
    date: date
    projectId: int
    userId: int
    triggerType: Optional[str] = None                   # ← Enum 직렬화 안전빵
    completedFeatures: List[RetrospectiveFeatureSummary] = Field(default_factory=list)  
    # ↑ null이 오면 422라서 기본 빈 리스트로
    productivityMetrics: RetrospectiveProductivityMetrics
    model_config = {"extra": "ignore"}                  # ← 여유롭게

class RetrospectiveGenerateResponse(BaseModel):
    date: date
    projectId: int
    userId: int
    triggerType: str
    contentMarkdown: str
    summary: RetrospectiveSummary
    productivityMetrics: RetrospectiveProductivityMetrics
    completedFeatures: List[RetrospectiveFeatureSummary]
    reviewIssuesTop: List[RetrospectiveIssueSummary]
