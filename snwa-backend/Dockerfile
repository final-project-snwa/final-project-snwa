# --- 로컬 테스트용 (주석 해제) ---
# .jar 파일 생성
#FROM eclipse-temurin:21-jdk AS build
#WORKDIR /app
#
#COPY gradlew ./
#COPY gradle ./gradle
#COPY build.gradle settings.gradle ./
#COPY src ./src
#
#RUN chmod +x gradlew
#RUN ./gradlew bootJar
#RUN rm -f build/libs/*.jar && ./gradlew bootJar

# .jar 실행
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#ARG JAR_FILE=build/libs/snwa-backend-0.0.1-SNAPSHOT.jar
#COPY --from=build /app/${JAR_FILE} app.jar
#EXPOSE 8080
#CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]

#jdk는 빌드&테스트 할 때 사용 , 실행만 하면 될때는 jre
# --- 운영 배포용 (로컬 테스트 시 주석 처리) ---
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY app.jar app.jar

EXPOSE 8080

CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]

#도커는 기본적으로 utc를 사용하기 때문에 한국 시간에 맞추려면 아래 코드를 적용 시켜야합니다!!
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get install -y tzdata
RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime