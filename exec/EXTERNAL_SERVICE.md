# 외부 서비스 정보 정리 문서

본 문서는 Codaily 프로젝트에서 사용된 외부 서비스(소셜 인증, 코드 저장소 API, AI 모델 API, 배포 인프라 등)에 대해 가입 및 활용 시 필요한 정보를 정리한 문서입니다.

---

## 1. 소셜 인증 (OAuth2)

프로젝트는 Spring Security OAuth2를 기반으로 **3가지 소셜 로그인을 지원**합니다.

| 제공자 | 인증/토큰 URI | Redirect URI (예시) | 비고 |
|--------|--------------|----------------------|------|
| **Google** | https://accounts.google.com/o/oauth2/v2/auth<br>https://oauth2.googleapis.com/token | `{baseUrl}/api/login/oauth2/code/google` | Gmail/Google 계정 로그인 |
| **Naver** | https://nid.naver.com/oauth2.0/authorize<br>https://nid.naver.com/oauth2.0/token | `{baseUrl}/api/login/oauth2/code/naver` | 네이버 계정 로그인 |
| **Kakao** | https://kauth.kakao.com/oauth/authorize<br>https://kauth.kakao.com/oauth/token | `{baseUrl}/api/login/oauth2/code/kakao` | 카카오 계정 로그인 |

- Callback URL은 FE/BE 도메인 설정에 따라 달라질 수 있으며, 반드시 각 플랫폼 개발자 콘솔에 등록 필요
- Client ID / Client Secret 값은 플랫폼별 애플리케이션 생성 후 발급받아야 함

---

## 2. 코드 저장소 및 외부 API

#### GitHub API (OAuth2 인증)

Codaily 프로젝트는 GitHub API를 활용하여 **레포지토리 생성/삭제, 커밋 조회, Webhook 이벤트 처리**를 수행합니다.  
이를 위해 GitHub OAuth2 인증을 거쳐 액세스 토큰을 발급받습니다.

##### 인증 요청 URL 형식
```text
https://github.com/login/oauth/authorize
  ?client_id=${GITHUB_CLIENT_ID}
  &scope=repo,user,admin:repo_hook
  &redirect_uri=${import.meta.env.VITE_GITHUB_REDIRECT_URI}
```

---

## 3. AI 모델 / 외부 AI 서비스

본 프로젝트의 AI 모듈은 **LangChain + LangGraph 오케스트레이션** 기반으로 동작하며, 실제 모델 호출은 **GMS API**를 사용합니다.

- **LangChain**
  - 역할: 프롬프트 체인 관리, LLM 호출 추상화
- **LangGraph**
  - 역할: 병렬/비동기 작업 처리, 코드 리뷰 오케스트레이션
- **GMS API**
  - Codaily AI 기능의 핵심 모델 API
  - 요구사항 명세서 생성, 코드 리뷰, 회고 리포트 자동 생성에 활용
- **모델 키 및 url 관리**
  - `AI/.env` 파일에 API KEY 보관 (`OPENAI_API_KEY`, `OPENAI_API_BASE`)
  - 운영 시 Secret Manager 또는 환경변수로 관리 권장

---

## 4. 인프라 / 배포 환경

| 서비스 | 사용 목적 | 비고 |
|--------|-----------|------|
| **AWS EC2 (Ubuntu)** | 애플리케이션 서버 배포 | Public IP 기반 |
| **Nginx** | Reverse Proxy, SSL, 라우팅(`/api`, `/ai`) | FE/BE/AI 트래픽 분배 |
| **Docker & Docker Compose** | FE, BE, AI, DB 컨테이너화 | CI/CD 및 로컬 실행 환경 통일 |
| **Jenkins** | CI/CD 파이프라인, GitHub Webhook 기반 자동 빌드/배포 | FE/BE/AI 재배포 자동화 |

---

## 5. 데이터베이스

- **PostgreSQL**
  - 메인 RDBMS
  - 버전: 17.5
  - 연결 정보는 BE 폴더의 `application.yml` 파일에서 관리


---

# 요약

Codaily 프로젝트는 다음과 같은 외부 서비스를 사용합니다:
- **소셜 로그인**: Google, Naver, Kakao  
- **코드 저장소/연동**: GitHub API (레포 생성·삭제·Webhook 이벤트)  
- **AI 서비스**: LangChain, LangGraph, **GMS API**  
- **인프라**: AWS EC2, Nginx, Docker, Jenkins  
- **DB**: PostgreSQL
