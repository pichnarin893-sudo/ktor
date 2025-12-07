package com.natjoub.auth.plugins

import com.natjoub.auth.controllers.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configure routing for Auth Service
 */
fun Application.configureRouting() {
    routing {
        // Health check endpoint
        get("/") {
            call.respondText("Auth Service is running")
        }

        get("/health") {
            call.respondText("OK")
        }

        // Auth service routes
        publicAuthRoutes()
        adminRoutes()
        sellerRoutes()
        customerRoutes()

        // Internal service-to-service routes
        internalRoutes()
    }
}
