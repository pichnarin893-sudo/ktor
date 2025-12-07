package com.natjoub.auth.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

/**
 * Configure call logging for request/response monitoring
 */
fun Application.configureCallLogging() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()
            val userAgent = call.request.headers["User-Agent"]
            val remoteHost = call.request.local.remoteHost

            "$httpMethod $path - Status: $status - From: $remoteHost - UA: $userAgent"
        }
    }
}
