package com.factory.auth.models.entity

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Role enumeration
 */
enum class RoleType(val value: String) {
    EMPLOYEE("employee"),
    CUSTOMER("customer");
//    STAFF("staff");

    companion object {
        fun fromString(value: String): RoleType? {
            return values().find { it.value.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Gender enumeration
 */
enum class Gender(val value: String) {
    MALE("male"),
    FEMALE("female"),
    OTHER("other");

    companion object {
        fun fromString(value: String): Gender? {
            return values().find { it.value.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Role entity
 */
data class Role(
    val id: UUID,
    val name: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * User entity
 */
data class User(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val dob: LocalDate?,
    val gender: String?,
    val roleId: UUID,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Credential entity
 */
data class Credential(
    val id: UUID,
    val userId: UUID,
    val email: String,
    val username: String?,
    val phoneNumber: String?,
    val password: String,
    val otp: String?,
    val otpExpiry: LocalDateTime?,
    val isVerified: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Refresh token entity
 */
data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val token: String,
    val expiresAt: LocalDateTime,
    val isRevoked: Boolean,
    val createdAt: LocalDateTime
)

/**
 * Token blacklist entity
 */
data class BlacklistedToken(
    val id: UUID,
    val token: String,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime
)

/**
 * Complete user with credentials and role
 */
data class UserWithCredentialsAndRole(
    val user: User,
    val credential: Credential,
    val role: Role
)
