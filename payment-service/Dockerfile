FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /application
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /application
COPY --from=builder /application/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]