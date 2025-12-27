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
            val response: HttpResponse = client.get("$inventoryServiceUrl/api/v1/inventory/items/$productId")

            if (response.status == HttpStatusCode.OK) {
                val body = response.bodyAsText()
                val inventoryItem = json.decodeFromString<InventoryItemResponse>(body)
                // Map inventory item to ProductInfo
                ProductInfo(
                    id = inventoryItem.id,
                    name = inventoryItem.name,
                    description = inventoryItem.description,
                    price = inventoryItem.unitPrice,
                    stockQuantity = inventoryItem.totalStock
                )
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
data class InventoryItemResponse(
    val id: String,
    val sku: String,
    val name: String,
    val description: String? = null,
    val categoryId: String,
    val categoryName: String,
    val unitOfMeasure: String,
    val unitPrice: String,
    val reorderLevel: Int,
    val reorderQuantity: Int,
    val barcode: String? = null,
    val imageUrl: String? = null,
    val isActive: Boolean,
    val totalStock: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ProductInfo(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: String,
    val stockQuantity: Int? = null
)
