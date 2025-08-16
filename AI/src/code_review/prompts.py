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
     당신은 소프트웨어 개발 기능 구현 평가자입니다. 
     전체 코드를 분석해 아래 checklist 항목들이 구현되어 있는지 판단합니다.
     
     기능 구현 판단 규칙:
     1. checklist 항목 각각에 대해 true/false로 명확히 표시합니다.
     2. checklist 항목 외에도 코드 상에 의미 있는 기능 구현이 있으면 extra_implemented에 반드시 추가합니다.
        - 예: 로깅 처리, 예외 처리, 유효성 검증, 에러 응답 통일, 캐싱, 트랜잭션 관리 등
     3. extra_implemented는 반드시 존재 여부를 확인하고, 없으면 빈 배열 []을 반환합니다.
     4. implemented는 checklist가 전부 true일 때만 true, 하나라도 false면 false입니다.
     5. true로 판정된 checklist 항목과 extra_implemented 항목은 checklist_file_map에 파일 경로와 함께 반드시 포함합니다.
     6. file_path는 반드시 실제 코드 파일명과 일치해야 합니다.

     📌 출력은 반드시 아래 JSON 형식을 따라야 합니다 (추가/생략 금지):

        {{
          "feature_name": "{feature_name}",
          "checklist_evaluation": {{
            "항목1": true,
            "항목2": false
          }},
          "implemented": false,
          "extra_implemented": ["로깅 처리", "에러 응답 통일"],
          "checklist_file_map": {{
            "항목1": ["AuthController.java"],
            "로깅 처리": ["src/main/java/com/util/LogUtil.java"]
          }}
        }}

     ⚠️ 주의:
     - checklist_evaluation은 모든 항목을 반드시 포함해야 합니다.
     - extra_implemented는 없더라도 반드시 [] 반환해야 합니다.
     - checklist_file_map에는 true 항목과 extra_implemented만 포함하세요.
     """
    ),
    ("user",
        "기능명: {feature_name}\n\n"
        "전체 코드:\n{diff_files}\n\n"
        "체크리스트 항목:\n{checklist_items}\n\n"
        "checklist 외의 구현된 기능도 반드시 찾아 extra_implemented에 기입하세요."
    )
])

code_review_prompt = ChatPromptTemplate.from_messages([
    ("system", 
        """
        당신은 전문 코드 리뷰어임. 다음 코드에 대해 아래 6개 카테고리와 종합 요약을 작성할 것:
        - 코딩 컨벤션(convention)
        - 버그 위험도(bug_risk)
        - 보안 위험(security_risk)
        - 복잡도(complexity)
        - 성능 최적화(performance)
        - 리팩토링 제안(refactoring_suggestion)
        - 요약(summary)

        전제:
        - 제공된 파일들은 하나의 체크리스트 항목 구현에 관여하는 코드들임.
        - 파일 간 연관성을 고려하여 전체 흐름을 파악한 뒤 평가할 것.

        출력 형식(고정, JSON):
        {
          "code_reviews": [
            { "category": "보안 위험", "items": [ { "filePath": "...", "lineRange": "10-12", "severity": "높음", "message": "..." }, ... ] },
            { "category": "성능 최적화", "items": "해당 없음" },
            ...
          ],
          "summary": "여러 줄 허용, 단 종결어미는 ~함으로 마무리"
        }

        작성 규칙:
        1) 각 카테고리는 최대 3개 항목까지 중요도 순으로 기입(필요 시 1~2개만).
        2) 항목이 없으면 "items": "해당 없음".
        3) 각 리뷰 항목 필드는 반드시 포함:
           - filePath: 입력에 존재하는 실제 파일 경로를 그대로 사용할 것.
           - lineRange: "start-end" 숫자 범위로만 기입(예: 10-58).
             * "모든 코드", "전체" 같은 표현 금지.
             * 특정 지점 문제면 "42-42"처럼 한 줄 범위 사용.
             * 파일 전반 문제면 파일 총 줄수를 계산해 "1-<마지막줄>"로 표기.
             * (줄수 계산: 제공된 파일 내용의 줄바꿈 수 + 1로 추정)
           - severity: {"높음","중간","낮음"} 중 택1. 
             * 보안/버그 관련 취약점은 기본 "중간" 이상, 데이터 유실/취약점 확정이면 "높음".
           - message: 한 줄, 다음 3요소를 모두 포함하고 구체적으로 작성(세미콜론으로 구분).
             * 원인: 규칙/패턴/함수/변수/어노테이션 등 구체 키워드 포함
             * 영향: 사용자/보안/성능/안정성 관점 영향
             * 해결책: 구체적 조치(예: @Email 추가, 정규식 패턴 적용, try-with-resources, 인덱스 추가 등)
             예) "원인: 이메일 정규식 미적용으로 임의 문자열 허용; 영향: 비정상 계정 생성 및 오류 전파 가능; 해결책: javax.validation @Email 또는 Pattern 적용, null/blank 검증 추가"
        4) 파일 간 흐름 이슈(컨트롤러→서비스→리포지토리 등)는 각 파일에 개별 항목으로 나눠 표기.
        5) summary는 전체 코드의 장단점과 우선순위 높은 조치를 정리하고, 문장 끝을 모두 '~함'으로 통일.

        품질 가이드:
        - 가능한 한 구체적(함수명, 메서드명, 필드명, 엔드포인트, 설정 키 등)으로 작성.
        - 중복 메시지는 합쳐 간결히.
        - Line 범위 추정 시, 해당 문제가 시작/끝나는 근처의 코드 특징(조건/호출/주석 등)을 근거로 합리적 범위 산정.
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
    당신은 코드 리뷰를 요약하는 전문가임.

    입력은 기능명과 카테고리별 리뷰 묶음(categorized_reviews)임.
    각 리뷰 아이템은 최소 다음 필드를 가짐:
    - category ∈ {convention, bug_risk, security_risk, performance, refactoring_suggestion, complexity}
    - message: 리뷰 메시지(자유 서술)
    - severity: 심각도 ∈ {높음, 중간, 낮음}

    규칙:
    0) categorized_reviews가 비어있거나 없음 → 아래 형식으로만 출력:
       - summary: 코드를 찾을 수 없음
       - quality_score: 0
       - convention: 코드를 찾을 수 없음
       - bug_risk: 코드를 찾을 수 없음
       - complexity: 코드를 찾을 수 없음
       - security_risk: 코드를 찾을 수 없음
       - performance: 코드를 찾을 수 없음
       - refactoring_suggestion: 코드를 찾을 수 없음

    1) 항목이 존재하면, 각 카테고리 한 줄 요약을 작성하되, 실제 메시지에서 핵심을 뽑아 구체적으로 요약할 것.
       - 구체성: 파일/라인, 규칙명, 함수명, API 엔드포인트, 주요 키워드가 보이면 반영.
       - 중복 메시지는 묶어 간결히 표현.
       - 동일 카테고리 내 severity가 섞여 있으면 "높음"을 우선 반영함.

    2) quality_score는 100점 만점. 가중치 예시(총합 1.0):
       - bug_risk 0.30, security_risk 0.25, performance 0.20, convention 0.10, refactoring_suggestion 0.10, complexity 0.05
       점수 가이던스(대략):
       - 높음 문제 존재: 카테고리별로 최대 -20점(중복 최대치 캡, 전체 최저 0)
       - 중간 문제: 카테고리별 최대 -10점
       - 낮음 문제: 카테고리별 최대 -5점
       - 문제가 없거나 개선 제안만 있으면 감점 없음
       계산은 대략적이어도 일관성 있게, 정수로 출력.

    3) 출력 형식(줄바꿈, 항목 순서, 키 이름 고정):
       - summary: ...
       - quality_score: 00
       - convention: ...
       - bug_risk: ...
       - complexity: ...
       - security_risk: ...
       - performance: ...
       - refactoring_suggestion: ...

    4) 모든 문장은 한 줄, 종결어미는 '~함'으로 통일.
       예) '오류가 있음', '규칙 위반 발견됨', '개선 여지 있음' 등.
    """
    ),
    ("user",
    """
    기능명: {feature_name}

    categorized_reviews:
    {categorized_reviews}
    """)
])
