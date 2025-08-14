
from fastapi import APIRouter
from .checklist_schema import FeatureChecklistRequest, FeatureChecklistResponse, FeatureChecklistExtraRequest, FeatureChecklistExtraResponse
from .feature_checklist_prompt import feature_checklist_prompt, feature_checklist_extra_prompt
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
        formatted += f"- ID: {f.featureId}\n  기능명: {f.title}\n\n"

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




# 추가 기능 체크리스트 생성
@router.post("/api/generate-checklist/extra", response_model=FeatureChecklistExtraResponse)
async def generate_checklist(request: FeatureChecklistExtraRequest):
    project_name = request.projectName
    # 1. 기능 목록 포맷 문자열 생성
    formatted = ""
    for f in request.features:
        formatted += f"- 프로젝트 주제: {project_name}\n ID: {f.featureId}\n  기능명: {f.title}\n\n"

    # 2. 프롬프트 구성 및 GPT 호출
    prompt = feature_checklist_extra_prompt.format_messages(feature_list=formatted)
    response = await llm.ainvoke(prompt)
    print(response)               # response 객체 전체
    print(response.content)

     # 3. LLM 응답에서 content 가져오기
    raw = response.content

    # JSON 펜스 제거
    json_str = re.sub(r"^```json\s*|\s*```$", "", raw, flags=re.MULTILINE).strip()

    # JSON 파싱
    parsed = json.loads(json_str)

    # valid 값은 전체 결과에 추가
    first_feature_data = next(iter(parsed.values()))
    valid_value = first_feature_data["valid"]
    
    # checklistMap 만들기
    checklist_map = {
        fid: data["checklist"] for fid, data in parsed.items() if data["valid"]
    }

    print(f"유효하지 않은 기능입니다.  {valid_value}")

    return {
        "checklistMap": checklist_map,
        "valid": valid_value
    }