import json
from fastapi import APIRouter, Request
from fastapi.responses import StreamingResponse
from langchain.chat_models import init_chat_model
from langchain_community.chat_message_histories import ChatMessageHistory
from src.specification.specification_router import stream_function_specification
from src.specification.chat_service import is_ready_to_generate_spec

router = APIRouter()

model = init_chat_model(
    model="gpt-4.1-nano",
    model_provider="openai",
    streaming=True
)

# 간단한 메모리
user_histories = {}


import time
import json
from typing import AsyncIterable

async def monitor_sse_stream(stream: AsyncIterable[dict], wrapper_type: str = "spec"):
    """
    SSE를 감싸면서 시간 측정 및 sub_function 개수를 로그로 출력
    """
    start_time = time.perf_counter()
    first_chunk_sent = False
    total_sub_functions = 0

    async for chunk in stream:
        wrapper = {
            "type": wrapper_type,
            "content": chunk
        }
        yield f"data: {json.dumps(wrapper, ensure_ascii=False)}\n\n"

        if not first_chunk_sent:
            elapsed_first = time.perf_counter() - start_time
            print(f"[{wrapper_type}] 첫 SSE 조각 전송까지: {elapsed_first:.2f}초")
            first_chunk_sent = True

        # sub_function 개수 누적
        try:
            sub_funcs = chunk.get("sub_functions", [])
            total_sub_functions += len(sub_funcs)
        except Exception as e:
            print(f"[{wrapper_type}] sub_function 파싱 실패 (무시됨): {e}")

    elapsed_total = time.perf_counter() - start_time
    print(f"[{wrapper_type}] 전체 SSE 완료 시간: {elapsed_total:.2f}초")
    print(f"[{wrapper_type}] 총 상세 기능 개수: {total_sub_functions}개")


@router.get("/gpt/stream")
async def chat_stream(request: Request, user_id: str, message: str):
    history = user_histories.setdefault(user_id, ChatMessageHistory())
    history.add_user_message(message)

    is_ready = is_ready_to_generate_spec(history.messages)

    if is_ready:
        async def spec_event_generator():
            # stream_function_specification()은 async generator를 리턴한다고 가정
            async for line in monitor_sse_stream(stream_function_specification(message), wrapper_type="spec"):
                yield line

        return StreamingResponse(spec_event_generator(), media_type="text/event-stream")

    else:
        async def chat_event_generator():
            collected = []
            async for chunk in model.astream(history.messages):
                collected.append(chunk.content)
                wrapper = {
                    "type": "chat",
                    "content": chunk.content
                }
                yield f"data: {json.dumps(wrapper, ensure_ascii=False)}\n\n"
            history.add_ai_message("".join(collected))

        return StreamingResponse(chat_event_generator(), media_type="text/event-stream")

