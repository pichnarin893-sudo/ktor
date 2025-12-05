package com.natjoub.auth.models.dto

import kotlinx.serialization.Serializable
import java.util.*

/**
 * Request DTOs
 */

@Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val username: String? = null,
    val phoneNumber: String? = null,
    val password: String,
    val role: String,
    val dob: String? = null,
    val gender: String? = null
)

@Serializable
data class LoginRequest(
    val identifier: String, // Can be email, username, or phone number
    val password: String
)

@Serializable
data class VerifyOTPRequest(
    val identifier: String,
    val otp: String
)

@Serializable
data class ResendOTPRequest(
    val identifier: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val username: String? = null,
    val phoneNumber: String? = null
)

@Serializable
data class UpdateUserStatusRequest(
    val isActive: Boolean
)

/**
 * Response DTOs
 */

@Serializable
data class UserDTO(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val username: String? = null,
    val phoneNumber: String? = null,
    val role: String,
    val dob: String? = null,
    val gender: String? = null,
    val isActive: Boolean,
    val isVerified: Boolean,
    val createdAt: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDTO,
    val expiresIn: Long // seconds
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

@Serializable
data class ProfileResponse(
    val user: UserDTO
)

@Serializable
data class MessageResponse(
    val message: String
)

@Serializable
data class UserListResponse(
    val users: List<UserDTO>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

@Serializable
data class DashboardStatsResponse(
    val totalBookings: Int,
    val activeListings: Int,
    val revenue: Double,
    val pendingOrders: Int
)

/**
 * Generic API Response wrapper
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: ErrorDetail? = null
)

@Serializable
data class ErrorDetail(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)
