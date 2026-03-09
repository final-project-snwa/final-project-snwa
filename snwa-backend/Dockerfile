# 1. 공통 실행 환경
FROM eclipse-temurin:21-jre AS base
WORKDIR /app

EXPOSE 8080
# 한국 시간 설정
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
    apt-get install -y tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    rm -rf /var/lib/apt/lists/*

# 2. 로컬 빌드용 환경
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x gradlew && ./gradlew bootJar

# 3. [로컬용] dev - 위에서 빌드한 jar 사용
FROM base AS dev
WORKDIR /app

ARG JAR_FILE=build/libs/snwa-backend-0.0.1-SNAPSHOT.jar
#ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /app/${JAR_FILE} app.jar

EXPOSE 8080
CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]

# 4. [배포용] prod - GitHub Actions가 보낸 jar 사용
FROM base AS prod
WORKDIR /app

COPY app.jar app.jar

EXPOSE 8080
CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]