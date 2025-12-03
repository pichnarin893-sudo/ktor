package com.example.config

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

data class AppConfig(
    val database: DatabaseConfig,
    val jwt: JwtConfig,
    val app: ApplicationConfig
)

data class DatabaseConfig(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int
)

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expirationTime: Long
)

data class ApplicationConfig(
    val environment: String,
    val enableSwagger: Boolean
)

fun loadConfiguration(): AppConfig {
    val config = HoconApplicationConfig(ConfigFactory.load())
    
    return AppConfig(
        database = DatabaseConfig(
            url = config.property("database.url").getString(),
            driver = config.property("database.driver").getString(),
            user = config.property("database.user").getString(),
            password = config.property("database.password").getString(),
            maxPoolSize = config.property("database.maxPoolSize").getString().toInt()
        ),
        jwt = JwtConfig(
            secret = config.property("jwt.secret").getString(),
            issuer = config.property("jwt.issuer").getString(),
            audience = config.property("jwt.audience").getString(),
            realm = config.property("jwt.realm").getString(),
            expirationTime = config.property("jwt.expirationTime").getString().toLong()
        ),
        app = ApplicationConfig(
            environment = config.property("app.environment").getString(),
            enableSwagger = config.property("app.enableSwagger").getString().toBoolean()
        )
    )
}