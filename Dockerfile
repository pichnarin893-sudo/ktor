# Build stage
FROM gradle:9.2-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle gradle

# Copy source code INCLUDING resources
COPY src ./src

# Build the application
RUN gradle clean buildFatJar --no-daemon

# List JAR contents to verify migration is included (for debugging)
RUN jar tf build/libs/app-all.jar | grep migration || echo "WARNING: No migration files found!"

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the built jar
COPY --from=build /app/build/libs/app-all.jar app.jar

# Verify migration is in the copied JAR
RUN jar tf app.jar | grep migration || echo "ERROR: Migration not in final JAR!"

# Create logs directory
RUN mkdir -p logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]