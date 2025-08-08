from langchain_core.prompts import ChatPromptTemplate

feature_inference_prompt = ChatPromptTemplate.from_messages([
    ("system", 
     """
     당신은 소프트웨어 구조 분석 전문가입니다.
     아래에 주어진 코드 변경(diff)을 바탕으로 사용자가 구현한 기능명을 유추하세요.

     📌 반드시 지켜야 할 조건:
     - 기능은 아래 제공된 기능 후보 목록(available_features) 중에서만 고르세요.
     - 복수의 기능이 포함되어 있다면, 쉼표(,)로 구분된 하나의 줄로 기능명을 모두 나열하세요.
     - 후보 목록에 없거나 관련 없는 변경은 포함하지 마세요.
     - 결과는 오직 아래 형식 중 하나로만 출력하세요:
     - 없다면 `"기능 없음"`으로만 응답
     - 절대 설명이나 줄바꿈 없이, 딱 그 문장 하나만 출력할 것

     👉 "기능 없음"
     👉 "기능1, 기능2, 기능3"
    
        📌 예시 출력:
        로그인 기능
        회원가입 기능
        기능 없음
        
     """),
    ("human", 
     """
     ✅ 코드 변경 내용:
     {diff_text}

     ✅ 가능한 기능 목록:
     {available_features}
     """)
])