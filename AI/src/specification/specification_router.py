from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
import asyncio
import time

from src.specification.specification_service import *

router = APIRouter()

class RequestBody(BaseModel):
    project_description: str

from fastapi import Request
import time

from fastapi.responses import StreamingResponse
from fastapi import Request
import time
import json


@router.get("/generate")
async def generate_spec(project_description: str, request: Request):
    print("[요청된 설명]", project_description)

    try:
        start_time = time.perf_counter()
        first_chunk_sent = False
        total_sub_functions = 0  # 누적 변수

        async def timed_stream():
            nonlocal first_chunk_sent, total_sub_functions

            async for chunk in stream_function_specification(project_description):
                # chunk는 dict 형태
                wrapper = {
                    "type": "spec",
                    "content": chunk
                }
                yield f"data: {json.dumps(wrapper, ensure_ascii=False)}\n\n"

                if not first_chunk_sent:
                    elapsed_first = time.perf_counter() - start_time
                    print(f"첫 SSE 조각 전송까지: {elapsed_first:.2f}초")
                    first_chunk_sent = True

                # sub_function 개수 누적
                try:
                    sub_funcs = chunk.get("sub_functions", [])
                    total_sub_functions += len(sub_funcs)
                except Exception as e:
                    print(f"sub_function 파싱 실패 (무시됨): {e}")

            # 전체 종료 시간
            elapsed_total = time.perf_counter() - start_time
            print(f"전체 SSE 완료 시간: {elapsed_total:.2f}초")
            print(f"총 상세 기능 개수: {total_sub_functions}개")

        return StreamingResponse(timed_stream(), media_type="text/event-stream")




        # 동기 함수 시간 측정
        # start_sync = time.perf_counter()
        # result_sync = await asyncio.to_thread(generate_function_specification_pipeline, request.project_description)
        # end_sync = time.perf_counter()

        # 비동기 함수 시간 측정
        # start_async = time.perf_counter()
        # result_async = await generate_function_specification_pipeline_async(request.project_description)
        # end_async = time.perf_counter()

        # return {
        #     "async": {
        #         "result": result_async,
        #         "time_seconds": round(end_async - start_async, 4)
        #     }
        # }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# @router.post("/generate")
# async def generate_spec(request: RequestBody):
#     try:
#         print(request.project_description)
#         result = await asyncio.to_thread(generate_function_specification_pipeline, request.project_description)
#         result2 = await generate_function_specification_pipeline_async(request.project_description)
#         return result
#     except Exception as e:
#         raise HTTPException(status_code=500, detail=str(e))
