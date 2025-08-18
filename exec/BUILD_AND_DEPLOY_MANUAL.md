# 1. GitLab 소스 클론 이후 빌드 및 배포 매뉴얼

## 1) 사용 스택 (제품 / 설정 / 버전)

### 공통 / 플랫폼
- **Cloud / OS**: AWS EC2 (Ubuntu)
- **컨테이너**: Docker (appnet 브리지 네트워크 사용)
- **리버스 프록시**: Host Nginx (EC2 설치, TLS 종료/라우팅)
  - 인증서: Let’s Encrypt
    - ssl_certificate: `/etc/letsencrypt/live/{DOMAIN}/fullchain.pem`
    - ssl_certificate_key: `/etc/letsencrypt/live/{DOMAIN}/privkey.pem`

### 프론트엔드 (정적 서빙)
- **FE 컨테이너**: Nginx (런타임) + (Vite/React 빌드 산출물)
- **통신 흐름**: Browser → Host Nginx → FE 컨테이너
- **참고**: SPA 라우팅 필요 시 FE Nginx 설정에  
  `try_files $uri /index.html;` 추가 권장

### 백엔드 (API 서버)
- **BE 컨테이너**: Spring Boot (내장 Tomcat)
- **JDK 버전**: 17
- **외부 API**: GitHub API, Google/Naver/Kakao OAuth2 API 사용

### AI 서비스
- **python version**: 3.13.5
- **AI 컨테이너**: FastAPI (+Uvicorn)
- **모델 경로**: `/home/ubuntu/models` → `/app/models:ro` (읽기 전용 마운트)
- **AI API**: GMS API Gateway (`https://gms.ssafy.io/gmsapi/api.openai.com/v1`)

### 데이터베이스
- **DB**: PostgreSQL (EC2 호스트 설치, 도커 외부)
- **접속 정보**
  - jdbc:postgresql://172.18.0.1:5432/{DB_NAME}
  - username: `{DB_USER}`
  - password: `{DB_PASSWORD}`

### IDE
- IntelliJ IDEA 2025 1.3
- VS Code 1.103.1

---

## 2) 빌드 시 사용되는 환경 변수
### FE, BE, AI에 들어가야 하는 환경 변수 파일
#### FE
FE 폴더에 .env 파일 추가
```
# .env
# GitHub OAuth
VITE_GITHUB_CLIENT_ID={your-github-client-id}
VITE_GITHUB_SCOPE=repo,user,admin:repo_hook
VITE_GITHUB_REDIRECT_URI={your-domain}/api/oauth/github/callback

# API Base URLs
VITE_BASE_URL={your-domain}/api/
VITE_BASE_URL_2=http://localhost:8080
VITE_BASE_URL_3={your-domain}
```

#### BE
BE/src/main/resources 폴더에 applicaion.yml, application-secret.yml 추가
```
# applicaion.yml
spring:
  application:
    name: Codaily

  jackson:
    serialization:
      fail-on-empty-beans: false

  profiles:
    include: secret

  datasource:
    driver-class-name: org.postgresql.Driver

  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB
      enabled: true
      file-size-threshold: 2KB

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB.CLIENT_ID}
            client-secret: ${GITHUB.CLIENT_SECRET}
            redirect-uri: ${app.url.backend}/oauth/github/callback
            scope:
              - user
              - repo
          google:
            client-id: ${GOOGLE.CLIENT_ID}
            client-secret: ${GOOGLE.CLIENT_SECRET}
            scope:
              - profile
              - email
            redirect-uri: "{baseUrl}/api/login/oauth2/code/google"
          naver:
            client-id: ${NAVER.CLIENT_ID}
            client-secret: ${NAVER.CLIENT_SECRET}
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/api/login/oauth2/code/naver"
          kakao:
            client-id: ${KAKAO_REST_API_KEY}
            redirect-uri: "{baseUrl}/api/login/oauth2/code/kakao"
            authorization-grant-type: authorization_code
            scope: profile_nickname
            client-name: Kakao
        provider:
          github:
            authorization-uri: https://github.com/login/oauth/authorize
            token-uri: https://github.com/login/oauth/access_token
            user-info-uri: https://api.github.com/user
          naver:
            authorization-uri: https://nid.naver.com/oauth2.0/authorize
            token-uri: https://nid.naver.com/oauth2.0/token
            user-info-uri: https://openapi.naver.com/v1/nid/me
            user-name-attribute: response
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id

github:
  client-id: ${GITHUB.CLIENT_ID}
  client-secret: ${GITHUB.CLIENT_SECRET}
  redirect-uri: ${app.url.backend}/oauth/github/callback
  token-uri: https://github.com/login/oauth/access_token
  user-uri: https://api.github.com/user
  api-url: https://api.github.com

gpt:
  api:
    #feature-inference-url: ${app.url.ai}/api/feature-inference
    feature-inference-url: ${internal.ai-base-url}/api/code-review/feature-inference
    #generate-checklist-url: ${app.url.ai}/api/generate-checklist
    generate-checklist-url: ${internal.ai-base-url}/api/generate-checklist

file:
  upload:
    dir: uploads
    base-url: ${app.url.backend}

springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    config-url: /v3/api-docs/swagger-config
    url: /v3/api-docs
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
  override-with-generic-response: false

server:
  port: 8081
```

```
# application-secret.yml
# OAuth keys (소셜 로그인: Google/Naver/Kakao)
GOOGLE:
  CLIENT_ID: {GOOGLE_CLIENT_ID}
  CLIENT_SECRET: {GOOGLE_CLIENT_SECRET}
NAVER:
  CLIENT_ID: {NAVER_CLIENT_ID}
  CLIENT_SECRET: {NAVER_CLIENT_SECRET}
KAKAO_REST_API_KEY: {KAKAO_REST_API_KEY}

# GitHub API (레포/웹훅/커밋 조회 등)
GITHUB:
  CLIENT_ID: {GITHUB_CLIENT_ID}
  CLIENT_SECRET: {GITHUB_CLIENT_SECRET}

spring:
  datasource:
    url: jdbc:postgresql://{DB_HOST}:{DB_PORT}/{DB_NAME}
    username: {DB_USER}
    password: {DB_PASSWORD}

jwt:
  secret: {JWT_SECRET}

app:
  frontend-url: {your-domain}
  url:
    ai: {your-domain}
    backend: {your-domain}
    webhook: {your-domain}/api/webhook/

# 백엔드→AI 내부 호출(프록시 미경유, 도커 DNS)
internal:
  ai-base-url: http://ai-app:8000
```

#### AI
AI 폴더에 .env 파일 추가
```
# .env
# GMS API Gateway (OpenAI 호환형 엔드포인트)
OPENAI_API_KEY={GMS_API_KEY}
OPENAI_API_BASE=https://gms.ssafy.io/gmsapi/api.openai.com/v1
```

### 배포 시 사용되는 환경 변수

#### FE (Jenkins FE Job)
- 경로: `/home/ubuntu/secret-fe/.env.production`
- **VITE_ 접두사 키만 주입**
  - `VITE_GITHUB_CLIENT_ID`
  - `VITE_GITHUB_SCOPE`
  - `VITE_GITHUB_REDIRECT_URI`
  - `VITE_BASE_URL`
  - `VITE_BASE_URL_2`
  - `VITE_BASE_URL_3`

#### BE (Jenkins BE Job)
- `SPRING_CONFIG_ADDITIONAL_LOCATION=file:/app/secret/`
- `SPRING_PROFILES_ACTIVE=secret`
- `INTERNAL_AI_BASE_URL=http://ai-app:8000`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://172.18.0.1:5432/{DB_NAME}`
- `SPRING_DATASOURCE_USERNAME={DB_USER}`
- `SPRING_DATASOURCE_PASSWORD={DB_PASSWORD}`

#### AI (Jenkins AI Job)
- `--env-file /home/ubuntu/secret-ai/.env`
- 모델 디렉토리:  
  `-v /home/ubuntu/models:/app/models:ro`

---

## 3) 배포 시 특이사항 및 체크리스트

### 공통
- 모든 컨테이너 **127.0.0.1 바인딩** (외부 직접 접근 불가, Host Nginx만 외부 공개)
- 도커 네트워크 `appnet` 사전 생성 및 공유
- 컨테이너 네이밍: `fe-app`, `backend-app`, `ai-app`

### Jenkins 파이프라인
- GitLab Webhook → Jenkins Job 트리거
- 공통 빌드 패턴:
  1. 이미지 태깅: `${BUILD_NUMBER}-YYYYmmdd-HHMM` & `latest`
  2. 기존 컨테이너 중지 및 제거 후 재실행
  3. 헬스체크 후 실패 시 로그 출력
- 헬스체크 엔드포인트:
  - FE: `/`
  - BE: `/actuator/health`
  - AI: `/health`

### Host Nginx
- 프록시 라우팅:
  - `/ → FE (127.0.0.1:8082)`
  - `/api/ → BE (127.0.0.1:8081)`
  - `/ai/ → AI (127.0.0.1:8000)`
- 주의: `proxy_pass` 뒤 슬래시(`"/"`) 없음 (URI 보존)
- AI 요청은 대용량 전송 대비 설정 필요:
client_max_body_size 50m;
proxy_read_timeout 600s;
proxy_send_timeout 600s;

---

## 4) DB 접속 / 계정 / 프로퍼티 정의 파일 목록

- **BE 시크릿 디렉토리**: `/home/ubuntu/secret/`
- 컨테이너 내 `/app/secret/`
- `application-secret.yml` 포함
- **AI 환경 변수 파일**: `/home/ubuntu/secret-ai/.env`
- **FE 환경 변수 파일**: `/home/ubuntu/secret-fe/.env.production`
- **Host Nginx 설정**: `/etc/nginx/sites-available/default`
- **FE Nginx conf**: `/home/ubuntu/FE/nginx.conf`
- **Jenkins Execute Shell**: 각 Job별 Script

---


## 로컬 실행
로컬 환경에서 Codaily 프로젝트를 실행하기 위한 간단한 안내입니다. 아래와 같이 프론트엔드, 백엔드, AI 모듈을 각각 별도의 터미널에서 실행해야 합니다. 실행에 앞서 Node.js (v22+ 권장), JDK (Java 17 이상), **Python 3.10+**가 설치되어 있어야 합니다.

### 프론트엔드 (FE) 실행

저장소 클론 후, 터미널을 열고 FE 디렉토리로 이동합니다.

필요한 패키지를 설치합니다: npm install

개발 서버를 실행합니다: npm run dev

브라우저에서 http://localhost:5173 으로 접속하여 프론트엔드 애플리케이션을 확인합니다.

### 백엔드 (BE) 실행

터미널에서 BE 디렉토리로 이동합니다.

데이터베이스(PostgreSQL)가 로컬에 설치되어 있고 application.yml (혹은 환경 변수)에 올바른 DB 접속 정보가 설정되었는지 확인합니다.

Gradle을 사용하여 백엔드 서버를 실행합니다: ./gradlew bootRun (또는 Windows의 경우 gradlew.bat bootRun).

백엔드 Spring Boot 서버가 http://localhost:8081 에서 실행됩니다. (정상 실행 시 API 엔드포인트들이 활성화됩니다.)
스웨거 API 문서를 통해 사용 가능한 API를 확인 가능합니다.(http://localhost:8081/swagger-ui/index.html)

### 인공지능 모듈 (AI) 실행

터미널에서 AI 디렉토리로 이동합니다.

가상환경(venv)을 활성화하고 의존성을 설치합니다: pip install -r requirements.txt.

FastAPI 서버를 실행합니다 (Uvicorn 사용): uvicorn app.main:app --reload.

기본적으로 http://localhost:8000 (또는 지정된 포트)에서 실행됩니다.

⚠️ Note: 세 가지 모듈(FE, BE, AI)은 각각 별개로 실행되어야 하지만 상호 연동하여 동작합니다. 먼저 백엔드와 AI 서버를 가동한 후 프론트엔드를 실행하면, 프론트엔드가 백엔드(API 서버)에 요청을 보내고 백엔드는 필요한 경우 AI 모듈을 호출하여 응답을 구성합니다. 각 모듈의 설정 파일에서 API 엔드포인트 URL이나 포트 번호 등을 조정할 수 있습니다.