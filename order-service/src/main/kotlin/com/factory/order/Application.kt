package com.factory.order

import com.factory.order.config.DatabaseFactory
import com.factory.order.di.orderModule
import com.factory.order.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // Initialize Koin DI
    install(Koin) {
        slf4jLogger()
        modules(orderModule)
    }

    // Initialize database
    DatabaseFactory.init()

    // Configure plugins
    configureSerialization()
    configureAuth()
    configureCORS()
    configureErrorHandling()
    configureCallLogging()
    configureMonitoring()
    configureRouting()
}
