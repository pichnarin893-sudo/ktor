package com.natjoub.inventory.client

import com.natjoub.common.dto.ApiResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * REST client for calling Auth Service
 * Used to validate users for stock movements and other cross-service operations
 */
class AuthServiceClient(
    private val authServiceUrl: String
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    /**
     * Validate if user exists and is active
     * Called when recording stock movements with performed_by field
     */
    suspend fun validateUser(userId: String, token: String): UserValidationResult {
        return try {
            val response = client.get("$authServiceUrl/api/v1/internal/users/$userId/validate") {
                bearerAuth(token)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val apiResponse = response.body<ApiResponse<UserValidationData>>()
                    val data = apiResponse.data
                    if (apiResponse.success && data != null) {
                        UserValidationResult(
                            valid = data.valid,
                            userId = userId,
                            role = data.role,
                            error = data.message
                        )
                    } else {
                        UserValidationResult(
                            valid = false,
                            userId = userId,
                            error = apiResponse.message ?: "Validation failed"
                        )
                    }
                }
                HttpStatusCode.Unauthorized -> {
                    UserValidationResult(
                        valid = false,
                        userId = userId,
                        error = "Unauthorized - Invalid token"
                    )
                }
                else -> {
                    UserValidationResult(
                        valid = false,
                        userId = userId,
                        error = "Auth service returned ${response.status}"
                    )
                }
            }
        } catch (e: Exception) {
            // In case Auth service is down, log and return invalid
            // In production, you might want to implement circuit breaker pattern
            UserValidationResult(
                valid = false,
                userId = userId,
                error = "Failed to contact Auth service: ${e.message}"
            )
        }
    }

    fun close() {
        client.close()
    }
}

@Serializable
data class UserValidationData(
    val valid: Boolean,
    val userId: String,
    val role: String? = null,
    val message: String? = null
)

data class UserValidationResult(
    val valid: Boolean,
    val userId: String,
    val role: String? = null,
    val error: String? = null
)
