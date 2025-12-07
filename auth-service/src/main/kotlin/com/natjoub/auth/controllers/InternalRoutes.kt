package com.natjoub.auth.controllers

import com.natjoub.auth.services.UserService
import com.natjoub.common.dto.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.util.UUID

/**
 * Internal API routes for service-to-service communication
 * These endpoints are called by other microservices (e.g., Inventory service)
 */
fun Route.internalRoutes() {
    val userService by inject<UserService>()

    route("/api/v1/internal") {
        authenticate {
            // Validate user endpoint - used by Inventory service to validate performed_by field
            get("/users/{userId}/validate") {
                val userId = call.parameters["userId"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Nothing>(
                            success = false,
                            message = "Missing userId parameter",
                            data = null
                        )
                    )

                try {
                    val uuid = UUID.fromString(userId)
                    val user = userService.getProfile(uuid)

                    if (user.isActive) {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse(
                                success = true,
                                data = UserValidationResponse(
                                    valid = true,
                                    userId = userId,
                                    role = user.role
                                )
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse(
                                success = true,
                                data = UserValidationResponse(
                                    valid = false,
                                    userId = userId,
                                    message = "User is inactive"
                                )
                            )
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Nothing>(
                            success = false,
                            message = "Invalid userId format",
                            data = null
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Nothing>(
                            success = false,
                            message = "Internal server error: ${e.message}",
                            data = null
                        )
                    )
                }
            }
        }
    }
}

@Serializable
data class UserValidationResponse(
    val valid: Boolean,
    val userId: String,
    val role: String? = null,
    val message: String? = null
)
