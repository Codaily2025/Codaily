FROM openjdk:17-jdk-slim

WORKDIR /app

# Gradle 산출물 복사 (Maven이면 아래 주석 라인으로 변경)
COPY build/libs/*.jar app.jar
# COPY target/*.jar app.jar

EXPOSE 8081

# 실행은 고정, 설정 주입은 런타임에서 환경변수로
ENTRYPOINT ["java","-jar","/app.jar","--server.port=8081"]
