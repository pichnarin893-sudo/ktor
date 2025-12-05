package com.natjoub.auth.plugins

import com.natjoub.auth.controllers.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configure routing and register all routes
 */
fun Application.configureRouting() {
    routing {
        // Health check endpoint
        get("/") {
            call.respondText("NatJoub Auth Service is running")
        }

        get("/health") {
            call.respondText("OK")
        }

        // Public auth routes
        publicAuthRoutes()

        // Protected role-specific routes
        adminRoutes()
        sellerRoutes()
        customerRoutes()
    }
}
