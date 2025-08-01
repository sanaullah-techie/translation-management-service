FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/translation-management-service-0.0.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]