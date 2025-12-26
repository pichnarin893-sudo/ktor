package com.factory.common.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

/**
 * Authorization helper functions
 * Shared by all services for JWT claim extraction and role checking
 */

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
 * Check if the user has employee role
 */
fun ApplicationCall.isEmployee(): Boolean {
    return hasRole(Role.EMPLOYEE)
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
 * Respond with forbidden status if not employee
 */
suspend fun ApplicationCall.requireEmployee(): Boolean {
    return requireRole(Role.EMPLOYEE)
}
