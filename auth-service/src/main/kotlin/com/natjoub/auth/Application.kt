package com.natjoub.auth

import com.natjoub.auth.config.DatabaseFactory
import com.natjoub.auth.config.loadConfiguration
import com.natjoub.auth.di.appModule
import com.natjoub.auth.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/**
 * Main entry point for the Auth Service
 */
fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8081,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

/**
 * Application module configuration for Auth Service
 */
fun Application.module() {
    // Load configuration
    val config = loadConfiguration()

    // Initialize auth database (auth_schema only)
    DatabaseFactory.init(config.database)

    // Setup Koin dependency injection with auth module only
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    // Configure shared plugins
    configureSerialization()
    configureCORS()
    configureCallLogging()
    configureRateLimit()
    configureErrorHandling()  // Auth-specific error handling
    configureMonitoring()     // Prometheus metrics

    // Configure auth-specific plugins
    configureAuth()  // JWT authentication

    // Configure routing (auth routes only)
    configureRouting()

    // Log startup
    environment.log.info("Auth Service started successfully")
    environment.log.info("Server running on port: ${environment.config.port}")
    environment.log.info("Database connected at: ${config.database.url}")
    environment.log.info("JWT Issuer: ${config.jwt.issuer}")
}
