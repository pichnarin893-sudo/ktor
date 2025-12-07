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
 * Protected seller routes (requires seller-jwt authentication)
 */
fun Route.sellerRoutes() {
    val authService by inject<AuthService>()
    val userService by inject<UserService>()

    authenticate("seller-jwt") {
        route("/v1/seller/auth") {
            /**
             * Get seller profile
             * GET /v1/seller/auth/profile
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
             * Update seller profile
             * PUT /v1/seller/auth/profile
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
             * Get seller dashboard statistics
             * GET /v1/seller/auth/dashboard
             */
            get("/dashboard") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.getUserId())

                val dashboard = userService.getSellerDashboard(userId)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = dashboard,
                        message = "Dashboard retrieved successfully"
                    )
                )
            }

            /**
             * Logout seller
             * POST /v1/seller/auth/logout
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
