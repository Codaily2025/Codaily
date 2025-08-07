from ..state import CodeReviewState
from code_review.graph.feature_subgraph import build_feature_subgraph


async def run_feature_router(state: CodeReviewState) -> CodeReviewState:
    feature_names = state.get("feature_names", [])
    feature_subgraph = build_feature_subgraph()
    results = {}

    for feature_name in feature_names:
        sub_state = {
            **state,
            "feature_name": feature_name,
        }

        result = await feature_subgraph.ainvoke(sub_state)
        results[feature_name] = result

    state["feature_results"] = results
    return state