FROM openjdk:8-alpine3.8

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT [ "java", "-jar", "app.jar" ]