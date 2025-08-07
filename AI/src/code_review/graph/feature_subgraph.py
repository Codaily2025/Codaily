from langgraph.graph import StateGraph
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

def build_feature_subgraph() -> StateGraph:
    builder = StateGraph(CodeReviewState)

    # 중간 자바 저장 로직 적어야 함
    
    builder.add_node("run_checklist_fetch", run_checklist_fetch)
    builder.add_node("run_feature_implementation_check", run_feature_implementation_check)
    builder.add_node("run_feature_code_review", run_feature_code_review)
    builder.add_node("run_feature_review_summary", run_feature_review_summary)

    builder.set_entry_point("run_checklist_fetch")
    builder.add_edge("run_checklist_fetch", "run_feature_implementation_check")
    builder.add_edge("run_feature_implementation_check", "run_feature_code_review")

    def should_summarize(state: CodeReviewState) -> bool:
        return state.get("implements", False)

    builder.add_conditional_edges(
        "run_feature_code_review",
        should_summarize,
        if_true="run_feature_review_summary",
        if_false=None
    )

    return builder.compile()
