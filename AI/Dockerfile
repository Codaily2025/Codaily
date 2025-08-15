FROM python:3.11-slim

ENV PYTHONUNBUFFERED=1 TZ=Asia/Seoul
# 파이썬 모듈 검색 경로에 /app, /app/src 추가
ENV PYTHONPATH=/app:/app/src

WORKDIR /app

# 빌드 도중 필요한 최소 툴
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential gcc libffi-dev \
    && rm -rf /var/lib/apt/lists/*

# 의존성
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 앱 코드
COPY . .

EXPOSE 8000

# uvicorn 타깃을 main:app 으로 (main.py 가 /app 에 있으므로)
CMD ["python","-m","uvicorn","main:app","--host","0.0.0.0","--port","8000","--proxy-headers"]
