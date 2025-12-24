package com.factory.auth.controllers

import com.factory.auth.models.dto.*
import com.factory.auth.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Public authentication routes (no authentication required)
 */
fun Route.publicAuthRoutes() {
    val authService by inject<AuthService>()

    route("/v1/auth") {
        /**
         * Register a new user (seller or customer)
         * POST /v1/auth/register
         */
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = authService.register(request)

            call.respond(
                HttpStatusCode.Created,
                ApiResponse(
                    success = true,
                    data = response,
                    message = "User registered successfully. Please verify your account with the OTP sent to your email."
                )
            )
        }

        /**
         * Login with email/username/phone and password
         * POST /v1/auth/login
         */
        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authService.login(request)

            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    data = response,
                    message = "Login successful"
                )
            )
        }

        /**
         * Verify OTP for account verification
         * POST /v1/auth/verify-otp
         */
        post("/verify-otp") {
            val request = call.receive<VerifyOTPRequest>()
            val response = authService.verifyOTP(request)

            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    data = response,
                    message = "OTP verified successfully"
                )
            )
        }

        /**
         * Resend OTP to user
         * POST /v1/auth/resend-otp
         */
        post("/resend-otp") {
            val request = call.receive<ResendOTPRequest>()
            val response = authService.resendOTP(request)

            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    data = response,
                    message = "OTP sent successfully"
                )
            )
        }

        /**
         * Request password reset token
         * POST /v1/auth/forgot-password
         * TODO: Implement password reset functionality
         */
        post("/forgot-password") {
            val request = call.receive<ForgotPasswordRequest>()

            // TODO: Implement forgot password logic
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    data = MessageResponse("Password reset instructions sent to your email"),
                    message = "Password reset email sent"
                )
            )
        }

        /**
         * Reset password using token
         * POST /v1/auth/reset-password
         * TODO: Implement password reset functionality
         */
        post("/reset-password") {
            val request = call.receive<ResetPasswordRequest>()

            // TODO: Implement reset password logic
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    data = MessageResponse("Password reset successful"),
                    message = "Password has been reset successfully"
                )
            )
        }

        /**
         * Refresh access token using refresh token
         * POST /v1/auth/refresh-token
         */
        post("/refresh-token") {
            val request = call.receive<RefreshTokenRequest>()
            val response = authService.refreshToken(request)

            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    data = response,
                    message = "Token refreshed successfully"
                )
            )
        }
    }
}
