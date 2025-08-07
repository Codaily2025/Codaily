import time
import json
from pydantic import BaseModel
from typing import AsyncIterable
from fastapi import APIRouter, Request
from fastapi.responses import StreamingResponse
from langchain.chat_models import init_chat_model
from langchain_community.chat_message_histories import ChatMessageHistory
from src.specification.specification_router import stream_function_specification
from src.specification.specification_service import *
from src.specification.chat_service import *

router = APIRouter()

model = init_chat_model(
    model="gpt-4.1-nano",
    model_provider="openai",
    streaming=True
)


async def monitor_sse_stream(stream: AsyncIterable[dict], wrapper_type: str = "spec"):
    """
    SSE를 감싸면서 시간 측정 및 sub_function 개수를 로그로 출력
    """
    start_time = time.perf_counter()
    first_chunk_sent = False
    total_sub_functions = 0

    async for chunk in stream:
        yield f"data: {json.dumps(chunk, ensure_ascii=False)}\n\n"

        if not first_chunk_sent:
            elapsed_first = time.perf_counter() - start_time
            print(f"[{wrapper_type}] 첫 SSE 조각 전송까지: {elapsed_first:.2f}초")
            first_chunk_sent = True

        # sub_function 개수 누적
        try:
            sub_funcs = chunk.get("content", {}).get("sub_feature", [])
            total_sub_functions += len(sub_funcs)
        except Exception as e:
            print(f"[{wrapper_type}] sub_feature 파싱 실패 (무시됨): {e}")

    elapsed_total = time.perf_counter() - start_time
    print(f"[{wrapper_type}] 전체 SSE 완료 시간: {elapsed_total:.2f}초")
    print(f"[{wrapper_type}] 총 상세 기능 개수: {total_sub_functions}개")


def format_history_to_text(messages):
    lines = []
    for msg in messages:
        if msg.type == "human":
            lines.append(f"User: {msg.content}")
        elif msg.type == "ai":
            lines.append(f"AI: {msg.content}")
    return "\n".join(lines)


class FeatureRef(BaseModel):
    id: int
    title: str
    field: str

class MessageRequest(BaseModel):
    message: str
    mainFeatures: List[FeatureRef] = []


@router.post("/intent")
async def determine_intent(req: MessageRequest):
    intent, feature_id, field = classify_chat_intent(req.message, req.mainFeatures)
    if intent not in {"chat", "spec", "spec:regenerate", "spec:add:feature:main", "spec:add:feature:sub", "spec:add:field"}:
        raise ValueError(f"[intent 판단 실패] 예외 출력: {intent}")
    
    return {
        "intent": intent,
        "featureId": feature_id,
        "field": field
    }


# 간단한 메모리
user_histories = {}

@router.get("/gpt/stream")
async def chat_stream(
    intent: str,
    user_id: str,
    message: str,
    feature_id: int = None,
    title: str = None,
    field: str = None
):
    history = user_histories.setdefault(user_id, ChatMessageHistory())
    history.add_user_message(message)

    if intent in [ChatIntent.SPEC, ChatIntent.SPEC_REGENERATE]:
        formatted_text = format_history_to_text(history.messages)

        async def spec_event_generator():
            async for line in monitor_sse_stream(stream_function_specification(formatted_text), wrapper_type=intent):
                yield line
        return StreamingResponse(spec_event_generator(), media_type="text/event-stream")

    elif intent == ChatIntent.CHAT:
        async def chat_event_generator():
            collected = []
            async for chunk in model.astream(history.messages):
                collected.append(chunk.content)
                wrapper = {
                    "type": intent,
                    "content": chunk.content
                }
                yield f"data: {json.dumps(wrapper, ensure_ascii=False)}\n\n"
            history.add_ai_message("".join(collected))
        return StreamingResponse(chat_event_generator(), media_type="text/event-stream")

    elif intent == ChatIntent.SPEC_ADD_FEATURE_MAIN:
        if not field:
            raise ValueError("field is required for adding a main feature.")

        def main_feature_generator():
            # 주 기능 1개 생성 (title + description)
            # print(message)
            main_feature = generate_main_feature_from_message(
                message_text=message,
                field=field
            )
            main_func_full = f"{main_feature['title']}. {main_feature['description']}"
            # print(main_func_full)
            # 상세 기능들 생성
            sub_features = generate_sub_functions(
                project_description=message,
                function_group=field,
                main_function=main_func_full
            )

            wrapper = {
                "type": intent,
                "content": {
                    "field": field,
                    "main_feature": main_feature,
                    "sub_feature": sub_features
                }
            }
            # print(wrapper)
            yield f"data: {json.dumps(wrapper, ensure_ascii=False)}\n\n"
            history.add_ai_message(f"주 기능이 생성되었습니다: {main_feature['title']}")

        return StreamingResponse(main_feature_generator(), media_type="text/event-stream")


    elif intent == ChatIntent.SPEC_ADD_FEATURE_SUB:
        if feature_id is None or not title or not field:
            raise ValueError("feature_id, title, and field are required for adding a sub feature.")

        async def sub_feature_generator():
            feature = await generate_sub_feature(
                history=history.messages,
                title=title,
                field=field
            )
            wrapper = {
                "type": intent,
                "content": {
                    "featureId": feature_id,
                    "field": field,
                    "subFeature": feature
                }
            }
            # print(wrapper)
            yield f"data: {json.dumps(wrapper, ensure_ascii=False)}\n\n"
            history.add_ai_message(f"상세 기능이 생성되었습니다: {feature['title']}")

        return StreamingResponse(sub_feature_generator(), media_type="text/event-stream")
    
    elif intent == ChatIntent.SPEC_ADD_FIELD:
        if not message:
            raise ValueError("message is required to add a new group.")

        async def add_group_generator():
            project_description = message
            group_title = await asyncio.to_thread(generate_group_title, project_description)

            # 그룹 하나에 대한 기능 흐름을 stream 형식으로 순차 반환
            async for item in stream_single_group(project_description, group_title):
                wrapped = {
                    "type": intent,
                    "content": item
                }
                # print(wrapped)
                yield f"data: {json.dumps(wrapped, ensure_ascii=False)}\n\n"


            # 사용자 이력에 메시지 추가
            history.add_ai_message(f"새로운 기능 그룹 '{group_title}'이 생성되었습니다.")

        return StreamingResponse(add_group_generator(), media_type="text/event-stream")

    else:
        raise ValueError(f"[chat_stream] 알 수 없는 intent: {intent}")
