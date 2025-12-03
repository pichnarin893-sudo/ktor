package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureValidation() {
    install(RequestValidation) {
        // Add validation rules here as needed
        // Example:
        // validate<CreateUserRequest> { request ->
        //     if (request.email.isBlank()) {
        //         ValidationResult.Invalid("Email cannot be blank")
        //     } else {
        //         ValidationResult.Valid
        //     }
        // }
    }
}