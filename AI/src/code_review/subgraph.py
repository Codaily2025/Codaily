from langgraph.graph import StateGraph, END
from src.code_review.state import CodeReviewState
from .graph.nodes import (
    run_checklist_fetch,
    run_feature_implementation_check,
    apply_checklist_evaluation,
    run_feature_code_review,
    run_code_review_file_fetch,
    run_code_review_item_fetch,
    run_code_review_summary,
)

def create_feature_graph():
    
    builder = StateGraph(CodeReviewState)

    # ── 노드 등록 ─────────────────────────────────────────────
    builder.add_node("run_checklist_fetch", run_checklist_fetch)
    builder.add_node("run_feature_implementation_check", run_feature_implementation_check)
    builder.add_node("apply_checklist_evaluation", apply_checklist_evaluation)
    builder.add_node("run_code_review_file_fetch", run_code_review_file_fetch)
    builder.add_node("run_feature_code_review", run_feature_code_review)
    builder.add_node("run_code_review_item_fetch", run_code_review_item_fetch)
    builder.add_node("run_code_review_summary", run_code_review_summary)

    # ── 시작 노드 ─────────────────────────────────────────────
    builder.set_entry_point("run_checklist_fetch")
    builder.add_edge("run_checklist_fetch", "run_feature_implementation_check")

    # ── 분기 1: 구현 체크 결과에 따른 요약 직행 vs 세부 코드리뷰 ──
    # state["go_summary"] 가 True → 바로 요약 노드로
    # False → 체크리스트 평가 적용 → 파일 매핑 → 코드리뷰
    def route_after_impl_check(state: CodeReviewState) -> str:
        return "to_summary" if bool(state.get("go_summary")) else "to_detail"

    builder.add_conditional_edges(
        "run_feature_implementation_check",
        route_after_impl_check,
        {
            "to_summary": "run_code_review_summary",
            "to_detail": "apply_checklist_evaluation",
        },
    )

    # 세부 리뷰 경로
    builder.add_edge("apply_checklist_evaluation", "run_code_review_file_fetch")
    builder.add_edge("run_code_review_file_fetch", "run_feature_code_review")

    # ── 분기 2: 세부 리뷰 이후 최종 요약 여부 ──────────────────
    # 세부 리뷰를 돈 경우엔 implements 기준으로 요약 수행 (미구현이면 종료)
    def should_run_code_review_summary(state: CodeReviewState) -> bool:
        return bool(state.get("implemented"))  # run_feature_implementation_check에서 설정됨

    builder.add_conditional_edges(
        "run_feature_code_review",
        should_run_code_review_summary,
        {
            True: "run_code_review_item_fetch",
            False: END,
        },
    )

    builder.add_edge("run_code_review_item_fetch", "run_code_review_summary")
    # ── 종료 지점 ──────────────────────────────────────────────
    builder.set_finish_point("run_code_review_summary")

    return builder.compile()


# from langgraph.graph import StateGraph, END
# from src.code_review.state import CodeReviewState
# from .graph.nodes import (
#     run_checklist_fetch,
#     run_feature_implementation_check,
#     apply_checklist_evaluation,
#     run_feature_code_review,
#     run_code_review_file_fetch,
#     run_code_review_summary,
# )


# def create_feature_graph():
#     builder = StateGraph(CodeReviewState)

#     # 노드 등록
#     builder.add_node("run_checklist_fetch", run_checklist_fetch)
#     builder.add_node("run_feature_implementation_check", run_feature_implementation_check)
#     builder.add_node("apply_checklist_evaluation", apply_checklist_evaluation)
#     builder.add_node("run_code_review_file_fetch", run_code_review_file_fetch)
#     builder.add_node("run_feature_code_review", run_feature_code_review)
#     builder.add_node("run_code_review_summary", run_code_review_summary)

#     # 시작 노드
#     builder.set_entry_point("run_checklist_fetch")
#     builder.add_edge("run_checklist_fetch", "run_feature_implementation_check")

#     # checklist 평가 → 분기
#     def should_review(state: CodeReviewState):
#         evals = state.get("checklist_evaluation") or {}
#         if state.get("force_done", False):
#             return True
#         return any(evals.values())

#     builder.add_conditional_edges(
#         "run_feature_implementation_check",  # 현재 노드
#         should_review,                       # 분기 함수
#         {
#             True: "apply_checklist_evaluation",
#             False: END
#         }
#     )

#     builder.add_edge("apply_checklist_evaluation", "run_code_review_file_fetch")
#     builder.add_edge("run_code_review_file_fetch", "run_feature_code_review")

#     # 리뷰 끝난 후 → 요약할지 말지
#     def should_run_code_review_summary(state: CodeReviewState) -> bool:
#         checklist = state.get("checklist", [])
#         if state.get("force_done", False):
#             return True
#         return all(item.get("done", False) for item in checklist)

#     builder.add_conditional_edges(
#         "run_feature_code_review",
#         should_run_code_review_summary,
#         {
#             True: "run_code_review_summary",
#             False: END
#         }
#     )

#     builder.set_finish_point("run_code_review_summary")

#     return builder.compile()


# # from langgraph.graph import StateGraph, END
# # from src.code_review.state import CodeReviewState
# # from .graph.nodes import (
# #     run_checklist_fetch,
# #     run_feature_implementation_check,
# #     apply_checklist_evaluation,
# #     run_feature_code_review,
# #     run_code_review_file_fetch,
# #     run_code_review_summary,
# #     send_result_to_java,
    
# # )


# # def create_feature_graph():
# #     builder = StateGraph(CodeReviewState)

# #     builder.add_node("run_checklist_fetch", run_checklist_fetch)
# #     builder.add_node("run_feature_implementation_check", run_feature_implementation_check)
# #     builder.add_node("apply_checklist_evaluation", apply_checklist_evaluation)
# #     builder.add_node("run_code_review_file_fetch", run_code_review_file_fetch)
# #     builder.add_node("run_feature_code_review", run_feature_code_review)
# #     builder.add_node("run_code_review_summary", run_code_review_summary)
# #     builder.add_node("send_result_to_java", send_result_to_java)

# #     # 시작 노드
# #     builder.set_entry_point("run_checklist_fetch")
# #     builder.add_edge("run_checklist_fetch", "run_feature_implementation_check")

# #     # checklist 평가 → 분기
# #     def should_review(state: CodeReviewState):
# #         evals = state.checklist_evaluation or {}
# #         if state.get("force_done", False):
# #             return True
# #         return any(evals.values())

# #     builder.add_conditional_edges(
# #         "run_feature_implementation_check",
# #         condition=should_review,
# #         path_map={
# #             True: "apply_checklist_evaluation",
# #             False: "send_result_to_java"
# #         }
# #     )

# #     builder.add_edge("apply_checklist_evaluation", "run_code_review_file_fetch")
# #     builder.add_edge("run_code_review_file_fetch", "run_feature_code_review")

# #     # 리뷰 끝난 후 → 요약할지 말지
# #     def should_run_code_review_summary(state: CodeReviewState) -> bool:
# #         checklist = state.get("checklist", [])
# #         if state.get("force_done", False):
# #             return True
# #         return all(item.get("done", False) for item in checklist)

# #     builder.add_conditional_edges(
# #         "run_feature_code_review",
# #         condition=should_run_code_review_summary,
# #         path_map={
# #             True: "run_code_review_summary",
# #             False: "send_result_to_java"
# #         }
# #     )

# #     builder.set_finish_point("run_code_review_summary")

# #     return builder.compile()
