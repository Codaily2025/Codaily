# AI/Dockerfile
FROM python:3.11-slim

ENV PYTHONUNBUFFERED=1 TZ=Asia/Seoul
WORKDIR /app

# 일부 패키지 빌드 대비
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential gcc libffi-dev \
  && rm -rf /var/lib/apt/lists/*

# 의존성
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 앱 복사
COPY . .

# src 모듈 임포트 가능하도록
ENV PYTHONPATH=/app

EXPOSE 8000
# main.py가 루트에 있으므로 app.main이 아니라 main:app
CMD ["python","-m","uvicorn","main:app","--host","0.0.0.0","--port","8000"]
