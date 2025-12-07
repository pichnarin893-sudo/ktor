package com.natjoub.auth.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.natjoub.core.config.JWTConfig
import com.natjoub.auth.repositories.TokenRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject

/**
 * Configure JWT authentication with role-based configurations
 */
fun Application.configureAuth() {
    val jwtConfig by inject<JWTConfig>()
    val tokenRepository by inject<TokenRepository>()

    authentication {
        // Default JWT configuration (for routes that don't specify a name)
        jwt {
            realm = jwtConfig.realm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )

            validate { credential ->
                // Check if token is blacklisted
                val token = request.headers["Authorization"]?.removePrefix("Bearer ")
                if (token != null) {
                    val isBlacklisted = runBlocking {
                        tokenRepository.isTokenBlacklisted(token)
                    }
                    if (isBlacklisted) {
                        return@validate null
                    }
                }

                // Validate audience (accept any valid role)
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf(
                    "success" to false,
                    "error" to mapOf(
                        "code" to "UNAUTHORIZED",
                        "message" to "Invalid or expired token"
                    )
                ))
            }
        }

        // Admin JWT configuration
        jwt("admin-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )

            validate { credential ->
                // Check if token is blacklisted
                val token = request.headers["Authorization"]?.removePrefix("Bearer ")
                if (token != null) {
                    val isBlacklisted = runBlocking {
                        tokenRepository.isTokenBlacklisted(token)
                    }
                    if (isBlacklisted) {
                        return@validate null
                    }
                }

                // Validate audience and role
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    val role = credential.payload.getClaim("role").asString()
                    if (role == "admin") {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf(
                    "success" to false,
                    "error" to mapOf(
                        "code" to "UNAUTHORIZED",
                        "message" to "Invalid or expired token"
                    )
                ))
            }
        }

        // Seller JWT configuration
        jwt("seller-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )

            validate { credential ->
                // Check if token is blacklisted
                val token = request.headers["Authorization"]?.removePrefix("Bearer ")
                if (token != null) {
                    val isBlacklisted = runBlocking {
                        tokenRepository.isTokenBlacklisted(token)
                    }
                    if (isBlacklisted) {
                        return@validate null
                    }
                }

                // Validate audience and role
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    val role = credential.payload.getClaim("role").asString()
                    if (role == "seller") {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf(
                    "success" to false,
                    "error" to mapOf(
                        "code" to "UNAUTHORIZED",
                        "message" to "Invalid or expired token"
                    )
                ))
            }
        }

        // Customer JWT configuration
        jwt("customer-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )

            validate { credential ->
                // Check if token is blacklisted
                val token = request.headers["Authorization"]?.removePrefix("Bearer ")
                if (token != null) {
                    val isBlacklisted = runBlocking {
                        tokenRepository.isTokenBlacklisted(token)
                    }
                    if (isBlacklisted) {
                        return@validate null
                    }
                }

                // Validate audience and role
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    val role = credential.payload.getClaim("role").asString()
                    if (role == "customer") {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf(
                    "success" to false,
                    "error" to mapOf(
                        "code" to "UNAUTHORIZED",
                        "message" to "Invalid or expired token"
                    )
                ))
            }
        }
    }
}

/**
 * Extract user ID from JWT principal
 */
fun JWTPrincipal.getUserId(): String? {
    return payload.getClaim("user_id").asString()
}

/**
 * Extract role from JWT principal
 */
fun JWTPrincipal.getRole(): String? {
    return payload.getClaim("role").asString()
}
