package com.factory.common.security

/**
 * Allowed roles for authorization
 * Shared across all services
 */
enum class Role {
    EMPLOYEE,
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
