# 1. 베이스 이미지 선택 (Java 17 실행 환경)
FROM openjdk:17-jdk-slim

# 2. 빌드된 JAR 파일의 경로를 변수로 지정
ARG JAR_FILE=build/libs/*.jar

# 3. JAR 파일을 컨테이너 내부로 복사하고 이름을 app.jar로 변경
COPY ${JAR_FILE} app.jar

# 4. 컨테이너가 시작될 때 실행할 명령어 지정
ENTRYPOINT ["java","-jar","/app.jar"]