package com.factory.order.repositories

import com.factory.order.models.entity.OrderItems
import com.factory.order.models.entity.Orders
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.UUID

interface OrderRepository {
    suspend fun createOrder(
        customerId: UUID,
        totalAmount: BigDecimal,
        deliveryAddress: String,
        notes: String?,
        items: List<OrderItemData>
    ): UUID

    suspend fun getOrderById(orderId: UUID): OrderData?
    suspend fun getOrdersByCustomerId(customerId: UUID): List<OrderData>
    suspend fun getAllOrders(limit: Int, offset: Int): List<OrderData>
    suspend fun updateOrderStatus(orderId: UUID, status: String): Boolean
    suspend fun deleteOrder(orderId: UUID): Boolean
}

data class OrderData(
    val id: UUID,
    val customerId: UUID,
    val totalAmount: BigDecimal,
    val status: String,
    val deliveryAddress: String,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
    val items: List<OrderItemData>
)

data class OrderItemData(
    val id: UUID = UUID.randomUUID(),
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)

class OrderRepositoryImpl : OrderRepository {
    override suspend fun createOrder(
        customerId: UUID,
        totalAmount: BigDecimal,
        deliveryAddress: String,
        notes: String?,
        items: List<OrderItemData>
    ): UUID = transaction {
        val orderId = Orders.insertAndGetId {
            it[Orders.customerId] = customerId
            it[Orders.totalAmount] = totalAmount
            it[Orders.status] = "PENDING"
            it[Orders.deliveryAddress] = deliveryAddress
            it[Orders.notes] = notes
        }.value

        items.forEach { item ->
            OrderItems.insert {
                it[OrderItems.orderId] = orderId
                it[productId] = item.productId
                it[productName] = item.productName
                it[quantity] = item.quantity
                it[unitPrice] = item.unitPrice
                it[subtotal] = item.subtotal
            }
        }

        orderId
    }

    override suspend fun getOrderById(orderId: UUID): OrderData? = transaction {
        val orderRow = Orders.select { Orders.id eq orderId }.singleOrNull()
            ?: return@transaction null

        val items = OrderItems.select { OrderItems.orderId eq orderId }.map {
            OrderItemData(
                id = it[OrderItems.id].value,
                productId = it[OrderItems.productId],
                productName = it[OrderItems.productName],
                quantity = it[OrderItems.quantity],
                unitPrice = it[OrderItems.unitPrice],
                subtotal = it[OrderItems.subtotal]
            )
        }

        OrderData(
            id = orderRow[Orders.id].value,
            customerId = orderRow[Orders.customerId],
            totalAmount = orderRow[Orders.totalAmount],
            status = orderRow[Orders.status],
            deliveryAddress = orderRow[Orders.deliveryAddress],
            notes = orderRow[Orders.notes],
            createdAt = orderRow[Orders.createdAt].toString(),
            updatedAt = orderRow[Orders.updatedAt].toString(),
            items = items
        )
    }

    override suspend fun getOrdersByCustomerId(customerId: UUID): List<OrderData> = transaction {
        Orders.select { Orders.customerId eq customerId }
            .orderBy(Orders.createdAt to SortOrder.DESC)
            .map { orderRow ->
                val items = OrderItems.select { OrderItems.orderId eq orderRow[Orders.id].value }.map {
                    OrderItemData(
                        id = it[OrderItems.id].value,
                        productId = it[OrderItems.productId],
                        productName = it[OrderItems.productName],
                        quantity = it[OrderItems.quantity],
                        unitPrice = it[OrderItems.unitPrice],
                        subtotal = it[OrderItems.subtotal]
                    )
                }

                OrderData(
                    id = orderRow[Orders.id].value,
                    customerId = orderRow[Orders.customerId],
                    totalAmount = orderRow[Orders.totalAmount],
                    status = orderRow[Orders.status],
                    deliveryAddress = orderRow[Orders.deliveryAddress],
                    notes = orderRow[Orders.notes],
                    createdAt = orderRow[Orders.createdAt].toString(),
                    updatedAt = orderRow[Orders.updatedAt].toString(),
                    items = items
                )
            }
    }

    override suspend fun getAllOrders(limit: Int, offset: Int): List<OrderData> = transaction {
        Orders.selectAll()
            .orderBy(Orders.createdAt to SortOrder.DESC)
            .limit(limit, offset.toLong())
            .map { orderRow ->
                val items = OrderItems.select { OrderItems.orderId eq orderRow[Orders.id].value }.map {
                    OrderItemData(
                        id = it[OrderItems.id].value,
                        productId = it[OrderItems.productId],
                        productName = it[OrderItems.productName],
                        quantity = it[OrderItems.quantity],
                        unitPrice = it[OrderItems.unitPrice],
                        subtotal = it[OrderItems.subtotal]
                    )
                }

                OrderData(
                    id = orderRow[Orders.id].value,
                    customerId = orderRow[Orders.customerId],
                    totalAmount = orderRow[Orders.totalAmount],
                    status = orderRow[Orders.status],
                    deliveryAddress = orderRow[Orders.deliveryAddress],
                    notes = orderRow[Orders.notes],
                    createdAt = orderRow[Orders.createdAt].toString(),
                    updatedAt = orderRow[Orders.updatedAt].toString(),
                    items = items
                )
            }
    }

    override suspend fun updateOrderStatus(orderId: UUID, status: String): Boolean = transaction {
        Orders.update({ Orders.id eq orderId }) {
            it[Orders.status] = status
        } > 0
    }

    override suspend fun deleteOrder(orderId: UUID): Boolean = transaction {
        Orders.deleteWhere { id eq orderId } > 0
    }
}
