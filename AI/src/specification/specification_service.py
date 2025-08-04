import re
import os
import asyncio
from dotenv import load_dotenv
from langchain.chat_models import init_chat_model
from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from typing import AsyncGenerator


MIN_GROUPS = 0
MAX_GROUPS = 2

MIN_MAIN_FUNCTIONS = 0
MAX_MAIN_FUNCTIONS = 2

MIN_SUB_FUNCTIONS = 0
MAX_SUB_FUNCTIONS = 2


# 1. 환경 변수 로드
load_dotenv()
api_key = os.getenv("OPENAI_API_KEY")
base_url = os.getenv("OPENAI_API_BASE")

# 2. GPT-4.1 mini 모델 초기화
model = init_chat_model(
    "gpt-4.1-nano",
    model_provider="openai",
)

# 3. 프롬프트 템플릿 설정
system_template = "Translate the following from English into {language}"
prompt_template = ChatPromptTemplate.from_messages([
    ("system", system_template),
    ("user", "{text}")
])

# 4. 파서 선언 (AIMessage → str)
parser = StrOutputParser()

# 5. 체인 구성 (프롬프트 → 모델 → 파서)
chain = prompt_template | model | parser

# 6. 요구사항 명세서 생성 함수
def generate_requirements(input_text, language="Korean"):
    return chain.invoke({"language": language, "text": input_text})




def generate_function_groups(project_description: str) -> list:
    """
    주어진 프로젝트 설명을 기반으로 핵심 기능 그룹 목록을 리스트로 반환합니다.
    """
    prompt = ChatPromptTemplate.from_messages([
        ("system", "당신은 사용자의 프로젝트 설명을 바탕으로 기능 명세서를 구성하는 전문가입니다."),
        ("user", (
            "**다음 조건을 정확히 따라 기능 그룹을 도출하세요.**\n"
            "- 항목 개수: **{min}개 이상, {max}개 이하**\n"
            "- 형식: `- 항목명` (설명이나 번호 없이)\n\n"
            "▼ 프로젝트 설명:\n"
            "{{description}}"
        ).format(min=MIN_GROUPS, max=MAX_GROUPS))
    ])

    chain = prompt | model | StrOutputParser()
    raw_output = chain.invoke({"description": project_description})

    # 리스트로 변환 (Markdown-style list 형식 파싱)
    lines = raw_output.strip().split("\n")
    groups = [re.sub(r"^-", "", line).strip() for line in lines if line.strip().startswith("-")]

    return groups


def parse_bullet_items(response: str, key_name: str) -> list[dict]:
    """
    '- 이름: 설명' 형식의 문자열 리스트를 파싱하여 리스트[dict]로 변환합니다.

    :param response: LLM에서 출력된 문자열
    :param key_name: 기능명을 저장할 dict 키 ("기능명" 또는 "상세기능명" 등)
    :return: [{"기능명": "...", "설명": "..."}, ...]
    """
    result = []
    lines = response.strip().splitlines()
    for line in lines:
        if line.strip().startswith("-") and ":" in line:
            content = line.lstrip("-").strip()
            name, desc = content.split(":", 1)
            result.append({
                key_name: name.strip(),
                "description": desc.strip()
            })
    return result


def generate_main_functions(project_description: str, function_group: str) -> list[dict]:
    """
    주어진 기능 그룹에 대해 주요 기능 항목을 리스트로 반환합니다.
    예시:
    [
        {"기능명": "회원가입", "설명": "이메일과 비밀번호로 계정 생성"},
        {"기능명": "로그인", "설명": "계정으로 서비스에 접근"}
    ]
    """
    prompt = ChatPromptTemplate.from_messages([
        ("system", (
            "당신은 사용자의 프로젝트 설명을 바탕으로 기능 명세서를 작성하는 전문가입니다.\n\n"
            "출력 형식은 반드시 다음 예시를 따르세요 (기능명은 짧고, 설명은 한 줄로):\n"
            "- 회원가입: 사용자가 이메일과 비밀번호로 계정을 생성할 수 있음\n"
            "- 로그인: 사용자가 계정으로 서비스에 로그인할 수 있음\n"
            "- 장바구니 담기: 사용자가 상품을 장바구니에 추가할 수 있음\n\n"
            "작성 기준:\n"
            "- 기능명은 가능한 한 간결하고 구체적인 **명사형 표현**으로 작성하세요\n"
            "- 설명은 **사용자 관점**에서, 이 기능을 통해 **무엇을 할 수 있는지 한 줄로** 작성하세요\n"
            "- 형식 오류나 누락이 없도록 주의하세요"
        )),
        ("user", (
            "**아래 조건을 정확히 지켜서 주요 기능을 도출하세요.**\n"
            "- 개수: **{min}개 이상, {max}개 이하**\n"
            "- 형식: `- 기능명: 설명` (설명은 한 줄이며 사용자 관점)\n\n"
            "▼ 프로젝트 설명:\n"
            "{{description}}\n\n"
            "▼ 기능 그룹:\n"
            "{{group}}"
        ).format(min=MIN_MAIN_FUNCTIONS, max=MAX_MAIN_FUNCTIONS))
    ])

    chain = prompt | model | StrOutputParser()
    response = chain.invoke({
        "description": project_description,
        "group": function_group
    })

    # 문자열 응답을 리스트[dict]로 파싱
    result = parse_bullet_items(response, "title")

    return result


def parse_bullet_items_with_duration_and_priority(response: str, key_name: str) -> list[dict]:
    """
    '- 이름: 설명: 시간: 우선순위' 형식의 문자열 리스트를 파싱하여 리스트[dict]로 변환합니다.
    """
    result = []
    lines = response.strip().splitlines()
    for line in lines:
        if line.strip().startswith("-") and line.count(":") >= 3:
            content = line.lstrip("-").strip()
            name, desc, duration, priority = map(str.strip, content.split(":", 3))
            try:
                result.append({
                    key_name: name,
                    "description": desc,
                    "estimated_time": float(duration.replace("시간", "").strip()),
                    "priority_level": int(priority)
                })
            except ValueError:
                continue  # 숫자 형식 오류 방지
    return result


def generate_sub_functions(project_description: str, function_group: str, main_function: str) -> list[dict]:
    """
    주어진 주 기능 항목에 대해 상세 기능 항목들을 list[dict] 형식으로 반환합니다.
    예: { "제목 입력": "작업 제목을 입력 받음", ... }
    """
    prompt = ChatPromptTemplate.from_messages([
        ("system", (
            "당신은 사용자의 프로젝트 설명을 바탕으로 **상세 기능 목록을 작성하는 전문가**입니다.\n\n"
            "각 항목은 반드시 다음 형식을 따라야 합니다:\n"
            "- 상세기능명: 설명: 예상시간(정수): 우선순위(1~10 정수)\n\n"
            "예시:\n"
            "- 이메일 입력 필드 표시: 사용자가 이메일을 입력할 수 있는 입력창을 화면에 표시: 1: 8\n"
            "- 가입 버튼 클릭 처리: 사용자가 가입 버튼을 클릭하면 계정 생성 요청을 서버에 전송: 1: 10\n"
            "- 비밀번호 확인 검증: 비밀번호와 비밀번호 확인 값이 같은지 실시간으로 검증: 1: 7\n\n"
            "작성 규칙:\n"
            "- 각 항목은 실제 개발 단위로 나뉠 수 있는 작고 명확한 단계여야 합니다.\n"
            "- 상세기능명은 **시스템이 수행하는 동작을 구체적인 동사로 시작**하세요.\n"
            "- 설명은 사용자가 겪는 행동 또는 시스템의 반응을 **한 문장**으로 요약하세요.\n"
            "- 예상 시간은 시간 단위의 정수로, 우선순위는 1~10 정수로 반드시 기입하세요.\n"
            "- 주 기능과 **동일하거나 유사한 기능은 제외**하세요.\n\n"
            "우선순위는 다음 기준에 따라 판단하세요:\n"
            "1. 다른 기능의 선행 조건인지\n"
            "2. 사용자 경험에서의 중요도\n"
            "3. 기능의 핵심 여부\n"
            "4. 보안이나 성능에 미치는 영향"
        )),
        ("user", (
            "**다음 조건을 정확히 따르세요:**\n"
            "- 개수: **{min}개 이상, {max}개 이하**\n"
            "- 형식: `- 상세기능명: 설명: 예상시간: 우선순위` (모두 빠짐없이 작성)\n"
            "- 주 기능과 중복되거나 거의 같은 상세 기능은 작성하지 마세요.\n\n"
            "▼ 프로젝트 설명:\n"
            "{{description}}\n\n"
            "▼ 기능 그룹:\n"
            "{{group}}\n\n"
            "▼ 주 기능:\n"
            "{{main_function}}"
        ).format(min=MIN_SUB_FUNCTIONS, max=MAX_SUB_FUNCTIONS))
    ])

    chain = prompt | model | StrOutputParser()
    response = chain.invoke({
        "description": project_description,
        "group": function_group,
        "main_function": main_function
    })

    # 문자열 응답을 list[dict]로 파싱
    result = result = parse_bullet_items_with_duration_and_priority(response, "title")

    return result


# def build_function_spec_json(function_spec_data: list) -> dict:
#     """
#     기능 그룹, 주 기능, 상세 기능 데이터를 기반으로 기능 명세 JSON을 생성합니다.
#     """
#     result = {}

#     for item in function_spec_data:
#         group = item["function_group"] # str
#         main = item["main_function"]  # dict: {"기능명": ..., "설명": ...}
#         subs = item["sub_functions"]  # list[dict]: [{"기능명": ..., "설명": ...}, ...]

#         if group not in result:
#             result[group] = []

#         result[group].append({
#             "field": main["기능명"],
#             "설명": main["설명"],
#             "상세 기능": subs
#         })

    return result




async def process_main_function_and_put(project_description: str, group: str, main_func: dict, queue: asyncio.Queue):
    main_func_full = f"{main_func['기능명']}. {main_func['설명']}"
    sub_functions = await asyncio.to_thread(generate_sub_functions, project_description, group, main_func_full)
    result = {
        "field": group,
        "main_feature": main_func,
        "sub_feature": sub_functions
    }
    await queue.put(result)


async def process_group(project_description: str, group: str, queue: asyncio.Queue):
    main_functions = await asyncio.to_thread(generate_main_functions, project_description, group)
    tasks = [
        asyncio.create_task(process_main_function_and_put(project_description, group, main_func, queue))
        for main_func in main_functions
    ]
    await asyncio.gather(*tasks)


async def watch_all_groups(group_tasks: list[asyncio.Task], finished_flag: dict):
    await asyncio.gather(*group_tasks)
    finished_flag["done"] = True


async def stream_function_specification(project_description: str) -> AsyncGenerator[str, None]:
    queue = asyncio.Queue()
    finished_flag = {"done": False}

    function_groups = await asyncio.to_thread(generate_function_groups, project_description)
    group_tasks = [
        asyncio.create_task(process_group(project_description, group, queue))
        for group in function_groups
    ]
    asyncio.create_task(watch_all_groups(group_tasks, finished_flag))

    while not (finished_flag["done"] and queue.empty()):
        try:
            item = await asyncio.wait_for(queue.get(), timeout=1.0)
            yield item
        except asyncio.TimeoutError:
            continue




# async def stream_function_specification(project_description: str) -> AsyncGenerator[str, None]:
#     queue = asyncio.Queue()

#     async def process_main_function_and_put(group: str, main_func: dict):
#         main_func_full = f"{main_func['기능명']}. {main_func['설명']}"
#         sub_functions = await asyncio.to_thread(generate_sub_functions, project_description, group, main_func_full)
#         result = {
#             "function_group": group,
#             "main_function": main_func,
#             "sub_functions": sub_functions
#         }
#         await queue.put(result)

#     async def process_group(group: str):
#         main_functions = await asyncio.to_thread(generate_main_functions, project_description, group)
#         # group 내 main_function들 병렬 실행
#         tasks = [asyncio.create_task(process_main_function_and_put(group, main_func)) for main_func in main_functions]
#         await asyncio.gather(*tasks)

#     function_groups = await asyncio.to_thread(generate_function_groups, project_description)
#     # 모든 그룹 병렬 실행
#     group_tasks = [asyncio.create_task(process_group(group)) for group in function_groups]

#     # 완료 감시자
#     finished = False
#     async def watch_all():
#         nonlocal finished
#         await asyncio.gather(*group_tasks)
#         finished = True
#     asyncio.create_task(watch_all())

#     # 실시간 스트리밍
#     while not (finished and queue.empty()):
#         try:
#             item = await asyncio.wait_for(queue.get(), timeout=1.0)
#             yield f"data: {json.dumps(item, ensure_ascii=False)}\n\n"
#         except asyncio.TimeoutError:
#             continue




# async def process_main_function(project_description: str, group: str, main_func: dict):
#     main_func_full = f"{main_func['기능명']}. {main_func['설명']}"
#     sub_funcs = await asyncio.to_thread(generate_sub_functions, project_description, group, main_func_full)

#     return {
#         "function_group": group,
#         "main_function": main_func,
#         "sub_functions": sub_funcs
#     }


# async def process_group(project_description: str, group: str):
#     main_functions = await asyncio.to_thread(generate_main_functions, project_description, group)
#     return await asyncio.gather(*[
#         process_main_function(project_description, group, main_func)
#         for main_func in main_functions
#     ])


# async def stream_function_specification(project_description: str):
#     function_groups = await asyncio.to_thread(generate_function_groups, project_description)

#     for group in function_groups:
#         main_functions = await asyncio.to_thread(generate_main_functions, project_description, group)

#         for main_func in main_functions:
#             main_func_full = f"{main_func['기능명']}. {main_func['설명']}"
#             sub_functions = await asyncio.to_thread(generate_sub_functions, project_description, group, main_func_full)

#             result = {
#                 "function_group": group,
#                 "main_function": main_func,
#                 "sub_functions": sub_functions
#             }

#             yield f"data: {json.dumps(result, ensure_ascii=False)}\n\n"
            # await asyncio.sleep(0.1)  # optional: 속도 조절




# async def generate_function_specification_pipeline_async(project_description: str):
#     """
#     프로젝트 설명을 받아 기능 그룹 → 주 기능 → 상세 기능 → 최종 JSON까지 생성하는 비동기 파이프라인입니다.
#     """

#     # 1. 기능 그룹 생성
#     function_groups = await asyncio.to_thread(generate_function_groups, project_description)
#     print("[기능 그룹]", function_groups)
#     all_specs = []

#     results = await asyncio.gather(*[process_group(project_description, group) for group in function_groups])
#     for group_result in results:
#         all_specs.extend(group_result)

#     # 4. 최종 JSON 변환
#     function_spec_json = build_function_spec_json(all_specs)
#     json_string = json.dumps(function_spec_json, indent=4, ensure_ascii=False)
#     print(json_string)

#     return function_spec_json




# def generate_function_specification_pipeline(project_description: str):
#     """
#     프로젝트 설명을 받아 기능 그룹 → 주 기능 → 상세 기능 → 최종 JSON까지 생성하는 통합 파이프라인입니다.
#     """
#     # 1. 기능 그룹 생성
#     function_groups = generate_function_groups(project_description)
#     print(function_groups)
#     all_specs = []
    
#     # 2. 각 기능 그룹에 대해 주 기능 생성
#     for group in function_groups:
#         main_functions = generate_main_functions(project_description, group)
#         # print("group: " + group + "\nmain_functions: " + ", ".join([f"{item['기능명']}: {item['설명']}\n" for item in main_functions]))
#         # print("\n")

#         for main_func in main_functions:
#             # 3. 각 주 기능에 대해 상세 기능 생성
#             main_func_full = f"{main_func['기능명']}. {main_func['설명']}"
#             sub_funcs = generate_sub_functions(project_description, group, main_func_full)

#             print(f"[group] {group}")
#             print(f"    [main_func_full] {main_func_full}")
#             print("        [sub_funcs]")
#             for item in sub_funcs:
#                 print(f"          - {item['상세기능명']}: {item['설명']}")
#             print("\n")

#             all_specs.append({
#                 "function_group": group,
#                 "main_function": main_func,
#                 "sub_functions": sub_funcs
#             })

#     # 4. 최종 JSON 변환
#     function_spec_json = build_function_spec_json(all_specs)
#     # 결과 출력 (들여쓰기 + 한글 깨짐 방지)
#     json_string = json.dumps(function_spec_json, indent=4, ensure_ascii=False)
#     print(json_string)

#     return function_spec_json