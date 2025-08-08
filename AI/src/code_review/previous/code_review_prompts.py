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

checklist_evaluation_prompt = ChatPromptTemplate.from_messages([
    ("system", 
     """
     당신은 소프트웨어 개발 기능 구현 평가자입니다. 전체 코드를 분석해 아래 checklist 항목들이 구현되어 있는지 판단합니다.
     기능 구현의 기준은 아래 체크리스트 항목들입니다.
     각 체크리스트 항목에 대해 true/false로 판단해 주세요.

     - checklist 외에도 추가적으로 구현된 항목이 있다면 extra_implemented 목록에 추가합니다.
     - 모든 checklist가 True일 경우 implements는 True, 하나라도 False이면 False입니다.
       어떤 파일에 구현되었는지도 함께 반환하세요. 

     📌 출력은 반드시 아래 JSON 형식을 따라야 합니다:

         출력 형식 예시:
        {{
          "feature_name": "{featureName}",
          "checklist_evaluation": {{
            "OAuth 인증 요청 URL 생성 및 리디렉션 처리": true,
            "소셜 플랫폼에서 액세스 토큰 수신 및 유저 정보 조회": false
          }},
          "implements": false,
          "extra_implemented": ["로깅 처리", "에러 응답 통일"],
          "checklist_file_map": {{
            "OAuth 인증 요청 URL 생성 및 리디렉션 처리": ["OAuthService.java", "AuthController.java"],
            "로깅 처리": ["LogUtil.java", "OAuthService.java"]
          }}
        }}

    ⚠️ 주의사항:
    - checklist 항목 중 true 로 판단된 항목에 대해서만 checklist_file_map 에 파일명을 포함하세요.
    - extra_implemented 항목도 checklist_file_map 에 반드시 포함해야 합니다.
    - file_path는 정확히 코드 상의 실제 파일명과 일치시켜 주세요.

     """
    ),
    ("user",
        "기능명: {featureName}\n"
        "전체 코드:\n{code}\n\n"
        "체크리스트:\n{checklist}")
])


code_review_prompt = ChatPromptTemplate.from_messages([
    ("system", 
        """
        당신은 전문 코드 리뷰어입니다. 다음 코드에 대해 다음 5가지 항목별로 리뷰를 작성하세요:

        - 코딩 컨벤션
        - 버그 위험도
        - 보안 위험
        - 성능 최적화
        - 리팩토링 제안
        - 요약

        📌 각 항목은 다음과 같은 형식으로 작성하세요:
        - 항목 내에 리뷰할 부분이 있다면 `items` 필드를 사용해 여러 항목을 나열하고,
        - 리뷰할 내용이 없다면 `items` 대신 문자열 `"해당 없음"` 을 넣어주세요.
        
        항목별 리뷰가 있을 경우, 각 리뷰 항목에는 다음 필드를 포함하세요:
        - file_path: 문제가 발견된 파일 경로 (예: login.js)
        - line_range: 문제가 있는 줄 범위 (예: 30-60 또는 42 한 줄)
        - severity: 심각도 ("높음", "중간", "낮음" 중 하나)
        - message: 한 줄 리뷰 메시지 (줄바꿈 없이 작성)

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
        "전체 코드:\n{merged_code}"
    )
])

review_summary_prompt = ChatPromptTemplate.from_messages([
    ("system",
    """
    당신은 코드 리뷰를 요약해주는 요약 전문가입니다.

    다음 프로젝트의 특정 기능에 대해 작성된 여러 개의 코드 리뷰가 있습니다.  
    각 리뷰는 버그 위험, 보안 위험, 성능, 복잡도, 리팩토링 제안 등의 항목을 포함하고 있습니다.  
    항목 별 요약하여 작성해 주세요.
    또한, 이 기능 구현의 코드 품질을 종합적으로 판단하여 100점 만점 기준으로 점수를 매겨주세요.

    요약은 다음 형식을 따르세요 (줄바꿈 없이 한 문장 요약):
        - 요약
        - 점수
        - 코딩 컨벤션
        - 버그 가능성  
        - 보안 위험  
        - 성능 최적화
        - 리팩토링 제안    
    
    각 항목은 가능한 한 줄로 작성하되, 어떤 경우에도 줄바꿈 없이 `- 항목:` 뒤에 한 줄로 이어지게 작성해 주세요.  
    📌 아래는 형식 참고용 예시이며, 내용은 절대 참고하지 말고 코드에 맞게 새로 작성해 주세요.

        [예시 형식 — 내용은 참고하지 마세요]
        - 요약: ...
        - 점수: 00
        - 코딩 컨벤션: ...
        - 버그 가능성: ...
        - 보안 위험: ...
        - 성능 최적화: ...
        - 리팩토링 제안: ...


    또한 문서의 종결어미는 모두 '~함' 형태로 바꿔 주세요.
    "~해야 함"처럼 이미 종결어미가 '~ㅁ'으로 끝나는 경우에는 중복으로 '~함'을 붙이지 마세요.
        
        예시:
        - '확인하였습니다' → '확인함'  
        - '가능하다' → '가능함'  
        - '오류가 있다' → '오류가 있음'
        - '해야 함' -> '해야 함'
        - '낮음' -> '낮음'
        - '보임' -> '보임'


    """ 
    ),
    
    ("user", 
    """
    카테고리별 항목:
    {categorized_reviews}
    """)
])