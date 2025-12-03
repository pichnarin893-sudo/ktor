package com.example.plugins

import com.example.exception.NotFoundException
import com.example.exception.ValidationException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StatusPages")

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "Internal Server Error",
                    message = cause.message ?: "An unexpected error occurred"
                )
            )
        }
        
        exception<NotFoundException> { call, cause ->
            logger.warn("Resource not found: ${cause.message}")
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "Not Found",
                    message = cause.message ?: "Resource not found"
                )
            )
        }
        
        exception<ValidationException> { call, cause ->
            logger.warn("Validation error: ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Validation Error",
                    message = cause.message ?: "Invalid input"
                )
            )
        }
        
        exception<UnauthorizedException> { call, cause ->
            logger.warn("Unauthorized access: ${cause.message}")
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    error = "Unauthorized",
                    message = cause.message ?: "Authentication required"
                )
            )
        }
        
        exception<IllegalArgumentException> { call, cause ->
            logger.warn("Bad request: ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = cause.message ?: "Invalid request"
                )
            )
        }
    }
}