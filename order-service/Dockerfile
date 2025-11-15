FROM openjdk:21-jdk-slim

WORKDIR /app

COPY gradlew /app/
COPY gradle /app/gradle
COPY build.gradle /app/
COPY settings.gradle /app/
COPY src /app/src

RUN chmod +x gradlew

RUN ./gradlew clean build -x test

EXPOSE 8082

CMD ["java", "-jar", "build/libs/order-service-0.0.1-SNAPSHOT.jar"]