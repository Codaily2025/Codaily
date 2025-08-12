# src/retrospective/retrospective_router.py
from fastapi import APIRouter
from src.retrospective.retrospective_models import (
    RetrospectiveGenerateRequest,
    RetrospectiveGenerateResponse,
)
from src.retrospective.retrospective_service import generate_retrospective

router = APIRouter()


@router.post("/generate", response_model=RetrospectiveGenerateResponse)
async def post_generate_retrospective(req: RetrospectiveGenerateRequest):
    return await generate_retrospective(req)
