package com.factory.order.models.entity

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * Order status enum
 */
enum class OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

/**
 * Orders table definition
 */
object Orders : UUIDTable("orders") {
    val customerId = uuid("customer_id")
    val totalAmount = decimal("total_amount", 12, 2)
    val status = varchar("status", 20).default("PENDING")
    val deliveryAddress = text("delivery_address")
    val notes = text("notes").nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

/**
 * Order items table definition
 */
object OrderItems : UUIDTable("order_items") {
    val orderId = uuid("order_id").references(Orders.id)
    val productId = uuid("product_id")
    val productName = varchar("product_name", 255)
    val quantity = integer("quantity")
    val unitPrice = decimal("unit_price", 12, 2)
    val subtotal = decimal("subtotal", 12, 2)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}
