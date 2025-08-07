from langgraph.graph import StateGraph, END
from .nodes import (
    run_feature_inference,
    run_checklist_fetch,
    run_feature_implementation_check,
    run_code_review_file_fetch,
    run_feature_code_review,
    run_code_review_summary,
    send_result_to_java,
)

from ..state import CodeReviewState

builder = StateGraph(CodeReviewState)

builder.add_node("run_feature_inference", run_feature_inference)
builder.add_node("run_parallel_feature_graphs", run_parallel_feature_graphs)
builder.add_node("send_result_to_java", send_result_to_java)