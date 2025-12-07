package com.natjoub.inventory.models.entity

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Branch entity
 * Represents a physical location/store
 */
data class Branch(
    val id: UUID,
    val name: String,
    val code: String,
    val address: String?,
    val city: String?,
    val country: String?,
    val phoneNumber: String?,
    val email: String?,
    val managerName: String?,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Category entity
 * Represents a product category with hierarchical support
 */
data class Category(
    val id: UUID,
    val name: String,
    val description: String?,
    val parentCategoryId: UUID?,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Inventory item entity
 * Represents a product/item in inventory
 */
data class InventoryItem(
    val id: UUID,
    val sku: String,
    val name: String,
    val description: String?,
    val categoryId: UUID?,
    val unitOfMeasure: String,
    val unitPrice: BigDecimal,
    val reorderLevel: Int = 10,
    val reorderQuantity: Int = 50,
    val barcode: String?,
    val imageUrl: String?,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Stock level entity
 * Represents current stock at a branch
 */
data class StockLevel(
    val id: UUID,
    val inventoryItemId: UUID,
    val branchId: UUID,
    val quantity: Int,
    val reservedQuantity: Int = 0,
    val lastCountedAt: LocalDateTime?,
    val updatedAt: LocalDateTime
)

/**
 * Stock movement entity
 * Represents a stock movement transaction
 */
data class StockMovement(
    val id: UUID,
    val inventoryItemId: UUID,
    val fromBranchId: UUID?,
    val toBranchId: UUID?,
    val movementType: MovementType,
    val quantity: Int,
    val unitPrice: BigDecimal?,
    val referenceNumber: String?,
    val notes: String?,
    val performedBy: UUID?,
    val createdAt: LocalDateTime
)

/**
 * Movement type enum
 */
enum class MovementType {
    IN,         // Stock received
    OUT,        // Stock sold/used
    TRANSFER,   // Stock transferred between branches
    ADJUSTMENT, // Stock count adjustment
    RETURN      // Stock returned
}
