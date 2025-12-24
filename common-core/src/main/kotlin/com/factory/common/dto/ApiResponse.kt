package com.factory.common.dto

import kotlinx.serialization.Serializable

/**
 * Generic API Response wrapper
 * Shared by all services for consistent response format
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: ErrorDetail? = null
)

@Serializable
data class ErrorDetail(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)
