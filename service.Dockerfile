FROM eclipse-temurin:21-jre

WORKDIR /app

ARG MODULE
ARG PORT

COPY ${MODULE}/target/${MODULE}-0.0.1-SNAPSHOT.jar app.jar

EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
