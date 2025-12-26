package com.factory.order.services

import com.factory.order.client.InventoryServiceClient
import com.factory.order.models.dto.*
import com.factory.order.repositories.OrderData
import com.factory.order.repositories.OrderItemData
import com.factory.order.repositories.OrderRepository
import java.math.BigDecimal
import java.util.UUID

class OrderService(
    private val orderRepository: OrderRepository,
    private val inventoryClient: InventoryServiceClient
) {
    suspend fun createOrder(customerId: UUID, request: CreateOrderRequest): OrderResponse {
        // Fetch product details from inventory service
        val orderItems = mutableListOf<OrderItemData>()
        var totalAmount = BigDecimal.ZERO

        request.items.forEach { item ->
            val productId = UUID.fromString(item.productId)
            val product = inventoryClient.getProduct(productId)
                ?: throw IllegalArgumentException("Product not found: ${item.productId}")

            val unitPrice = BigDecimal(product.price)
            val subtotal = unitPrice.multiply(BigDecimal(item.quantity))

            orderItems.add(
                OrderItemData(
                    productId = productId,
                    productName = product.name,
                    quantity = item.quantity,
                    unitPrice = unitPrice,
                    subtotal = subtotal
                )
            )

            totalAmount = totalAmount.add(subtotal)
        }

        // Create the order
        val orderId = orderRepository.createOrder(
            customerId = customerId,
            totalAmount = totalAmount,
            deliveryAddress = request.deliveryAddress,
            notes = request.notes,
            items = orderItems
        )

        // Fetch and return the created order
        return getOrder(orderId)
            ?: throw IllegalStateException("Failed to create order")
    }

    suspend fun getOrder(orderId: UUID): OrderResponse? {
        val orderData = orderRepository.getOrderById(orderId) ?: return null
        return orderDataToResponse(orderData)
    }

    suspend fun getCustomerOrders(customerId: UUID): List<OrderResponse> {
        return orderRepository.getOrdersByCustomerId(customerId).map { orderDataToResponse(it) }
    }

    suspend fun getAllOrders(limit: Int = 100, offset: Int = 0): List<OrderResponse> {
        return orderRepository.getAllOrders(limit, offset).map { orderDataToResponse(it) }
    }

    suspend fun updateOrderStatus(orderId: UUID, status: String): Boolean {
        // Validate status
        val validStatuses = listOf("PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED")
        if (status.uppercase() !in validStatuses) {
            throw IllegalArgumentException("Invalid status: $status")
        }

        return orderRepository.updateOrderStatus(orderId, status.uppercase())
    }

    suspend fun deleteOrder(orderId: UUID): Boolean {
        return orderRepository.deleteOrder(orderId)
    }

    private fun orderDataToResponse(data: OrderData): OrderResponse {
        return OrderResponse(
            id = data.id.toString(),
            customerId = data.customerId.toString(),
            totalAmount = data.totalAmount.toString(),
            status = data.status,
            deliveryAddress = data.deliveryAddress,
            notes = data.notes,
            items = data.items.map { item ->
                OrderItemResponse(
                    id = item.id.toString(),
                    productId = item.productId.toString(),
                    productName = item.productName,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice.toString(),
                    subtotal = item.subtotal.toString()
                )
            },
            createdAt = data.createdAt,
            updatedAt = data.updatedAt
        )
    }
}
