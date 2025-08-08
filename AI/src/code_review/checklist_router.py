
from fastapi import APIRouter
from .schema import FeatureChecklistRequest, FeatureChecklistResponse
from .feature_checklist_prompt import feature_checklist_prompt
from langchain_openai import ChatOpenAI
import json
import re


router = APIRouter()
llm = ChatOpenAI(model="gpt-3.5-turbo", temperature=0)

@router.post("/api/generate-checklist", response_model=FeatureChecklistResponse)
async def generate_checklist(request: FeatureChecklistRequest):
    # 1. 기능 목록 포맷 문자열 생성
    formatted = ""
    for f in request.features:
        formatted += f"- ID: {f.featureId}\n  제목: {f.title}\n  설명: {f.description}\n\n"

    # 2. 프롬프트 구성 및 GPT 호출
    prompt = feature_checklist_prompt.format_messages(feature_list=formatted)
    response = await llm.ainvoke(prompt)
    print(response)               # response 객체 전체
    print(response.content)

     # 3. LLM 응답에서 content 가져오기
    raw = response.content

    # 4. ```json … ``` 마크다운 펜스 제거
    json_str = re.sub(r"^```json\s*|\s*```$", "", raw, flags=re.MULTILINE).strip()

    # 5. JSON 파싱
    checklist_map = json.loads(json_str)


    return {"checklistMap": checklist_map}
