# Multi-stage build for production
FROM maven:3.9.4-openjdk-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app

# Create non-root user
RUN addgroup --system vault && adduser --system --group vault

# Copy application jar
COPY --from=build /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R vault:vault /app

USER vault

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]