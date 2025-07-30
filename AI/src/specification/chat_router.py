from fastapi import APIRouter, Request
from fastapi.responses import StreamingResponse
from langchain_core.messages import HumanMessage
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

@router.get("/gpt/stream")
async def chat_stream(request: Request, user_id: str, message: str):
    # 대화 기록 가져오기
    history = user_histories.setdefault(user_id, ChatMessageHistory())
    history.add_user_message(message)

    # 현재까지의 전체 메시지로 판단
    is_ready = is_ready_to_generate_spec(history.messages)

    if is_ready:
        async def spec_event_generator():
            # 명세서 생성만 수행
            async for chunk in stream_function_specification(message):
                yield f"data: {chunk}\n\n"
        return StreamingResponse(spec_event_generator(), media_type="text/event-stream")

    else:
        async def chat_event_generator():
            collected = []
            async for chunk in model.astream(history.messages):
                collected.append(chunk.content)
                # print(chunk.content)
                yield f"data: {chunk.content}\n\n"
            full_response = "".join(collected)
            history.add_ai_message(full_response)

        return StreamingResponse(chat_event_generator(), media_type="text/event-stream")
