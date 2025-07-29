import os
import requests
from dotenv import load_dotenv
from src.config.osConfig import *

# .env 파일에서 환경 변수 로드
load_config()

# 환경 변수에서 API 키와 Base URL 가져오기
api_key = os.getenv("OPENAI_API_KEY")
base_url = os.getenv("OPENAI_API_BASE")

# GMS API 엔드포인트 (gpt-4.1-mini)
url = f"{base_url}/chat/completions"

# 요청 헤더 설정
headers = {
    "Authorization": f"Bearer {api_key}",
    "Content-Type": "application/json"
}

# 요청 본문 설정
data = {
    "model": "gpt-4.1-mini",
    "messages": [
        {"role": "system", "content": "Answer in Korean"},
        {"role": "user", "content": "Summarize the 2024 total production data."}
    ],
    "max_tokens": 4096,
    "temperature": 0.3
}

# GMS API에 POST 요청 보내기
response = requests.post(url, headers=headers, json=data)

# 응답 처리
if response.status_code == 200:
    print("응답:", response.json())
else:
    print("에러:", response.status_code, response.text)
