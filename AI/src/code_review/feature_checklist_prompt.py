from langchain.prompts import ChatPromptTemplate

feature_checklist_prompt = ChatPromptTemplate.from_messages([
    (
        "system",
        "당신은 기능을 구현하는데 필요한 표준 구현 목록을 정리해주는 소프트웨어 기획 전문가입니다."
    ),
    (
        "human",
        """
        다음은 사용자의 기능 명세 목록입니다. 각 기능마다 featureId, 제목, 설명이 주어집니다.

        당신의 역할은 **각 기능이 실제로 동작하는 데 필요한 표준 구현 항목만** 추려서 체크리스트 형태로 제공하는 것입니다.

        📌 반드시 지켜야 할 조건:
        - **해당 기능이 작동하려면 꼭 필요한 핵심 구현 요소만 포함**하세요.
        - 다음 항목은 **절대 포함하지 마세요**:
            - 버튼, 입력 필드, UI 구성 요소
            - 유효성 검사, 포맷 검사
            - 비밀번호 길이 조건, 정규식 검사
            - 예외 처리
            - 보안 강화 요소
            - 사용자 경험/편의성
        - 항목은 **짧고 구체적으로** 작성하세요.
        - 최소 2개 최대 5개 항목까지만 작성하세요.
        - 응답은 반드시 JSON 형식으로 출력하세요.

        📌 응답 예시:
            ```json
            {{
            "101": [
                "OAuth 인증 URL로 리다이렉트",
                "Authorization code로 access token 요청",
                "사용자 정보로 JWT 발급"
            ]
            }}


        아래는 기능 목록입니다:
        {feature_list}
        """
    )
])
