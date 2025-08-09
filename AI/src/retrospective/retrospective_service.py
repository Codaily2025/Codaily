# src/retrospective/retrospective_service.py
import json
from typing import Dict
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser
from langchain.chat_models import init_chat_model

from .models import (
    RetrospectiveGenerateRequest,
    RetrospectiveGenerateResponse,
    RetrospectiveSummary,
    RetrospectiveIssueSummary,
)

# ====== 여기서 바로 모델/파서/프롬프트 생성 (네 스타일 그대로) ======
# 환경변수로 바꾸고 싶으면 아래 model, provider 문자열만 수정하면 됨
model = init_chat_model(
    model="gpt-4.1-nano",
    model_provider="openai",
)
parser = JsonOutputParser()

RETROSPECTIVE_JSON_PROMPT = """
당신은 하루 회고 전문 비서입니다. 아래 정보를 바탕으로 '정확한 JSON만' 출력하세요.
출력 스키마:
{{
  "contentMarkdown": string,
  "summary": {{ "overall": string, "strengths": string, "improvements": string, "risks": string }},
  "reviewIssuesTop": [ {{ "issueId": number|null, "summary": string }}, ... ]
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
- JSON 이외의 텍스트는 절대 출력하지 마세요.
- "contentMarkdown"는 Markdown 섹션(## 요약, ## 진행, ## 이슈, ## 개선/액션)으로 구성.
- actionItems는 2~5개, 명확하고 실행 가능하게.
"""

prompt = ChatPromptTemplate.from_template(RETROSPECTIVE_JSON_PROMPT)

retrospective_chain = prompt | model | parser

def _features_text(req: RetrospectiveGenerateRequest) -> str:
    if not req.completedFeatures:
        return "- (없음)"
    lines = []
    for f in req.completedFeatures:
        parts = []
        if f.summary:
            parts.append(f"요약: {f.summary}")
        if f.codeQualityScore is not None:
            parts.append(f"품질: {f.codeQualityScore}")
        if f.checklistDoneCount is not None and f.checklistCount is not None:
            parts.append(f"체크리스트 {f.checklistDoneCount}/{f.checklistCount}")
        tail = (": " + "; ".join(parts)) if parts else ""
        lines.append(f"- {f.title or ''} ({f.field or ''}){tail}")
    return "\n".join(lines)

def _metrics_text(req: RetrospectiveGenerateRequest) -> str:
    m = req.productivityMetrics
    return (
        f"코드품질={m.codeQuality}, 생산성점수={m.productivityScore}, "
        f"완료기능수={m.completedFeatures}, 총커밋={m.totalCommits}"
    )

async def generate_retrospective(req: RetrospectiveGenerateRequest) -> RetrospectiveGenerateResponse:
    variables = {
        "date": str(req.date),
        "project_id": req.projectId,
        "user_id": req.userId,
        "trigger_type": req.triggerType,
        "features_text": _features_text(req),
        "metrics_text": _metrics_text(req),
    }

    try:
        result: Dict = await retrospective_chain.ainvoke(variables)
    except Exception as e:
        # 모델/파서 오류 시 폴백
        result = {
            "contentMarkdown": f"## 회고 생성 실패\n오류: {e}",
            "summary": {"overall":"", "strengths":"", "improvements":"", "risks":""},
            "reviewIssuesTop": []
        }

    return RetrospectiveGenerateResponse(
        date=req.date,
        projectId=req.projectId,
        userId=req.userId,
        triggerType=req.triggerType,
        contentMarkdown=result.get("contentMarkdown", ""),
        summary=RetrospectiveSummary(**result.get("summary", {
            "overall":"", "strengths":"", "improvements":"", "risks":""
        })),
        productivityMetrics=req.productivityMetrics,
        completedFeatures=req.completedFeatures,
        reviewIssuesTop=[RetrospectiveIssueSummary(**i) for i in result.get("reviewIssuesTop", [])],
    )
