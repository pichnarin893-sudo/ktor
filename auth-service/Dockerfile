# Build stage
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy project files
COPY settings.gradle.kts gradle.properties ./
COPY build.gradle.kts ./
COPY common-core common-core
COPY auth-service auth-service

# Build the auth service fat JAR
RUN gradle :auth-service:buildFatJar --no-daemon

# Verify migration files are included
RUN ls -la /app/auth-service/build/libs/

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy fat JAR from build stage
COPY --from=build /app/auth-service/build/libs/auth-service-all.jar app.jar

# Create logs directory
RUN mkdir -p logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port 8081
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
