package com.factory.inventory.config

import com.factory.common.config.DatabaseConfig
import com.factory.common.config.JWTConfig
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

/**
 * Application configuration for Inventory Service
 */
data class AppConfig(
    val database: DatabaseConfig,
    val jwt: JWTConfig,
    val authServiceUrl: String
)

/**
 * Load configuration from application.conf and environment variables
 */
fun loadConfiguration(): AppConfig {
    val config = HoconApplicationConfig(ConfigFactory.load())

    val databaseConfig = DatabaseConfig(
        url = System.getenv("DATABASE_URL")
            ?: config.tryGetString("database.url")
            ?: "jdbc:postgresql://localhost:5432/microservice_db",
        driver = config.tryGetString("database.driver") ?: "org.postgresql.Driver",
        user = System.getenv("DATABASE_USER")
            ?: config.tryGetString("database.user")
            ?: "postgres",
        password = System.getenv("DATABASE_PASSWORD")
            ?: config.tryGetString("database.password")
            ?: "postgres",
        maxPoolSize = System.getenv("DATABASE_MAX_POOL_SIZE")?.toIntOrNull()
            ?: config.tryGetString("database.maxPoolSize")?.toIntOrNull()
            ?: 10  // Smaller pool for individual service
    )

    val jwtConfig = JWTConfig(
        secret = System.getenv("JWT_SECRET")
            ?: config.tryGetString("jwt.secret")
            ?: "default-secret-change-in-production",
        issuer = System.getenv("JWT_ISSUER")
            ?: config.tryGetString("jwt.issuer")
            ?: "http://localhost:8081/",  // Points to Auth service
        audience = System.getenv("JWT_AUDIENCE")
            ?: config.tryGetString("jwt.audience")
            ?: "http://localhost:8081/api",  // Points to Auth service
        realm = config.tryGetString("jwt.realm") ?: "NatJoub Inventory Service"
    )

    val authServiceUrl = System.getenv("AUTH_SERVICE_URL")
        ?: config.tryGetString("authService.url")
        ?: "http://localhost:8081"

    return AppConfig(databaseConfig, jwtConfig, authServiceUrl)
}
