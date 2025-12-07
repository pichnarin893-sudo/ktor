package com.natjoub.common.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

/**
 * Allowed roles for authorization
 */
enum class Role {
    ADMIN,
    SELLER,
    CUSTOMER;

    companion object {
        fun fromString(role: String): Role? {
            return try {
                valueOf(role.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}

/**
 * Authorization helper class
 */
object Authorization {

    /**
     * Extract the role from JWT principal
     */
    fun ApplicationCall.getUserRole(): String? {
        val principal = principal<JWTPrincipal>()
        return principal?.payload?.getClaim("role")?.asString()
    }

    /**
     * Extract the user ID from JWT principal
     */
    fun ApplicationCall.getUserId(): String? {
        val principal = principal<JWTPrincipal>()
        return principal?.payload?.getClaim("user_id")?.asString()
    }

    /**
     * Check if the user has the required role
     */
    fun ApplicationCall.hasRole(vararg roles: Role): Boolean {
        val userRole = getUserRole()?.let { Role.fromString(it) }
        return userRole != null && roles.contains(userRole)
    }

    /**
     * Check if the user has admin role
     */
    fun ApplicationCall.isAdmin(): Boolean {
        return hasRole(Role.ADMIN)
    }

    /**
     * Check if the user has seller role
     */
    fun ApplicationCall.isSeller(): Boolean {
        return hasRole(Role.SELLER)
    }

    /**
     * Check if the user has customer role
     */
    fun ApplicationCall.isCustomer(): Boolean {
        return hasRole(Role.CUSTOMER)
    }

    /**
     * Respond with forbidden status if role check fails
     */
    suspend fun ApplicationCall.requireRole(vararg roles: Role): Boolean {
        if (!hasRole(*roles)) {
            respond(HttpStatusCode.Forbidden, mapOf(
                "success" to false,
                "error" to mapOf(
                    "code" to "FORBIDDEN",
                    "message" to "You don't have permission to access this resource"
                )
            ))
            return false
        }
        return true
    }

    /**
     * Respond with forbidden status if not admin
     */
    suspend fun ApplicationCall.requireAdmin(): Boolean {
        return requireRole(Role.ADMIN)
    }

    /**
     * Respond with forbidden status if not admin or seller
     */
    suspend fun ApplicationCall.requireAdminOrSeller(): Boolean {
        return requireRole(Role.ADMIN, Role.SELLER)
    }
}
