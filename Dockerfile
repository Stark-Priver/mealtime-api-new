FROM ubuntu:latest AS build

# Update package list and install OpenJDK 17
RUN apt-get update && apt-get install openjdk-17-jdk -y

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Make gradlew executable and run build
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

FROM openjdk:17-jdk-slim

EXPOSE 8080

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/mealtime-api-0.0.1.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]