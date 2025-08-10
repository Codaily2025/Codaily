FROM openjdk:17-jdk-slim

WORKDIR /app

# Jenkins에서 넘기는 정확한 JAR 경로를 받는다
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

EXPOSE 8081

# JAR의 전체 경로를 정확히 지정
ENTRYPOINT ["java","-jar","/app/app.jar","--server.port=8081"]
