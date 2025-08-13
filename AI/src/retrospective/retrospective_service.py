# src/retrospective/retrospective_service.py
import json
from typing import Dict
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser
from langchain.chat_models import init_chat_model
from src.retrospective.retrospective_prompts import RETROSPECTIVE_JSON_PROMPT
from src.retrospective.retrospective_models import (
    RetrospectiveGenerateRequest,
    RetrospectiveGenerateResponse,
    RetrospectiveSummary,
    RetrospectiveIssueSummary,
)

model = init_chat_model(
    model="gpt-4.1-nano",
    model_provider="openai",
)
parser = JsonOutputParser()

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


async def generate_retrospective(
    req: RetrospectiveGenerateRequest,
) -> RetrospectiveGenerateResponse:
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
            "summary": {
                "overall": "",
                "strengths": "",
                "improvements": "",
                "risks": "",
            },
            "reviewIssuesTop": [],
        }

    return RetrospectiveGenerateResponse(
        date=req.date,
        projectId=req.projectId,
        userId=req.userId,
        triggerType=req.triggerType,
        contentMarkdown=result.get("contentMarkdown", ""),
        summary=RetrospectiveSummary(
            **result.get(
                "summary",
                {"overall": "", "strengths": "", "improvements": "", "risks": ""},
            )
        ),
        productivityMetrics=req.productivityMetrics,
        completedFeatures=req.completedFeatures,
        reviewIssuesTop=[
            RetrospectiveIssueSummary(**i) for i in result.get("reviewIssuesTop", [])
        ],
    )
