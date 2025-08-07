# 1. JDK 17 slim 버전 베이스 이미지
FROM openjdk:17-jdk-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드 결과물 복사 (가장 최신 jar 파일)
COPY build/libs/*.jar app.jar

# 4. Spring Boot 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

# 5. 외부에 열 포트 (Swagger UI 등 접근 가능)
EXPOSE 8081
