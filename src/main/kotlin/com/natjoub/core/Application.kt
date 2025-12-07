package com.natjoub.core

import com.natjoub.auth.di.appModule
import com.natjoub.core.config.DatabaseFactory
import com.natjoub.core.config.loadConfiguration
import com.natjoub.core.plugins.*
import com.natjoub.inventory.di.inventoryModule
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import com.natjoub.auth.plugins.configureAuth

/**
 * Main entry point for the NatJoub Microservices Application
 * This is the shared application core that bootstraps all services
 */
fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

/**
 * Application module configuration
 * Configures shared infrastructure and registers all service modules
 */
fun Application.module() {
    // Load configuration
    val config = loadConfiguration()

    // Initialize database (shared connection pool for all services)
    DatabaseFactory.init(config.database)

    // Setup Koin dependency injection with all service modules
    install(Koin) {
        slf4jLogger()
        modules(
            appModule,        // Auth service module
            inventoryModule   // Inventory service module
        )
    }

    // Configure shared plugins
    configureSerialization()
    configureCORS()
    configureCallLogging()
    configureRateLimit()
    configureErrorHandling()
    configureMonitoring()  // Prometheus metrics and monitoring

    // Configure service-specific plugins
    configureAuth()  // Auth service authentication

    // Configure routing (registers all service routes)
    configureRouting()

    // Log startup
    environment.log.info("NatJoub Microservices Application started successfully")
    environment.log.info("Server running on port: ${environment.config.port}")
    environment.log.info("Protheus monitoring available on port: 9090")
    environment.log.info("Gafana dashboard available on port: 3000")
    environment.log.info("Database connected at: ${config.database.url}")
    environment.log.info("JWT Issuer: ${config.jwt.issuer}")
    environment.log.info("Services loaded: Auth, Inventory")
}
