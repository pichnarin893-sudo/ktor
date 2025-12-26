package com.factory.telegram.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

// Auth Service Client
class AuthServiceClient(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun registerCustomer(
        telegramId: Long,
        firstName: String,
        lastName: String,
        email: String
    ): String {
        val response: HttpResponse = client.post("$baseUrl/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "password" to "telegram_$telegramId",  // Auto-generated password
                "role" to "customer",
                "telegramId" to telegramId
            ))
        }

        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<AuthResponse>(body)
        return apiResponse.data?.token ?: throw Exception("Registration failed")
    }

    suspend fun loginCustomer(telegramId: Long): String {
        val response: HttpResponse = client.post("$baseUrl/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "identifier" to "customer_$telegramId@telegram.bot",
                "password" to "telegram_$telegramId"
            ))
        }

        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<AuthResponse>(body)
        return apiResponse.data?.token ?: throw Exception("Login failed")
    }
}

@Serializable
data class AuthResponse(
    val success: Boolean,
    val data: TokenData? = null,
    val message: String? = null
)

@Serializable
data class TokenData(
    val token: String
)

// Inventory Service Client
class InventoryServiceClient(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getProducts(): List<Product> {
        val response: HttpResponse = client.get("$baseUrl/api/v1/inventory/items")
        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<ProductListResponse>(body)
        return apiResponse.data ?: emptyList()
    }

    suspend fun getCategories(): List<Category> {
        val response: HttpResponse = client.get("$baseUrl/api/v1/inventory/categories")
        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<CategoryListResponse>(body)
        return apiResponse.data ?: emptyList()
    }

    suspend fun getProductsByCategory(categoryId: String): List<Product> {
        val response: HttpResponse = client.get("$baseUrl/api/v1/inventory/items") {
            parameter("categoryId", categoryId)
        }
        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<ProductListResponse>(body)
        return apiResponse.data ?: emptyList()
    }
}

@Serializable
data class ProductListResponse(
    val data: List<Product>? = null,
    val total: Long? = null,
    val page: Int? = null,
    val pageSize: Int? = null,
    val totalPages: Int? = null
)

@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String? = null,
    val unitPrice: String,
    val totalStock: Int? = null,
    val categoryId: String? = null,
    val categoryName: String? = null
)

@Serializable
data class CategoryListResponse(
    val data: List<Category>? = null,
    val total: Long? = null,
    val page: Int? = null,
    val pageSize: Int? = null,
    val totalPages: Int? = null
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val description: String? = null,
    val isActive: Boolean? = null
)

// Order Service Client
class OrderServiceClient(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun createOrder(
        token: String,
        productId: String,
        quantity: Int,
        deliveryAddress: String
    ): Order {
        val response: HttpResponse = client.post("$baseUrl/v1/customer/orders") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(mapOf(
                "items" to listOf(
                    mapOf(
                        "productId" to productId,
                        "quantity" to quantity
                    )
                ),
                "deliveryAddress" to deliveryAddress,
                "notes" to "Order via Telegram Bot"
            ))
        }

        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<OrderResponse>(body)
        return apiResponse.data ?: throw Exception("Order creation failed")
    }

    suspend fun getMyOrders(token: String): List<Order> {
        val response: HttpResponse = client.get("$baseUrl/v1/customer/orders") {
            header("Authorization", "Bearer $token")
        }

        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<OrderListResponse>(body)
        return apiResponse.data ?: emptyList()
    }
}

@Serializable
data class OrderResponse(
    val success: Boolean,
    val data: Order? = null
)

@Serializable
data class OrderListResponse(
    val success: Boolean,
    val data: List<Order>? = null
)

@Serializable
data class Order(
    val id: String,
    val totalAmount: String,
    val status: String,
    val createdAt: String
)
