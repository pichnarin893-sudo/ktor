FROM gradle:9.2-jdk21 AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY telegram-bot-service ./telegram-bot-service

# Build fat JAR
RUN gradle :telegram-bot-service:fatJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/telegram-bot-service/build/libs/telegram-bot-service-all.jar ./telegram-bot.jar

# Run the application
CMD ["java", "-jar", "telegram-bot.jar"]
