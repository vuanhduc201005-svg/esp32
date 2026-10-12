FROM amazoncorretto:17.0.17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} common-service.jar
ENTRYPOINT ["java", "-jar", "common-service.jar"]
EXPOSE 8763