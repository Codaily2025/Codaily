from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain.chat_models import init_chat_model
from langchain_core.messages import BaseMessage
from typing import List
from enum import Enum


class ChatIntent(str, Enum):
    CHAT = "chat"
    SPEC = "spec"
    SPEC_REGENERATE = "spec:regenerate"


# 프롬프트: intent 분류
from langchain_core.prompts import ChatPromptTemplate

prompt = ChatPromptTemplate.from_messages([
    ("system",
        "당신은 사용자와 챗봇의 대화를 분석하여, 현재 어떤 동작을 수행하려는지 판단하는 시스템입니다.\n\n"

        "아래 대화 내역을 기반으로 다음 세 가지 중 하나를 정확히 판단하여 소문자로 출력하세요:\n\n"
        "- 'chat': 아직 기능 명세서를 만들기에는 정보가 부족하거나, 주제와 무관한 일반적인 대화를 주고받고 있는 상태입니다.\n"
        "- 'spec': 사용자가 만들고자 하는 서비스 또는 기능에 대해 충분한 정보를 제공했고, 그 정보가 서비스의 목적과 직접적으로 관련되어 있어 명세서를 생성할 수 있는 상태입니다.\n"
        "- 'spec:regenerate': 사용자가 이미 생성된 명세서를 새로 만들어달라고 명시적으로 요청한 상태입니다. (예: '다시 만들어줘', '명세서 다시 생성해줘')\n\n"

        "다음 조건 중 **2개 이상을 충족**할 경우에만 'spec'으로 판단하세요:\n"
        "1. **사용자**가 만들고자 하는 서비스의 목적이나 해결하고자 하는 문제를 구체적으로 설명했을 때\n"
        "2. '로그인 기능', '프로필 수정' 등 **핵심 기능명**을 명확히 언급했을 때\n"
        "3. 사용 시나리오나 흐름(예: '이메일을 입력하면 인증 링크가 전송됨')을 설명했을 때\n"
        "4. 입력값과 출력값(예: '사용자는 이름을 입력하고, 결과로 요약된 정보를 받음')을 언급했을 때\n"
        "5. 사용자가 '요구사항 정리해줘', '명세서 만들어줘' 등 명세 생성 의사를 명시했을 때\n\n"
        "6. 챗봇이 '이제 명세서를 만들어드릴게요', '다음은 명세입니다'처럼 **명세 생성 의사를 먼저 밝힌 경우**\n\n"
        "사용자가 명세서를 생성하길 요청했더라도, **프로젝트 주제/목적/기능/시나리오 중 하나 이상이 명확하지 않으면 spec이 아닌 chat**으로 판단할 것.\n\n"

        "단, 다음의 경우는 반드시 'chat'으로 판단해야 합니다:\n"
        "- 위 조건이 거의 충족되지 않은 상태 (단순한 아이디어, 막연한 주제 탐색)\n"
        "- 사용자가 언급한 기능이 서비스의 핵심 목적과 관련 없는 **부가 기능**일 경우\n"
        "  (예: 다크모드, 테마 설정, 지도 확대, 택시 호출 등 핵심 흐름과 연결되지 않은 기능)\n\n"

        "출력은 반드시 다음 중 하나로만 하세요 (따옴표 없이):\n"
        "chat\n"
        "spec\n"
        "spec:regenerate"
    ),
    ("user", "대화 내역:\n\n{chat_history}")
])


model = init_chat_model(
    model="gpt-4.1-nano",
    model_provider="openai"
)
parser = StrOutputParser()
intent_chain = prompt | model | parser


def classify_chat_intent(message: str) -> str:
    """
    단일 사용자 message(str) 기반으로 chat/spec/spec:regenerate 중 하나를 판단합니다.
    """
    result = intent_chain.invoke({"chat_history": message})
    normalized = result.strip().lower()

    print(f"[Intent 판단] message: {message} → intent: {normalized}")
    
    if normalized in ["chat", "spec", "spec:regenerate"]:
        return normalized
    else:
        raise ValueError(f"[Chat Intent 판단 오류] 예상치 못한 출력: {result}")


# def classify_chat_intent(messages: List[BaseMessage]) -> str:
#     # 역할에 따라 포맷팅
#     formatted_history = ""
#     for m in messages:
#         role = "사용자" if m.type == "human" else "AI"
#         formatted_history += f"{role}: {m.content}\n"

#     result = intent_chain.invoke({"chat_history": formatted_history})
#     normalized = result.strip().lower()

#     print(normalized)
#     if normalized in ["chat", "spec", "spec:regenerate"]:
#         return normalized
#     else:
#         raise ValueError(f"[Chat Intent 판단 오류] 예상치 못한 출력: {result}")



# # 프롬프트 정의 (명확하게 제한)
# prompt = ChatPromptTemplate.from_messages([
#     ("system",
#         "당신은 기능 명세서를 생성할 준비가 되었는지를 판단하는 AI입니다.\n\n"
#         "다음 중 **하나라도 정확히 충족할 경우에만** 'true'라고 답하세요:\n\n"
#         "1. 사용자가 만들고자 하는 기능이나 서비스의 **목적을 명확하고 구체적으로 설명**했을 때\n"
#         "2. 사용자가 '로그인 기능을 만들고 싶다', '프로필 수정 기능을 추가하고 싶다' 등 **구체적인 기능명을 직접 언급**했을 때\n"
#         "3. 사용자가 기능을 어떻게 사용할지에 대해 명확하게 **사용 시나리오나 흐름**을 설명했을 때\n"
#         "4. 사용자가 '요구사항 정리해줘', '명세서 생성해줘', '기능 정리해줘' 등 **명시적 요청**을 했을 때\n"
#         "5. 챗봇이 '이제 명세서를 만들어드릴게요', '다음은 명세입니다'처럼 **명세 생성 의사를 먼저 밝힌 경우**\n\n"
#         "아래와 같은 경우는 반드시 'false'라고 답하세요:\n"
#         "- 단순히 '프로젝트 하고 싶어', '뭐 만들면 좋을까?'처럼 **아이디어 탐색 단계**일 때\n"
#         "- 사용자가 기능을 언급하지 않고, 챗봇도 기능을 요약하지 않은 경우\n"
#         "- 챗봇이 단지 예시나 아이디어만 나열한 경우\n\n"
#         "출력은 반드시 소문자 'true' 또는 'false'만 출력하세요."
#     ),
#     ("user", "대화 내역:\n\n{chat_history}")
# ])

# model = init_chat_model(
#     model="gpt-4.1-nano",
#     model_provider="openai"
# )
# parser = StrOutputParser()
# readiness_chain = prompt | model | parser

# def is_ready_to_generate_spec(messages: List[BaseMessage]) -> bool:
#     # 역할에 따라 구분된 메시지 리스트를 정리
#     formatted_history = ""
#     for m in messages:
#         role = "사용자" if m.type == "human" else "AI"
#         formatted_history += f"{role}: {m.content}\n"

#     result = readiness_chain.invoke({"chat_history": formatted_history})
#     normalized = result.strip().lower()

#     if normalized == "true":
#         return True
#     elif normalized == "false":
#         return False
#     else:
#         raise ValueError(f"[명세 판단 오류] 예상치 못한 출력: {result}")
