package com.natjoub.common.exceptions

/**
 * Exception thrown when input validation fails
 * Shared across all services
 */
class ValidationException(
    message: String,
    val details: Map<String, String>? = null
) : BaseException(message, "VALIDATION_ERROR", 400)
