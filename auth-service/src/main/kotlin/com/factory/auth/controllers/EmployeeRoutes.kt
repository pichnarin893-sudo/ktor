package com.factory.auth.controllers

import com.factory.auth.models.dto.*
import com.factory.auth.plugins.getUserId
import com.factory.auth.services.AuthService
import com.factory.auth.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

/**
 * Protected employee routes (requires employee-jwt authentication)
 * Employees have full access to manage users, inventory, and orders
 */
fun Route.employeeRoutes() {
    val authService by inject<AuthService>()
    val userService by inject<UserService>()

    authenticate("employee-jwt") {
        route("/v1/employee/auth") {
            /**
             * Get employee profile
             * GET /v1/employee/auth/profile
             */
            get("/profile") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.getUserId())

                val profile = userService.getProfile(userId)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = ProfileResponse(profile),
                        message = "Profile retrieved successfully"
                    )
                )
            }

            /**
             * Update employee profile
             * PUT /v1/employee/auth/profile
             */
            put("/profile") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.getUserId())
                val request = call.receive<UpdateProfileRequest>()

                val updatedProfile = userService.updateProfile(userId, request)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = ProfileResponse(updatedProfile),
                        message = "Profile updated successfully"
                    )
                )
            }

            /**
             * Get all users with optional role filtering
             * GET /v1/employee/auth/users?role=customer&limit=10&offset=0
             */
            get("/users") {
                val roleFilter = call.request.queryParameters["role"]
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                val users = userService.getAllUsers(roleFilter, limit, offset)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = users,
                        message = "Users retrieved successfully"
                    )
                )
            }

            /**
             * Get user by ID
             * GET /v1/employee/auth/users/{id}
             */
            get("/users/{id}") {
                val userId = UUID.fromString(call.parameters["id"])
                val profile = userService.getProfile(userId)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = ProfileResponse(profile),
                        message = "User retrieved successfully"
                    )
                )
            }

            /**
             * Update user status (activate/deactivate)
             * PUT /v1/employee/auth/users/{id}/status
             */
            put("/users/{id}/status") {
                val userId = UUID.fromString(call.parameters["id"])
                val request = call.receive<UpdateUserStatusRequest>()

                val updatedUser = userService.updateUserStatus(userId, request.isActive)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = ProfileResponse(updatedUser),
                        message = "User status updated successfully"
                    )
                )
            }

            /**
             * Delete user
             * DELETE /v1/employee/auth/users/{id}
             */
            delete("/users/{id}") {
                val userId = UUID.fromString(call.parameters["id"])

                val response = userService.deleteUser(userId)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = response,
                        message = "User deleted successfully"
                    )
                )
            }

            /**
             * Logout employee
             * POST /v1/employee/auth/logout
             */
            post("/logout") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.getUserId())
                val token = call.request.headers["Authorization"]?.removePrefix("Bearer ") ?: ""

                authService.logout(token, userId)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = MessageResponse("Logged out successfully"),
                        message = "Logout successful"
                    )
                )
            }
        }
    }
}
