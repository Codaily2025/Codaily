from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from src.config.osConfig import *
from src.specification.specification_router import router as specification_router
from src.specification.chat_router import router as chat_router


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
    
# 라우터 등록
app.include_router(specification_router, prefix="/specification")
app.include_router(chat_router, prefix="/chat")

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
