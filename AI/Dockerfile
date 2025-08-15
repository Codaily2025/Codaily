FROM python:3.11-slim

ENV PYTHONUNBUFFERED=1 TZ=Asia/Seoul
WORKDIR /app

# 일부 패키지 빌드 대비(가벼운 툴체인)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential gcc libffi-dev \
    && rm -rf /var/lib/apt/lists/*

# 의존성 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 앱 복사
COPY . .

EXPOSE 8000
# Nginx가 /ai 프리픽스를 제거하므로 root_path 설정 불필요
CMD ["python","-m","uvicorn","app.main:app","--host","0.0.0.0","--port","8000"]
