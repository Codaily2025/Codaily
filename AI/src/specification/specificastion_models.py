from typing import List
from pydantic import BaseModel


class FeatureRef(BaseModel):
    id: int
    title: str
    field: str


class MessageRequest(BaseModel):
    message: str
    mainFeatures: List[FeatureRef] = []
