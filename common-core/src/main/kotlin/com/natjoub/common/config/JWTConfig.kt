package com.natjoub.common.config

/**
 * JWT configuration data class
 * Shared by all services for token generation and validation
 */
data class JWTConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String
)
