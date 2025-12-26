package com.factory.order.models.dto

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Request to create a new order
 */
@Serializable
data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
    val deliveryAddress: String,
    val notes: String? = null
)

@Serializable
data class OrderItemRequest(
    val productId: String,  // UUID as string
    val quantity: Int
)

/**
 * Request to update order status
 */
@Serializable
data class UpdateOrderStatusRequest(
    val status: String  // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
)

/**
 * Order response (full details)
 */
@Serializable
data class OrderResponse(
    val id: String,
    val customerId: String,
    val totalAmount: String,  // Decimal as string for JSON
    val status: String,
    val deliveryAddress: String,
    val notes: String?,
    val items: List<OrderItemResponse>,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class OrderItemResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: String,
    val subtotal: String
)

/**
 * Simplified order list response
 */
@Serializable
data class OrderListResponse(
    val id: String,
    val totalAmount: String,
    val status: String,
    val itemCount: Int,
    val createdAt: String
)

/**
 * Generic API response wrapper
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

@Serializable
data class MessageResponse(
    val message: String
)

/**
 * Error response for authentication/authorization failures
 */
@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val error: String
)
