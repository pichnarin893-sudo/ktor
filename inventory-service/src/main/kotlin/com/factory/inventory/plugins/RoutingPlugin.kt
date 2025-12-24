package com.factory.inventory.plugins

import com.factory.inventory.controllers.inventoryRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configure routing for Inventory Service
 */
fun Application.configureRouting() {
    routing {
        // Health check endpoint
        get("/") {
            call.respondText("Inventory Service is running")
        }

        get("/health") {
            call.respondText("OK")
        }

        // Inventory service routes
        inventoryRoutes()
    }
}
