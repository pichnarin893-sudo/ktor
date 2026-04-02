# Build stage
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy project files
COPY settings.gradle.kts gradle.properties ./
COPY build.gradle.kts ./
COPY common-core common-core
COPY inventory-service inventory-service

# Build the inventory service fat JAR
RUN gradle :inventory-service:buildFatJar --no-daemon

# Verify migration files are included
RUN ls -la /app/inventory-service/build/libs/

# Runtime stage
FROM eclipse-temurin:25-jre
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy fat JAR from build stage
COPY --from=build /app/inventory-service/build/libs/inventory-service-all.jar app.jar

# Create logs directory
RUN mkdir -p logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port 8082
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
