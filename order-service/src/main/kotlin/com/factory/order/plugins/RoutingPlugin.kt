package com.factory.order.plugins

import com.factory.order.controllers.customerOrderRoutes
import com.factory.order.controllers.employeeOrderRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Order Service is running")
        }

        get("/health") {
            call.respondText("OK")
        }

        // Order routes
        employeeOrderRoutes()
        customerOrderRoutes()
    }
}
