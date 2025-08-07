from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser
from langchain.chat_models import init_chat_model
from langchain_core.messages import BaseMessage
from typing import List
from enum import Enum


class ChatIntent(str, Enum):
    CHAT = "chat"
    SPEC = "spec"
    SPEC_REGENERATE = "spec:regenerate"
    SPEC_ADD_FEATURE_MAIN = "spec:add:feature:main"
    SPEC_ADD_FEATURE_SUB = "spec:add:feature:sub"
    SPEC_ADD_FIELD = "spec:add:field"


# 프롬프트: intent 분류
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser

parser = JsonOutputParser()

from langchain.prompts import ChatPromptTemplate

prompt = ChatPromptTemplate.from_messages([
    ("system",
        "당신은 사용자와 챗봇의 대화를 분석하여, 현재 어떤 동작을 수행하려는지 판단하는 시스템입니다.\n\n"
        "출력은 반드시 다음 JSON 형식으로 반환하세요 (추가 설명 없이 JSON만 출력):\n"
        "{{\n"
        "  \"intent\": \"chat | spec | spec:regenerate | spec:add:feature:main | spec:add:feature:sub | spec:add:field\",\n"
        "  \"featureId\": number 또는 null,\n"
        "  \"field\": string 또는 null\n"
        "}}\n\n"

        "출력 필드 설명:\n"
        "- intent: 사용자의 현재 요청 목적을 나타냅니다.\n"
        "- featureId: intent가 'spec:add:feature:sub'일 경우, 해당 상세 기능이 속할 주 기능의 ID를 지정합니다. 그 외에는 null로 설정하세요.\n"
        "- field:\n"
        "    - intent가 'spec:add:feature:main' 또는 'spec:add:feature:sub'일 경우, 해당 기능이 속한 기능 그룹명을 지정합니다.\n"
        "    - intent가 'spec:add:field'이거나 그 외의 intent일 경우, 반드시 null로 설정하세요.\n\n"

        "판단 가능한 intent 목록과 의미는 다음과 같습니다:\n"
        "- 'chat': 아직 기능 명세서를 만들기에는 정보가 부족하거나, 주제와 무관한 일반적인 대화\n"
        "- 'spec': 사용자가 만들고자 하는 서비스 또는 기능에 대해 충분한 정보를 제공한 경우 (명세서 생성 가능)\n"
        "- 'spec:regenerate': 사용자가 기존 명세서를 새로 만들어달라고 요청한 경우\n"
        "- 'spec:add:feature:main': 기존 주 기능과 무관한 **새로운 기능** 추가 요청\n"
        "- 'spec:add:feature:sub': 기존 주 기능에 관련된 **상세 기능** 추가 요청\n"
        "- 'spec:add:field': 기존 기능 그룹에 포함되지 않는 **완전히 새로운 주제/카테고리의 기능** 요청\n\n"

        "다음 조건 중 **2개 이상을 충족**할 경우에만 'spec'으로 판단하세요:\n"
        "1. 서비스 목적이나 해결하고자 하는 문제 설명\n"
        "2. 명확한 핵심 기능명 ('로그인', '알림 설정' 등)\n"
        "3. 사용 흐름/시나리오 ('이메일을 입력하면 인증 링크 전송')\n"
        "4. 입력값과 출력값의 관계 ('입력하면 요약된 정보 반환')\n"
        "5. 명세 생성 의사 표현 ('명세서 만들어줘')\n"
        "6. 챗봇이 명세 생성 안내를 먼저 한 경우\n\n"

        "다음의 경우는 반드시 'chat'으로 판단하세요:\n"
        "- 위 조건이 거의 충족되지 않음 (아이디어 탐색 등)\n"
        "- 기능이 핵심 목적과 무관한 부가 기능일 경우 (예: 다크모드, 테마 변경 등)\n\n"

        "다음의 경우는 반드시 'spec:add:feature:main'으로 판단하세요:\n"
        "- 사용자가 새로운 기능 하나를 명확히 요청했고, 그것이 기존 주 기능들과 관련 없음\n\n"

        "다음의 경우는 반드시 'spec:add:feature:sub'으로 판단하세요:\n"
        "- 사용자가 기존 주 기능에 연결될 수 있는 상세 기능을 요청한 경우\n"
        "- 이때 반환값에 어떤 주 기능의 하위 기능인지 식별할 수 있도록 해당 주 기능의 'id'를 featureId로 설정하세요\n"
        "- 또한, 해당 주 기능이 속한 기능 그룹명을 field에 지정하세요.\n\n"

        "다음의 경우는 반드시 'spec:add:field'으로 판단하세요:\n"
        "- 사용자가 기존의 모든 기능 그룹에 포함되지 않는 **새로운 주제나 카테고리**의 기능을 제안한 경우\n"
        "- 예: '개인화 추천 시스템을 추가하고 싶어요'인데 기존 그룹은 전부 '캘린더', '태스크 관리' 등이라면 새로운 그룹 필요\n"
        "- 이 intent는 'spec:add:feature:main'보다 우선적으로 판단되어야 합니다.\n"
        "- 이 경우 field는 반드시 null로 설정하세요.\n\n"

        "출력 예시:\n"
        "- {{\"intent\": \"chat\", \"featureId\": null, \"field\": null}}\n"
        "- {{\"intent\": \"spec:regenerate\", \"featureId\": null, \"field\": null}}\n"
        "- {{\"intent\": \"spec:add:feature:main\", \"featureId\": null, \"field\": \"알림 설정\"}}\n"
        "- {{\"intent\": \"spec:add:feature:sub\", \"featureId\": 2, \"field\": \"일정 관리\"}}\n"
        "- {{\"intent\": \"spec:add:field\", \"featureId\": null, \"field\": null}}\n\n"

        "현재 주 기능 목록은 다음과 같은 형식으로 제공됩니다:\n"
        "- id: 1, title: \"작업 생성\", field: \"프로젝트 관리\"\n"
        "- id: 2, title: \"캘린더 보기\", field: \"일정 관리\"\n"
        "- ...\n\n"

        "- 각 항목은 하나의 주 기능을 나타내며, 다음 정보를 포함합니다:\n"
        "- title: 주 기능의 이름\n"
        "- field: 이 주 기능이 속한 상위 기능 그룹 이름\n\n"

        "※ 중요: JSON 외 다른 문장이나 설명을 절대 포함하지 마세요."
    ),
    ("user", "대화 내역:\n{chat_history}\n\n현재 주 기능 목록:\n{main_features}")
])


model = init_chat_model(
    model="gpt-4.1-nano",
    model_provider="openai"
)
parser = JsonOutputParser()
intent_chain = prompt | model | parser


def classify_chat_intent(message: str, main_features: list[dict]) -> tuple[str, int | None]:
    """
    LangChain을 통해 intent와 featureId를 추론함.
    """
    print(main_features)
    main_features_str = "\n".join(
    f"- id: {f.id}, title: {f.title}, field: {f.field}" for f in main_features) if main_features else "없음"

    result = intent_chain.invoke({
        "chat_history": message,
        "main_features": main_features
    })
    
    print(f"[Intent 판단] message: {message} → {result}")

    intent = result.get("intent")
    feature_id = result.get("featureId")
    field = result.get("field")

    valid = {
        "chat",
        "spec",
        "spec:regenerate",
        "spec:add:feature:main",
        "spec:add:feature:sub",
        "spec:add:field"
    }

    if intent not in valid:
        raise ValueError(f"[Chat Intent 판단 오류] 예상치 못한 intent: {intent}")
    print("intent result: " + str(intent) + " " + str(feature_id) + " " + str(field))
    return intent, feature_id, field


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
