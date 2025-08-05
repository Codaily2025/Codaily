from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain.chat_models import init_chat_model
from langchain_core.messages import BaseMessage
from typing import List

# 프롬프트 정의 (명확하게 제한)
prompt = ChatPromptTemplate.from_messages([
    ("system",
        "당신은 기능 명세서를 생성할 준비가 되었는지를 판단하는 AI입니다.\n\n"
        "다음 중 **하나라도 정확히 충족할 경우에만** 'true'라고 답하세요:\n\n"
        "1. 사용자가 만들고자 하는 기능이나 서비스의 **목적을 명확하고 구체적으로 설명**했을 때\n"
        "2. 사용자가 '로그인 기능을 만들고 싶다', '프로필 수정 기능을 추가하고 싶다' 등 **구체적인 기능명을 직접 언급**했을 때\n"
        "3. 사용자가 기능을 어떻게 사용할지에 대해 명확하게 **사용 시나리오나 흐름**을 설명했을 때\n"
        "4. 사용자가 '요구사항 정리해줘', '명세서 생성해줘', '기능 정리해줘' 등 **명시적 요청**을 했을 때\n"
        "5. 챗봇이 '이제 명세서를 만들어드릴게요', '다음은 명세입니다'처럼 **명세 생성 의사를 먼저 밝힌 경우**\n\n"
        "아래와 같은 경우는 반드시 'false'라고 답하세요:\n"
        "- 단순히 '프로젝트 하고 싶어', '뭐 만들면 좋을까?'처럼 **아이디어 탐색 단계**일 때\n"
        "- 사용자가 기능을 언급하지 않고, 챗봇도 기능을 요약하지 않은 경우\n"
        "- 챗봇이 단지 예시나 아이디어만 나열한 경우\n\n"
        "출력은 반드시 소문자 'true' 또는 'false'만 출력하세요."
    ),
    ("user", "대화 내역:\n\n{chat_history}")
])

model = init_chat_model(
    model="gpt-4.1-nano",
    model_provider="openai"
)
parser = StrOutputParser()
readiness_chain = prompt | model | parser

def is_ready_to_generate_spec(messages: List[BaseMessage]) -> bool:
    # 역할에 따라 구분된 메시지 리스트를 정리
    formatted_history = ""
    for m in messages:
        role = "사용자" if m.type == "human" else "AI"
        formatted_history += f"{role}: {m.content}\n"

    result = readiness_chain.invoke({"chat_history": formatted_history})
    normalized = result.strip().lower()

    if normalized == "true":
        return True
    elif normalized == "false":
        return False
    else:
        raise ValueError(f"[명세 판단 오류] 예상치 못한 출력: {result}")
