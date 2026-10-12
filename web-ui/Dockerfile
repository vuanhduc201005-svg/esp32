FROM amazoncorretto:17.0.17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} web-ui.jar
ENTRYPOINT ["java", "-jar", "web-ui.jar"]
EXPOSE 2509