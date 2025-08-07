from fastapi import APIRouter
from .code_review_schema import CodeReviewState
from .graph.main_graph import code_review_graph

router = APIRouter()

@router.post("/api/feature-inference")
async def start_feature_inference(state: CodeReviewState):
    final_state = await code_review_graph.ainvoke(state)
    return {"status": "ok"}
