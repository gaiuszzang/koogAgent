FROM eclipse-temurin:21-jre
# 작업 디렉토리 지정
WORKDIR /app
# 파일 복사
COPY build/libs/koogAgent.jar app.jar
COPY config.json config.json
# 포트 설정
EXPOSE 8080
# 실행
ENTRYPOINT ["java", "-jar", "app.jar", "--mode=server"]
