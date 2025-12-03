package com.example

import com.example.config.*
import com.example.di.appModule
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Load configuration
    val config = loadConfiguration()
    
    // Initialize database
    DatabaseFactory.init(config.database)
    
    // Setup Koin DI
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    
    // Configure plugins
    configureMonitoring()
    configureSerialization()
    configureHTTP()
    configureSecurity(config.jwt)
    configureStatusPages()
    configureValidation()
    configureRouting()
    
    // Health check
    configureHealthCheck()
}