package com.natjoub.inventory.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.natjoub.common.config.JWTConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

/**
 * Configure JWT authentication for Inventory Service
 * Simplified version - validates JWT signature but doesn't check token blacklist
 * (Token blacklist is managed by Auth service)
 */
fun Application.configureAuth() {
    val jwtConfig by inject<JWTConfig>()

    authentication {
        jwt {
            realm = jwtConfig.realm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )

            validate { credential ->
                // Validate JWT claims
                // NOTE: Cannot check token blacklist here (TokenRepository is in Auth service)
                // Trade-off: Revoked tokens will still work in Inventory service until expiry
                // Production solution: Use shared Redis cache or call Auth service API
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "success" to false,
                        "error" to mapOf(
                            "code" to "UNAUTHORIZED",
                            "message" to "Invalid or expired token"
                        )
                    )
                )
            }
        }
    }
}
