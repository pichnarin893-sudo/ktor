FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY common-core ./common-core
COPY order-service ./order-service

# Build fat JAR
RUN gradle :order-service:buildFatJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/order-service/build/libs/order-service-all.jar ./order-service.jar

# Expose port
EXPOSE 8083

# Run the application
CMD ["java", "-jar", "order-service.jar"]
