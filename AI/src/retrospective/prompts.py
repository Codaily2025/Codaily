# src/retrospective/prompts.py
RETROSPECTIVE_JSON_PROMPT = """
당신은 하루 회고 전문 비서입니다. 아래 정보를 바탕으로 결과를 반드시 올바른 JSON만으로 출력하세요.
필드는 정확히 이 스키마만 포함하세요:
{{
  "contentMarkdown": string,
  "summary": {{
    "overall": string,
    "strengths": string,
    "improvements": string,
    "risks": string
  }},
  "actionItems": [
    {{"title": string, "description": string|null}}, ...
  ],
  "reviewIssuesTop": [
    {{"issueId": number|null, "summary": string}}, ...
  ]
}}

[메타]
- 날짜: {date}
- 프로젝트: {project_id}
- 사용자: {user_id}
- 트리거: {trigger_type}

[완료 기능 요약]
{features_text}

[생산성/품질 지표]
{metrics_text}

지침:
- "contentMarkdown"는 Markdown 섹션들(## 요약, ## 진행, ## 이슈, ## 개선/액션 등)로 구성하세요.
- 액션 아이템은 2~5개로 간결하고 실행 가능하게.
- JSON 이외의 텍스트는 출력하지 마세요.
"""
