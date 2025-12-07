package com.natjoub.inventory

import com.natjoub.inventory.config.DatabaseFactory
import com.natjoub.inventory.config.loadConfiguration
import com.natjoub.inventory.di.inventoryModule
import com.natjoub.inventory.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/**
 * Main entry point for the Inventory Service
 */
fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8082,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

/**
 * Application module configuration for Inventory Service
 */
fun Application.module() {
    // Load configuration
    val config = loadConfiguration()

    // Initialize inventory database (inventory_schema only)
    DatabaseFactory.init(config.database)

    // Setup Koin dependency injection with inventory module only
    install(Koin) {
        slf4jLogger()
        modules(inventoryModule)
    }

    // Configure shared plugins
    configureSerialization()
    configureCORS()
    configureCallLogging()
    configureErrorHandling()  // Inventory-specific error handling
    configureMonitoring()     // Prometheus metrics
    // Rate limiting removed for inventory service (not critical for this microservice)

    // Configure auth plugin (JWT validation only, no blacklist check)
    configureAuth()

    // Configure routing (inventory routes only)
    configureRouting()

    // Log startup
    environment.log.info("Inventory Service started successfully")
    environment.log.info("Server running on port: ${environment.config.port}")
    environment.log.info("Database connected at: ${config.database.url}")
    environment.log.info("Auth Service URL: ${config.authServiceUrl}")
}
