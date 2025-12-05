package com.natjoub.auth.controllers

import com.natjoub.auth.models.dto.*
import com.natjoub.auth.plugins.getUserId
import com.natjoub.auth.services.AuthService
import com.natjoub.auth.services.UserService
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
 * Protected customer routes (requires customer-jwt authentication)
 */
fun Route.customerRoutes() {
    val authService by inject<AuthService>()
    val userService by inject<UserService>()

    authenticate("customer-jwt") {
        route("/v1/customer/auth") {
            /**
             * Get customer profile
             * GET /v1/customer/auth/profile
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
             * Update customer profile
             * PUT /v1/customer/auth/profile
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
             * Get customer bookings
             * GET /v1/customer/auth/bookings
             * TODO: Implement booking integration
             */
            get("/bookings") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.getUserId())

                // TODO: Integrate with booking service
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = mapOf("bookings" to emptyList<Any>(), "total" to 0),
                        message = "Bookings retrieved successfully"
                    )
                )
            }

            /**
             * Logout customer
             * POST /v1/customer/auth/logout
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
