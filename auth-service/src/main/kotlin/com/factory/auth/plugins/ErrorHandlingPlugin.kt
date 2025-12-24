package com.factory.auth.plugins

import com.factory.auth.exceptions.*
import com.factory.common.dto.ApiResponse
import com.factory.common.dto.ErrorDetail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

/**
 * Configure centralized error handling using StatusPages plugin
 */
fun Application.configureErrorHandling() {
    install(StatusPages) {
        // Handle authentication exceptions
        exception<AuthException> { call, cause ->
            call.respond(
                HttpStatusCode.fromValue(cause.statusCode),
                ApiResponse<Nothing>(
                    success = false,
                    data = null,
                    message = cause.message,
                    error = ErrorDetail(
                        code = cause.errorCode,
                        message = cause.message ?: "An error occurred"
                    )
                )
            )
        }

        // Handle validation exceptions with field details
        exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(
                    success = false,
                    data = null,
                    message = cause.message,
                    error = ErrorDetail(
                        code = cause.errorCode,
                        message = cause.message ?: "Validation failed",
                        details = cause.details
                    )
                )
            )
        }

        // Handle generic exceptions
        exception<Exception> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)

            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(
                    success = false,
                    data = null,
                    message = "An internal server error occurred",
                    error = ErrorDetail(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "An unexpected error occurred. Please try again later."
                    )
                )
            )
        }

        // Handle 400 Bad Request
        status(HttpStatusCode.BadRequest) { call, status ->
            call.respond(
                status,
                ApiResponse<Nothing>(
                    success = false,
                    data = null,
                    message = "Bad request",
                    error = ErrorDetail(
                        code = "BAD_REQUEST",
                        message = "The request was invalid or malformed"
                    )
                )
            )
        }

        // Handle 404 Not Found
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ApiResponse<Nothing>(
                    success = false,
                    data = null,
                    message = "Resource not found",
                    error = ErrorDetail(
                        code = "NOT_FOUND",
                        message = "The requested resource was not found"
                    )
                )
            )
        }

        // Handle 405 Method Not Allowed
        status(HttpStatusCode.MethodNotAllowed) { call, status ->
            call.respond(
                status,
                ApiResponse<Nothing>(
                    success = false,
                    data = null,
                    message = "Method not allowed",
                    error = ErrorDetail(
                        code = "METHOD_NOT_ALLOWED",
                        message = "The HTTP method is not allowed for this endpoint"
                    )
                )
            )
        }
    }
}
