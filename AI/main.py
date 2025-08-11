from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from src.config.osConfig import *
from src.specification.specification_router import router as specification_router
from src.specification.chat_router import router as chat_router
# from src.code_review.code_review_router import router as code_review_router
# from src.code_review.checklist_router import router as checklist_router
from src.retrospective.retrospective_router import router as retrospective_router


# 환경 변수 로드
load_config()

# FastAPI 인스턴스 생성
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 또는 ["http://localhost:3000"] 등 프론트 주소
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
async def print_routes():
    print("=== ROUTES ===")
    for r in app.routes:
        methods = ",".join(sorted(getattr(r, "methods", []) or []))
        print(f"{methods:10s} {r.path}")
    print("==============")


# 라우터 등록
app.include_router(specification_router, prefix="/specification")
app.include_router(chat_router, prefix="/chat")
# app.include_router(code_review_router, prefix="/api/code-review")
app.include_router(retrospective_router, prefix="/api/retrospective")
# app.include_router(checklist_router)

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
import json, logging

log = logging.getLogger("uvicorn.error")

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    body = (await request.body()).decode("utf-8", errors="ignore")
    log.error("422 on %s\npayload=%s\nerrors=\n%s",
              request.url.path,
              body,
              json.dumps(exc.errors(), ensure_ascii=False, indent=2))
    return JSONResponse(status_code=422, content={"detail": exc.errors()})

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)




# # GMS API 엔드포인트 (gpt-4.1-mini)
# url = f"{base_url}/chat/completions"

# # 요청 헤더 설정
# headers = {
#     "Authorization": f"Bearer {api_key}",
#     "Content-Type": "application/json"
# }

# # 요청 본문 설정
# data = {
#     "model": "gpt-4.1-mini",
#     "messages": [
#         {"role": "system", "content": "Answer in Korean"},
#         {"role": "user", "content": "Summarize the 2024 total production data."}
#     ],
#     "max_tokens": 4096,
#     "temperature": 0.3
# }

# # GMS API에 POST 요청 보내기
# response = requests.post(url, headers=headers, json=data)

# # 응답 처리
# if response.status_code == 200:
#     print("응답:", response.json())
# else:
#     print("에러:", response.status_code, response.text)


# # 프롬프트 및 명세서 생성
# input_text = "Create a system specification for a user authentication feature."
# requirements = generate_requirements(input_text)

# # 출력된 명세서
# print(requirements)
