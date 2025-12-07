package com.natjoub.core.plugins

import com.natjoub.auth.controllers.*
import com.natjoub.inventory.controllers.inventoryRoutes
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
            call.respondText("NatJoub Microservices is running")
        }

        get("/health") {
            call.respondText("OK")
        }

        // Auth service routes
        publicAuthRoutes()
        adminRoutes()
        sellerRoutes()
        customerRoutes()

        // Inventory service routes
        inventoryRoutes()
    }
}
