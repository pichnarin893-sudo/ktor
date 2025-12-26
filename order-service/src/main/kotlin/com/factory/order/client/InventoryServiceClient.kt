package com.factory.order.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Client for communicating with inventory-service
 */
class InventoryServiceClient(
    private val client: HttpClient,
    private val inventoryServiceUrl: String = "http://inventory-service:8082"
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getProduct(productId: UUID): ProductInfo? {
        return try {
            val response: HttpResponse = client.get("$inventoryServiceUrl/v1/inventory/items/$productId")

            if (response.status == HttpStatusCode.OK) {
                val body = response.bodyAsText()
                val apiResponse = json.decodeFromString<ApiResponse<ProductInfo>>(body)
                apiResponse.data
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error fetching product from inventory service: ${e.message}")
            null
        }
    }
}

@Serializable
data class ProductInfo(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: String,
    val stockQuantity: Int? = null
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)
