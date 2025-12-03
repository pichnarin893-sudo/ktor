package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val checks: Map<String, String>
)

fun Application.configureHealthCheck() {
    routing {
        get("/health") {
            val checks = mutableMapOf<String, String>()

            // Database health check
            checks["database"] = try {
                transaction {
                    // Simple query to check database connectivity
                    exec("SELECT 1") {}
                }
                "healthy"
            } catch (e: Exception) {
                "unhealthy: ${e.message}"
            }

            val isHealthy = checks.values.all { it == "healthy" }

            call.respond(
                if (isHealthy) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable,
                HealthResponse(
                    status = if (isHealthy) "UP" else "DOWN",
                    checks = checks
                )
            )
        }

        get("/health/ready") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ready"))
        }

        get("/health/live") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "alive"))
        }
    }
}