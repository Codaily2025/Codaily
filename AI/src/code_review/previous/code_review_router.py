from fastapi import APIRouter, HTTPException
from .schema import FeatureInferenceRequest, FeatureInferenceResponse
from .schema import ChecklistEvaluationResponse,ChecklistEvaluationRequest
from .schema import FeatureReviewResult, CodeReviewItemRequest
from .schema import FeatureReviewSummary, FeatureReviewSummaryRequest
from .schema import FeatureChecklistResponse, FeatureChecklistRequest
from .code_review_graph import run_feature_inference, run_feature_implementation_check,run_feature_code_review, run_feature_review_summary, graph
from fastapi.responses import JSONResponse

router = APIRouter()


@router.post("/feature-inference", response_model=FeatureInferenceResponse)
async def feature_inference(request: FeatureInferenceRequest):
    result = await run_feature_inference(request)
    return FeatureInferenceResponse(**result)
    

@router.post("/checklist-evaluation", response_model=ChecklistEvaluationResponse)
async def checklist_evaluation(request: ChecklistEvaluationRequest):
    result = await run_feature_implementation_check(request)
    return ChecklistEvaluationResponse(**result)
    

@router.post("/code-review/items", response_model=FeatureReviewResult)
async def code_review_items(request: CodeReviewItemRequest):
    result = await run_feature_code_review(request)
    return FeatureReviewResult(**result)


@router.post("/code-review/summary", response_model=FeatureReviewSummary)
async def code_review_summary(request: FeatureReviewSummaryRequest):
    result = await run_feature_review_summary(request)
    return FeatureReviewSummary(**result["summary_result"])
