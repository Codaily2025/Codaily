FROM openjdk:17-jdk-slim

WORKDIR /app

# JAR_FILE은 Jenkins에서 전달받음
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app.jar","--server.port=8081"]
