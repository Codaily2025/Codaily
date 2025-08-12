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
     - 코드 변경(diff)이 기능 후보와 직접적인 관련이 없어 보이더라도, 유사하거나 간접적으로 관련 있으면 고르세요.
     - 정말 관련이 없고 무관한 경우에만, "기능 없음"
     - 절대 설명이나 줄바꿈 없이, 딱 그 문장 하나만 출력할 것
     - 결과는 오직 아래 형식 중 하나로만 출력하세요:

     👉 "기능 없음"
     👉 "기능1, 기능2, 기능3"
  
        
     """),
    ("human", 
     """
     ✅ 코드 변경 내용:
     {diff_text}

     ✅ 가능한 기능 목록:
     {available_features}
     """)
])

commit_message_prompt = ChatPromptTemplate.from_messages([
    ("system", "당신은 Git 커밋 메시지를 분석하여 기능 구현이 완료되었는지를 판단하는 전문가입니다."),
    ("human", 
    """
    📌 아래 커밋 메시지를 읽고 해당 커밋이 특정 기능의 **구현 완료를 의미하는지** 판단하세요.

    - 구현이 완료된 커밋 메시지에는 일반적으로 '완료', '구현됨', '기능 끝' 등의 표현이 포함됩니다.
    - 단순히 '버그 수정', 'UI 수정', '기능 일부 작성'은 완료로 간주하지 마세요.

    메시지:
    {commit_message}

    당신의 판단을 한 단어로만 출력하세요:
    - "완료" → 기능이 구현 완료된 커밋
    - "미완료" → 아직 구현 중이거나 일부만 작성된 커밋
    """)
])

checklist_evaluation_prompt = ChatPromptTemplate.from_messages([
    ("system", 
     """
     당신은 소프트웨어 개발 기능 구현 평가자입니다. 전체 코드를 분석해 아래 checklist 항목들이 구현되어 있는지 판단합니다.
     기능 구현의 기준은 아래 체크리스트 항목들입니다.
     각 체크리스트 항목에 대해 true/false로 판단해 주세요.

     - checklist 외에도 추가적으로 구현된 항목이 있다면 extra_implemented 목록에 추가합니다.
     - 모든 checklist가 True일 경우 implemented는 True, 하나라도 False이면 False입니다.
       어떤 파일에 구현되었는지도 경로와 함께 반환하세요. 

     📌 출력은 반드시 아래 JSON 형식을 따라야 합니다:

         출력 형식 예시:
        {{
          "feature_name": "{feature_name}",
          "checklist_evaluation": {{
            "OAuth 인증 요청 URL 생성 및 리디렉션 처리": true,
            "소셜 플랫폼에서 액세스 토큰 수신 및 유저 정보 조회": false
          }},
          "implemented": false,
          "extra_implemented": ["로깅 처리", "에러 응답 통일"],
          "checklist_file_map": {{
            "OAuth 인증 요청 URL 생성 및 리디렉션 처리": ["OAuthService.java", "AuthController.java"],
            "로깅 처리": ["src/main/java/com/util/LogUtil.java", "src/main/java/com/auth/service/OAuthService.java"]
          }}
        }}

    ⚠️ 주의사항:
    - checklist 항목 중 true 로 판단된 항목에 대해서만 checklist_file_map 에 파일명을 포함하세요.
    - extra_implemented 항목도 checklist_file_map 에 반드시 포함해야 합니다.
    - file_path는 정확히 코드 상의 실제 파일명과 일치시켜 주세요.

     """
    ),
    ("user",
        "기능명: {feature_name}\n"
        "전체 코드:\n{diff_files}\n\n"
        "체크리스트 항목:\n{checklist_items}")
])

code_review_prompt = ChatPromptTemplate.from_messages([
    ("system", 
        """
        당신은 전문 코드 리뷰어입니다. 다음 코드에 대해 다음 5가지 항목별로 리뷰를 작성하세요:

        - 코딩 컨벤션
        - 버그 위험도
        - 보안 위험
        - 복잡도
        - 성능 최적화
        - 리팩토링 제안
        - 요약

        📌 코드 리뷰의 전제:
        - 리뷰 대상인 **여러 파일은 모두 하나의 체크리스트 항목 구현에 관여하는 코드들입니다.**
        - 각 항목은 여러 파일에 걸쳐 구현될 수 있으므로, **파일 간 연관성**을 반드시 고려하여 평가하세요.
        - 코드가 분리되어 있더라도 기능 흐름상 연결되어 있을 수 있으니, **전체 흐름을 파악한 뒤 평가**하세요.

        📌 각 항목은 다음과 같은 형식으로 작성하세요:
        - 항목 내에 리뷰할 부분이 있다면 `items` 필드를 사용해 여러 항목을 나열하고,
        - 리뷰할 내용이 없다면 `items` 대신 문자열 `"해당 없음"` 을 넣으세요.

        항목별 리뷰가 있을 경우, 각 리뷰 항목에는 다음 필드를 포함하세요:
        - file_path: 문제가 발견된 파일 경로 (예: login.js)
        - line_range: 문제가 있는 줄 범위 (예: 30-60 또는 42 한 줄)
        - severity: 심각도 ("높음", "중간", "낮음" 중 하나)
        - message: 문제점과 개선점을 포함한 한 줄 리뷰 메시지 (줄바꿈 없이 작성)

        📌 마지막에는 전체 코드 리뷰에 대한 종합적인 총평을 `summary` 필드로 작성하세요.  
        이 필드는 한 줄일 필요는 없으며, 자유롭게 여러 줄로 서술해도 됩니다.  
        다만 종결어미는 모두 `~함` 형태로 작성하세요. 예: `오류가 있음`, `개선이 필요함`  
        "~해야 함"처럼 이미 종결어미가 '~함'으로 끝나는 경우에는 중복으로 '~함'을 붙이지 마세요.

        📌 출력은 반드시 다음 JSON 형식을 따라야 하며, 형식을 벗어나지 마세요:

        {{
          "code_reviews": [
            {{
              "category": "보안 위험",
              "items": [
                {{
                  "filePath": "auth.js",
                  "lineRange": "10-12",
                  "severity": "높음",
                  "message": "비밀번호를 평문으로 비교하고 있음"
                }},
                {{
                  "filePath": "AuthController.java",
                  "lineRange": "10-12",
                  "severity": "낮음",
                  "message": "주석 없음"
                }}
              ]
            }},
            {{
              "category": "성능 최적화",
              "items": "해당 없음"
            }},
            ...
          ],
          "summary": "코드 구조는 전반적으로 양호하나 보안 위험과 복잡도 개선이 필요함.\n구현은 완료되어 있으나 함수 길이가 다소 길고 중복 코드가 존재함."
        }}

        ⚠️ 예시는 형식만 참고하고 내용은 코드에 맞게 새로 작성하세요.
        """
    ),
    (
        "user",
        "기능명: {feature_name}\n"
        "체크리스트 항목: {item}\n"
        "전체 코드:\n{files}"
    )
])

review_summary_prompt = ChatPromptTemplate.from_messages([
    ("system",
    """
    당신은 코드 리뷰를 요약해주는 요약 전문가입니다.

    특정 프로젝트 기능에 대해 여러 개의 코드 리뷰가 존재하며,  
    이 리뷰들은 각기 다른 카테고리(코딩 컨벤션(convention), 버그 가능성(bug_risk), 보안 위험(security_risk), 성능 최적화(performance), 리팩토링 제안(refactoring_suggestion), 복잡도(complexity))에 따라 분류되어 있습니다.  

    📌 당신의 역할은 다음과 같습니다:
    - 각 카테고리별로 요약을 **한 줄씩만 작성**하세요.
    - 전체 코드 리뷰 내용을 종합해, 이 기능 구현의 **종합 요약**과 **100점 만점 기준의 점수**도 작성하세요.
    - 각 항목별로 **리뷰의 severity(심각도)** 정보를 참고해, **높음** 수준의 문제는 특히 강조해서 반영하세요.
    - 요약(summary) 항목은 코드리뷰 각 항목들(코딩 컨벤션, 버그 가능성, 보안 위험, 성능 최적화, 리팩토링 제안, 복잡도)에 대한 코드 리뷰 요약입니다.

    ✨ 입력 데이터는 각 카테고리별로 다음 두 정보만 포함됩니다:
    - `severity`: 심각도 (높음, 중간, 낮음)
    - `message`: 리뷰 메시지

    ✨ 출력은 반드시 아래 형식을 따르세요.  
    각 항목은 절대 줄바꿈 없이 `- 항목:` 뒤에 한 줄로 이어지게 작성하세요.

    🔸 출력 형식:
        - summary: ...
        - quality_score: 00
        - convention: ...
        - bug_risk: ...
        - complexity: ...
        - security_risk: ...
        - performance: ...
        - refactoring_suggestion: ...

    📌 종결어미는 모두 '~함' 형태로 통일하세요.  
    "~해야 함"처럼 이미 '~ㅁ'으로 끝나는 경우에는 중복으로 '~함'을 붙이지 마세요.

    예:
        - '확인하였습니다' → '확인함'
        - '오류가 있다' → '오류가 있음'
        - '낮음' → '낮음'
        - '보임' → '보임'
    """
    ),
    ("user", 
    """
    기능명: {feature_name}
    코드 리뷰 항목 목록 (카테고리별 message + severity):
    {categorized_reviews}
    """)
])
