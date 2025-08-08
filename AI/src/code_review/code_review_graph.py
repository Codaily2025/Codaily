from langgraph.graph import StateGraph, END
from .state import CodeReviewState
from .graph.nodes import (
    run_feature_inference,
    run_parallel_feature_graphs,
    send_result_to_java,
)

# 메인 그래프 빌더 생성
builder = StateGraph(CodeReviewState)

# 노드 등록
builder.add_node("run_feature_inference", run_feature_inference)
builder.add_node("run_parallel_feature_graphs", run_parallel_feature_graphs)
builder.add_node("send_result_to_java", send_result_to_java)

# 시작점 설정
builder.set_entry_point("run_feature_inference")

# 조건 함수: 다음 노드 이름을 직접 반환
def should_continue(state: CodeReviewState) -> str:
    if state.get("feature_names"):
        return "run_parallel_feature_graphs"
    else:
        return END

# 조건 분기 연결
builder.add_conditional_edges(
    "run_feature_inference",
    should_continue,
    {
        "run_parallel_feature_graphs": "run_parallel_feature_graphs",
        END: END
    }
)

# 병렬 실행 후 자바로 결과 전송
builder.add_edge("run_parallel_feature_graphs", "send_result_to_java")

# 종료점 설정
builder.set_finish_point("send_result_to_java")

# 그래프 컴파일
code_review_graph = builder.compile()
