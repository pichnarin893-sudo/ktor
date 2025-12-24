package com.factory.auth.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

/**
 * Configure CORS (Cross-Origin Resource Sharing)
 */
fun Application.configureCORS() {
    install(CORS) {
        // Allow all methods
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        // Allow common headers
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)

        // Allow credentials
        allowCredentials = true

        // Allow any host for development - CHANGE THIS IN PRODUCTION
        anyHost()

        // For production, use specific origins:
        // allowHost("your-frontend-domain.com", schemes = listOf("https"))
        // allowHost("localhost:3000", schemes = listOf("http"))
    }
}
