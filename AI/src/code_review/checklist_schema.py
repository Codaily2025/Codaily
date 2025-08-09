from pydantic import BaseModel
from typing import List, Dict

# feature checklist 생성
class FeatureChecklistFeature(BaseModel):
    featureId: int
    title: str
    description: str

class FeatureChecklistRequest(BaseModel):
    features: List[FeatureChecklistFeature]

class FeatureChecklistResponse(BaseModel):
    checklistMap: Dict[int, List[str]]