FROM openjdk:17-jdk-slim
WORKDIR /app

# JAR 파일 하나만 복사할 경우 (가장 최신 SNAPSHOT만)
COPY build/libs/codaily-0.0.1-SNAPSHOT.jar app.jar

# 여러 개를 전부 복사하고 싶으면 디렉토리 지정
# COPY build/libs/*.jar /app/

ENTRYPOINT ["sh", "-c", "java -jar *.jar"]


# 애플리케이션 포트 (필요 시 변경)
EXPOSE 8081
