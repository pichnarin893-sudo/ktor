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
 * Main entry point for the NatJoub Auth Service
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
 */
fun Application.module() {
    // Load configuration
    val config = loadConfiguration()

    // Initialize database
    DatabaseFactory.init(config.database)

    // Setup Koin dependency injection
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    // Configure plugins
    configureSerialization()
    configureCORS()
    configureCallLogging()
    configureRateLimit()
    configureErrorHandling()
    configureAuth()
    configureRouting()

    // Log startup
    environment.log.info("NatJoub Auth Service started successfully")
    environment.log.info("Server running on port: ${environment.config.port}")
}
