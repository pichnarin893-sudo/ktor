package com.natjoub.inventory.models.dto

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

// ============= Branch DTOs =============

@Serializable
data class CreateBranchRequest(
    val name: String,
    val code: String,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val managerName: String? = null
)

@Serializable
data class UpdateBranchRequest(
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val managerName: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class BranchResponse(
    val id: String,
    val name: String,
    val code: String,
    val address: String?,
    val city: String?,
    val country: String?,
    val phoneNumber: String?,
    val email: String?,
    val managerName: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

// ============= Category DTOs =============

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val description: String? = null,
    val parentCategoryId: String? = null
)

@Serializable
data class UpdateCategoryRequest(
    val name: String? = null,
    val description: String? = null,
    val parentCategoryId: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class CategoryResponse(
    val id: String,
    val name: String,
    val description: String?,
    val parentCategoryId: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

// ============= Inventory Item DTOs =============

@Serializable
data class CreateInventoryItemRequest(
    val sku: String,
    val name: String,
    val description: String? = null,
    val categoryId: String? = null,
    val unitOfMeasure: String,
    val unitPrice: String, // Use String for decimal to avoid precision issues
    val reorderLevel: Int = 10,
    val reorderQuantity: Int = 50,
    val barcode: String? = null,
    val imageUrl: String? = null
)

@Serializable
data class UpdateInventoryItemRequest(
    val name: String? = null,
    val description: String? = null,
    val categoryId: String? = null,
    val unitOfMeasure: String? = null,
    val unitPrice: String? = null,
    val reorderLevel: Int? = null,
    val reorderQuantity: Int? = null,
    val barcode: String? = null,
    val imageUrl: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class InventoryItemResponse(
    val id: String,
    val sku: String,
    val name: String,
    val description: String?,
    val categoryId: String?,
    val categoryName: String?,
    val unitOfMeasure: String,
    val unitPrice: String,
    val reorderLevel: Int,
    val reorderQuantity: Int,
    val barcode: String?,
    val imageUrl: String?,
    val isActive: Boolean,
    val totalStock: Int = 0, // Total across all branches
    val createdAt: String,
    val updatedAt: String
)

// ============= Stock Level DTOs =============

@Serializable
data class StockLevelResponse(
    val id: String,
    val inventoryItemId: String,
    val inventoryItemName: String,
    val sku: String,
    val branchId: String,
    val branchName: String,
    val quantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int, // quantity - reservedQuantity
    val lastCountedAt: String?,
    val updatedAt: String
)

@Serializable
data class UpdateStockLevelRequest(
    val quantity: Int
)

// ============= Stock Movement DTOs =============

@Serializable
data class CreateStockMovementRequest(
    val inventoryItemId: String,
    val fromBranchId: String? = null,
    val toBranchId: String? = null,
    val movementType: String, // 'IN', 'OUT', 'TRANSFER', 'ADJUSTMENT', 'RETURN'
    val quantity: Int,
    val unitPrice: String? = null,
    val referenceNumber: String? = null,
    val notes: String? = null
)

@Serializable
data class StockMovementResponse(
    val id: String,
    val inventoryItemId: String,
    val inventoryItemName: String,
    val sku: String,
    val fromBranchId: String?,
    val fromBranchName: String?,
    val toBranchId: String?,
    val toBranchName: String?,
    val movementType: String,
    val quantity: Int,
    val unitPrice: String?,
    val referenceNumber: String?,
    val notes: String?,
    val performedBy: String?,
    val createdAt: String
)

// ============= Common DTOs =============

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String = LocalDateTime.now().toString()
)

@Serializable
data class SuccessResponse(
    val message: String,
    val data: String? = null
)
