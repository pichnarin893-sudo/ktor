package com.example.plugins

import com.example.api.routes.userRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Welcome to Ktor Microservice!")
        }
        
        route("/api/v1") {
            userRoutes()
            // Add more routes here
        }
    }
}