package com.factory.order.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.factory.common.config.JWTConfig
import com.factory.order.models.dto.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

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
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(error = "Invalid or expired token")
                )
            }
        }

        jwt("employee-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )

            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    val role = credential.payload.getClaim("role").asString()
                    if (role == "employee") {
                        JWTPrincipal(credential.payload)
                    } else null
                } else null
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(error = "Employee access required")
                )
            }
        }

        jwt("customer-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )

            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    val role = credential.payload.getClaim("role").asString()
                    if (role == "customer") {
                        JWTPrincipal(credential.payload)
                    } else null
                } else null
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(error = "Customer access required")
                )
            }
        }
    }
}
