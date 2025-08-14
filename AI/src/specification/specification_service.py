import re
import os
import asyncio
from dotenv import load_dotenv
from langchain.chat_models import init_chat_model
from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from typing import AsyncGenerator, Awaitable, Any

from src.specification.specification_prompts import *


MIN_FIELDS = 0
MAX_FIELDS = 5

MIN_MAIN_FUNCTIONS = 0
MAX_MAIN_FUNCTIONS = 5

MIN_SUB_FUNCTIONS = 0
MAX_SUB_FUNCTIONS = 5


# 1. 환경 변수 로드
load_dotenv()

# 2. GPT-4.1 mini 모델 초기화
model = init_chat_model(
    "gpt-4.1-nano",
    model_provider="openai",
)

str_parser = StrOutputParser()


async def function_fields_generator(project_description: str, time: int) -> list:
    """
    주어진 프로젝트 설명을 기반으로 핵심 기능 그룹 목록을 리스트로 반환합니다.
    """
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", SPEC_GEN_FIELD_SYS),
            (
                "user",
                SPEC_GEN_FIELD_USER.format(min=MIN_FIELDS, max=MAX_FIELDS),
            ),
        ]
    )

    chain = prompt | model | str_parser
    raw_output = await chain.ainvoke({"description": project_description, "time": time})

    # 리스트로 변환 (Markdown-style list 형식 파싱)
    lines = raw_output.strip().split("\n")
    groups = []
    for line in lines:
        if line.strip().startswith("-"):
            content = re.sub(r"^-", "", line).strip()  # "- " 제거
            if ":" in content:
                title, time_str = content.split(":", 1)
                try:
                    time_value = float(time_str.strip())  # 시간은 숫자로 변환
                except ValueError:
                    time_value = None
                groups.append({"title": title.strip(), "time": time_value})

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
            name, desc, time = content.split(":", 2)
            result.append(
                {
                    key_name: name.strip(),
                    "description": desc.strip(),
                    "time": float(time.strip()),
                }
            )
    return result


async def main_functions_generator(
    project_description: str, function_group: dict[str, float]
) -> list[dict]:
    """
    주어진 기능 그룹에 대해 주요 기능 항목을 리스트로 반환합니다.
    예시:
    [
        {"기능명": "회원가입", "설명": "이메일과 비밀번호로 계정 생성"},
        {"기능명": "로그인", "설명": "계정으로 서비스에 접근"}
    ]
    """
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", SPEC_GEN_MAIN_FUNC_SYS),
            (
                "user",
                SPEC_GEN_MAIN_FUNC_USER.format(
                    min=MIN_MAIN_FUNCTIONS, max=MAX_MAIN_FUNCTIONS
                ),
            ),
        ]
    )

    chain = prompt | model | str_parser
    response = await chain.ainvoke(
        {
            "description": project_description,
            "group": function_group,
            "time": function_group["time"],
        }
    )

    # 문자열 응답을 리스트[dict]로 파싱
    result = parse_bullet_items(response, "title")

    return result


def parse_bullet_items_with_duration_and_priority(
    response: str, key_name: str
) -> list[dict]:
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
                result.append(
                    {
                        key_name: name,
                        "description": desc,
                        "estimated_time": float(duration),
                        "priority_level": int(priority),
                    }
                )
            except ValueError:
                continue  # 숫자 형식 오류 방지
    return result


async def spec_sub_functions_generator(
    project_description: str, function_group: str, main_function: str
) -> list[dict]:
    """
    주어진 주 기능 항목에 대해 상세 기능 항목들을 list[dict] 형식으로 반환합니다.
    예: { "제목 입력": "작업 제목을 입력 받음", ... }
    """
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", SPEC_GEN_SUB_FUNC_SYS),
            (
                "user",
                SPEC_GEN_SUB_FUNC_USER.format(
                    min=MIN_SUB_FUNCTIONS, max=MAX_SUB_FUNCTIONS
                ),
            ),
        ]
    )

    chain = prompt | model | str_parser
    response = await chain.ainvoke(
        {
            "description": project_description,
            "group": function_group,
            "main_function": main_function,
        }
    )

    # 문자열 응답을 list[dict]로 파싱
    result = parse_bullet_items_with_duration_and_priority(response, "title")

    return result


async def process_main_function(
    project_description: str,
    field: str,
    main_func: dict[str, str, float],
    queue: asyncio.Queue,
):
    # print("main_func: ", main_func)
    main_func_full = (
        f"{main_func['title']}:{main_func['description']}:{main_func['time']}"
    )
    sub_functions = await spec_sub_functions_generator(
        project_description, field, main_func_full
    )
    result = {
        "field": field,
        "main_feature": {
            "title": main_func["title"],
            "description": main_func["description"],
        },
        "sub_feature": sub_functions,
    }
    # print("result: ", result)
    await queue.put(result)


async def process_field(
    project_description: str, field: dict[str, float], queue: asyncio.Queue
):
    main_functions = await main_functions_generator(project_description, field)
    # print("main_func: ", main_functions, "\n")
    tasks = [
        process_main_function(project_description, field["title"], main_func, queue)
        for main_func in main_functions
    ]
    await asyncio.gather(*tasks)


SENTINEL = object()


async def watch_all_fields(field_tasks: list[Awaitable[Any]], queue: asyncio.Queue):
    results = await asyncio.gather(*field_tasks, return_exceptions=True)

    for r in results:
        if isinstance(r, Exception):
            print(f"[watch] producer error: {r!r}")
            pass
    await queue.put(SENTINEL)


async def stream_function_specification(
    project_description: str,
    time: int,
) -> AsyncGenerator[str, None]:
    queue = asyncio.Queue()

    function_fields = await function_fields_generator(project_description, time)
    field_tasks = [
        process_field(project_description, field, queue) for field in function_fields
    ]

    async def generate_summary():
        summary = await wrap_project_summary_as_sse(project_description)
        await queue.put(summary)

    field_tasks.append(generate_summary())

    watcher = asyncio.create_task(watch_all_fields(field_tasks, queue))

    while True:
        try:
            item = await asyncio.wait_for(queue.get(), timeout=0.1)
            # print("item: ", item)
            if item is SENTINEL:
                break

            if isinstance(item, dict) and "type" in item and "content" in item:
                yield item
            else:
                yield wrap_as_sse_chunk("spec", item)
        except asyncio.TimeoutError:
            continue

    try:
        await watcher
    except Exception:
        print("기능 명세서 생성 오류")
        pass


async def project_summary_generator(history_text: str) -> dict:
    """
    주어진 대화 내용을 기반으로 프로젝트 제목, 설명, 명세서 제목을 추출합니다.
    """
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", SPEC_GEN_SUMMARY_SYS),
            ("user", "{history_text}"),
        ]
    )

    chain = prompt | model | str_parser
    raw_output = await chain.ainvoke({"history_text": history_text})

    # 파싱
    lines = raw_output.strip().split("\n")
    project_title = next(
        (
            l.replace("프로젝트 제목:", "").strip()
            for l in lines
            if l.startswith("프로젝트 제목:")
        ),
        "",
    )
    project_desc = next(
        (
            l.replace("프로젝트 설명:", "").strip()
            for l in lines
            if l.startswith("프로젝트 설명:")
        ),
        "",
    )
    spec_title = next(
        (
            l.replace("명세서 제목:", "").strip()
            for l in lines
            if l.startswith("명세서 제목:")
        ),
        "",
    )

    return {
        "projectTitle": project_title,
        "projectDescription": project_desc,
        "specTitle": spec_title,
    }


async def wrap_project_summary_as_sse(history_text: str) -> dict:
    """
    대화 내용을 바탕으로 생성된 프로젝트 요약을
    {"type": "project:summarization", "content": {...}} 형식으로 감싸 반환합니다.
    """
    summary = await project_summary_generator(history_text)
    return {"type": "project:summarization", "content": summary}


def wrap_as_sse_chunk(chunk_type: str, content: dict | list) -> str:
    return {"type": chunk_type, "content": content}


async def generate_main_feature_from_message(message_text: str, field: str) -> dict:
    """
    사용자 메시지로부터 주 기능 1개 (title, description) 생성
    """
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", SPEC_ADD_MAIN_FUNC_SYS),
            ("user", "{message_text}"),
        ]
    )

    chain = prompt | model | str_parser
    raw = await chain.ainvoke({"field": field, "message_text": message_text.strip()})

    # print(raw)
    # 파싱
    parsed = parse_bullet_items(raw, key_name="title")
    # print(parsed)
    return (
        parsed[0]
        if parsed
        else {"title": "(제목 없음)", "description": "", "time": 0.0}
    )


async def stream_sub_feature(history: list, title: str, field: str):
    """
    주어진 주 기능을 기반으로 GPT를 통해 상세 기능 1개를 생성합니다.
    """
    # history를 하나의 문자열로 변환
    message_text = "\n".join(m.content for m in history)

    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", SPEC_ADD_SUB_FUNC_SYS),
            ("user", SPEC_ADD_SUB_FUNC_USER),
        ]
    )

    # LLM 호출 및 파싱
    chain = prompt | model | str_parser
    response = await chain.ainvoke(
        {"field": field, "title": title, "message_text": message_text}
    )

    # print(response)
    parsed = parse_bullet_items_with_duration_and_priority(response, key_name="title")
    # print(parsed)
    if not parsed:
        raise ValueError("상세 기능 생성 실패: 유효한 항목이 없습니다.")
    # print(parsed[0])
    return parsed[0]


async def generate_field_from_message(user_message: str) -> dict[str, float]:
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", SPEC_ADD_GROUP_SYS),
            ("user", "요청 내용: {user_message}"),
        ]
    )

    chain = prompt | model | str_parser
    raw = (await chain.ainvoke({"user_message": user_message})).strip()

    # 파싱
    if ":" not in raw:
        return {"title": raw, "time": 0.0}
    title, time_str = raw.split(":", 1)
    try:
        time_val = float(time_str.strip())
    except ValueError:
        time_val = 0.0
    return {"title": title.strip(), "time": time_val}


async def stream_single_field(
    project_description: str, field: dict[str, float]
) -> AsyncGenerator[str, None]:
    queue = asyncio.Queue()

    field_tasks = [process_field(project_description, field, queue)]

    watcher = asyncio.create_task(watch_all_fields(field_tasks, queue))

    while True:
        try:
            item = await asyncio.wait_for(queue.get(), timeout=0.1)

            if item is SENTINEL:
                break

            yield item
        except asyncio.TimeoutError:
            continue

    try:
        await watcher
    except Exception:
        print("하나의 기능 그룹에 대한 주, 상세 기능 생성 오류")
        pass


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

#    return result


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
