import time
import json
from typing import List
from enum import Enum
from pydantic import BaseModel
from typing import AsyncIterable, AsyncGenerator
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser
from langchain.chat_models import init_chat_model
from langchain_core.messages import BaseMessage
from langchain_community.chat_message_histories import ChatMessageHistory

# from src.specification.specification_router import stream_function_specification
from src.specification.specification_service import *
from src.specification.specification_prompts import CHAT_INTENT_PROMPT


class ChatIntent(str, Enum):
    CHAT = "chat"
    SPEC = "spec"
    SPEC_REGENERATE = "spec:regenerate"
    SPEC_ADD_FEATURE_MAIN = "spec:add:feature:main"
    SPEC_ADD_FEATURE_SUB = "spec:add:feature:sub"
    SPEC_ADD_FIELD = "spec:add:field"


# 프롬프트: intent 분류

model = init_chat_model(model="gpt-4.1-nano", model_provider="openai")
parser = JsonOutputParser()


def format_history_to_text(messages):
    lines = []
    for msg in messages:
        if msg.type == "human":
            lines.append(f"User: {msg.content}")
        elif msg.type == "ai":
            lines.append(f"AI: {msg.content}")
    return "\n".join(lines)


async def classify_chat_intent(
    message: str, main_features: list[dict]
) -> tuple[ChatIntent, int | None]:
    """
    LangChain을 통해 intent와 featureId를 추론함.
    """
    # print(main_features)
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", CHAT_INTENT_PROMPT),
            (
                "user",
                "대화 내역:\n{chat_history}\n\n현재 주 기능 목록:\n{main_features}",
            ),
        ]
    )

    intent_chain = prompt | model | parser

    main_features_str = (
        "\n".join(
            f"- id: {f.id}, title: {f.title}, field: {f.field}" for f in main_features
        )
        if main_features
        else "없음"
    )

    result = await intent_chain.ainvoke(
        {"chat_history": message, "main_features": main_features_str}
    )

    print(f"[Intent 판단] message: {message} → {result}")

    intent = result.get("intent")
    feature_id = result.get("featureId")
    field = result.get("field")

    try:
        intent_enum = ChatIntent(intent)
    except ValueError:
        raise ValueError(f"[Chat Intent 판단 오류] 예상치 못한 intent: {intent}")

    # print(f"intent result: {intent_enum} {feature_id} {field}")
    return intent, feature_id, field


async def monitor_sse_stream(stream: AsyncIterable[dict], wrapper_type: ChatIntent):
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
            print(f"[{wrapper_type.value}] 첫 SSE 조각 전송까지: {elapsed_first:.2f}초")
            first_chunk_sent = True

        # sub_function 개수 누적
        try:
            sub_funcs = chunk.get("content", {}).get("sub_feature", [])
            total_sub_functions += len(sub_funcs)
        except Exception as e:
            print(f"[{wrapper_type.value}] sub_feature 파싱 실패 (무시됨): {e}")

    elapsed_total = time.perf_counter() - start_time
    print(f"[{wrapper_type.value}] 전체 SSE 완료 시간: {elapsed_total:.2f}초")
    print(f"[{wrapper_type.value}] 총 상세 기능 개수: {total_sub_functions}개")


async def gen_chat(
    history: ChatMessageHistory,  # 유저별 히스토리
    intent: ChatIntent,  # Enum
) -> AsyncGenerator[str, None]:
    collected: list[str] = []
    try:
        async for chunk in model.astream(history.messages):
            # chunk.content가 None인 경우 방어
            piece = getattr(chunk, "content", "") or ""
            collected.append(piece)

            wrapper = {
                "type": intent.value,  # 클라이언트엔 문자열
                "content": piece,
            }
            yield f"data: {json.dumps(wrapper, ensure_ascii=False)}\n\n"
    finally:
        # 스트림이 중간에 끊겨도 지금까지 생성한 내용을 기록
        if collected:
            history.add_ai_message("".join(collected))


async def gen_spec(formatted_text: str, wrapper_type: ChatIntent):
    async for line in monitor_sse_stream(
        stream_function_specification(formatted_text), wrapper_type=wrapper_type
    ):
        yield line


async def add_field(
    intent: ChatIntent,
    project_description: str,
    history: ChatMessageHistory,
):
    field_title = await generate_field_from_message(project_description)
    # print(group_title)
    # 그룹 하나에 대한 기능 흐름을 stream 형식으로 순차 반환
    try:
        async for item in stream_single_field(project_description, field_title):
            wrapped = {"type": intent.value, "content": item}
            # print(wrapped)
            yield f"data: {json.dumps(wrapped, ensure_ascii=False)}\n\n"
    finally:
        history.add_ai_message(f"새로운 기능 그룹 '{field_title}'이 생성되었습니다.")


async def add_main_feature(
    history: ChatMessageHistory,
    intent: ChatIntent,
    message: str,
    field: str,
):
    # 주 기능 1개 생성 (title + description)
    # print(message)
    main_feature = await generate_main_feature_from_message(
        message_text=message, field=field
    )
    main_func_full = f"{main_feature['title']}. {main_feature['description']}"
    # print(main_func_full)
    # 상세 기능들 생성
    sub_features = await spec_sub_functions_generator(
        project_description=message,
        function_group=field,
        main_function=main_func_full,
    )

    wrapper = {
        "type": intent,
        "content": {
            "field": field,
            "main_feature": main_feature,
            "sub_feature": sub_features,
        },
    }
    yield f"data: {json.dumps(wrapper, ensure_ascii=False)}\n\n"
    history.add_ai_message(f"주 기능이 생성되었습니다: {main_feature['title']}")


async def add_sub_feature(
    history: ChatMessageHistory,
    intent: ChatIntent,
    title: str,
    field: str,
    feature_id: int,
):
    feature = await stream_sub_feature(
        history=history.messages, title=title, field=field
    )
    wrapper = {
        "type": intent.value,
        "content": {
            "featureId": feature_id,
            "field": field,
            "subFeature": feature,
        },
    }
    # print(wrapper)
    yield f"data: {json.dumps(wrapper, ensure_ascii=False)}\n\n"
    history.add_ai_message(f"상세 기능이 생성되었습니다: {feature['title']}")
