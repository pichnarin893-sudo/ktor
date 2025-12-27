package com.factory.telegram.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

// Request DTOs
@Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String,
    val telegramId: Long,
    val username: String? = null,
    val phoneNumber: String? = null
)

@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class VerifyOTPRequest(
    val identifier: String,
    val otp: String
)

// Auth Service Client
class AuthServiceClient(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun registerCustomer(
        telegramId: Long,
        firstName: String,
        lastName: String,
        email: String,
        username: String?,
        phoneNumber: String?,
        password: String
    ): FullAuthResponse {
        val request = RegisterRequest(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            role = "customer",
            telegramId = telegramId,
            username = username,
            phoneNumber = phoneNumber
        )

        println("Registering customer with request: $request")

        val response: HttpResponse = client.post("$baseUrl/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<FullAuthApiResponse>(body)
        return apiResponse.data ?: throw Exception(apiResponse.message ?: "Registration failed")
    }

    suspend fun loginCustomer(identifier: String, password: String): FullAuthResponse {
        val request = LoginRequest(
            identifier = identifier,
            password = password
        )

        val response: HttpResponse = client.post("$baseUrl/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<FullAuthApiResponse>(body)
        return apiResponse.data ?: throw Exception(apiResponse.message ?: "Login failed")
    }

    suspend fun refreshToken(refreshToken: String): FullAuthResponse {
        val request = RefreshTokenRequest(refreshToken = refreshToken)

        val response: HttpResponse = client.post("$baseUrl/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<FullAuthApiResponse>(body)
        return apiResponse.data ?: throw Exception(apiResponse.message ?: "Token refresh failed")
    }

    suspend fun verifyOTP(email: String, otp: String) {
        val request = VerifyOTPRequest(
            identifier = email,
            otp = otp
        )

        val response: HttpResponse = client.post("$baseUrl/api/v1/auth/verify-otp") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            val apiResponse = json.decodeFromString<MessageApiResponse>(body)
            throw Exception(apiResponse.message ?: "OTP verification failed")
        }
    }

    suspend fun resendOTP(identifier: String): MessageResponse {
        val request = ResendOTPRequest(identifier = identifier)

        val response: HttpResponse = client.post("$baseUrl/api/v1/auth/resend-otp") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<MessageApiResponse>(body)
        return apiResponse.data ?: throw Exception(apiResponse.message ?: "Failed to resend OTP")
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

@Serializable
data class FullAuthApiResponse(
    val success: Boolean,
    val data: FullAuthResponse? = null,
    val message: String? = null
)

@Serializable
data class FullAuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserData,
    val expiresIn: Long // seconds
)

@Serializable
data class UserData(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val username: String? = null,
    val phoneNumber: String? = null,
    val role: String,
    val isActive: Boolean,
    val isVerified: Boolean,
    val createdAt: String
)

@Serializable
data class ResendOTPRequest(
    val identifier: String
)

@Serializable
data class MessageResponse(
    val message: String
)

@Serializable
data class MessageApiResponse(
    val success: Boolean,
    val data: MessageResponse? = null,
    val message: String? = null
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

@Serializable
data class OrderItemRequest(
    val productId: String,
    val quantity: Int
)

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
    val deliveryAddress: String,
    val notes: String? = null
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
        val request = CreateOrderRequest(
            items = listOf(
                OrderItemRequest(
                    productId = productId,
                    quantity = quantity
                )
            ),
            deliveryAddress = deliveryAddress,
            notes = "Order via Telegram Bot"
        )

        val response: HttpResponse = client.post("$baseUrl/api/v1/customer/orders") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }

        val body = response.bodyAsText()
        val apiResponse = json.decodeFromString<OrderResponse>(body)
        return apiResponse.data ?: throw Exception("Order creation failed")
    }

    suspend fun getMyOrders(token: String): List<Order> {
        val response: HttpResponse = client.get("$baseUrl/api/v1/customer/orders") {
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
