package com.natjoub.core.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import kotlin.time.Duration.Companion.minutes

/**
 * Configure rate limiting for authentication endpoints
 */
fun Application.configureRateLimit() {
    install(RateLimit) {
        // Rate limit for authentication endpoints
        register(RateLimitName("auth")) {
            rateLimiter(limit = 5, refillPeriod = 1.minutes)
            requestKey { call ->
                // Rate limit by IP address
                call.request.local.remoteHost
            }
        }

        // Rate limit for general API endpoints
        register(RateLimitName("api")) {
            rateLimiter(limit = 100, refillPeriod = 1.minutes)
            requestKey { call ->
                call.request.local.remoteHost
            }
        }
    }
}
