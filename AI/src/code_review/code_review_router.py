from fastapi import APIRouter
from .state import CodeReviewState
from .code_review_graph import code_review_graph

router = APIRouter()

@router.post("/feature-inference")
async def start_feature_inference(state: CodeReviewState):
    final_state = await code_review_graph.ainvoke(state)
    return {"status": "ok"}
