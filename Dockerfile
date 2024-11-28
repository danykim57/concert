# Base image
FROM openjdk:17-jdk-slim
WORKDIR /app
ADD build/libs/concert-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]