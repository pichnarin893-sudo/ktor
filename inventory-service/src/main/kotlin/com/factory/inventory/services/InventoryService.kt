package com.factory.inventory.services

import com.factory.inventory.exceptions.InventoryException
import com.factory.inventory.models.dto.*
import com.factory.inventory.models.entity.*
import com.factory.inventory.repositories.*
import java.math.BigDecimal
import java.util.UUID

/**
 * Service for inventory operations
 * Handles business logic for branches, categories, items, stock levels, and movements
 */
class InventoryService(
    private val branchRepository: BranchRepository,
    private val categoryRepository: CategoryRepository,
    private val inventoryItemRepository: InventoryItemRepository,
    private val stockLevelRepository: StockLevelRepository,
    private val stockMovementRepository: StockMovementRepository,
    private val authServiceClient: com.natjoub.inventory.client.AuthServiceClient
) {

    // ============= Branch Operations =============

    suspend fun createBranch(request: CreateBranchRequest): BranchResponse {
        // Check if code already exists
        branchRepository.findByCode(request.code)?.let {
            throw InventoryException.BranchAlreadyExists("Branch with code ${request.code} already exists")
        }

        val branch = branchRepository.create(
            name = request.name,
            code = request.code,
            address = request.address,
            city = request.city,
            country = request.country,
            phoneNumber = request.phoneNumber,
            email = request.email,
            managerName = request.managerName
        )

        return branch.toResponse()
    }

    suspend fun getBranchById(id: String): BranchResponse {
        val branchId = UUID.fromString(id)
        val branch = branchRepository.findById(branchId)
            ?: throw InventoryException.BranchNotFound("Branch not found with id: $id")
        return branch.toResponse()
    }

    suspend fun getAllBranches(limit: Int = 100, offset: Long = 0): PaginatedResponse<BranchResponse> {
        val branches = branchRepository.findAll(limit, offset)
        val total = branchRepository.count()
        return PaginatedResponse(
            data = branches.map { it.toResponse() },
            total = total,
            page = (offset / limit).toInt() + 1,
            pageSize = limit,
            totalPages = ((total + limit - 1) / limit).toInt()
        )
    }

    suspend fun updateBranch(id: String, request: UpdateBranchRequest): BranchResponse {
        val branchId = UUID.fromString(id)
        val exists = branchRepository.findById(branchId)
            ?: throw InventoryException.BranchNotFound("Branch not found with id: $id")

        branchRepository.update(
            id = branchId,
            name = request.name,
            address = request.address,
            city = request.city,
            country = request.country,
            phoneNumber = request.phoneNumber,
            email = request.email,
            managerName = request.managerName,
            isActive = request.isActive
        )

        return getBranchById(id)
    }

    suspend fun deleteBranch(id: String): Boolean {
        val branchId = UUID.fromString(id)
        branchRepository.findById(branchId)
            ?: throw InventoryException.BranchNotFound("Branch not found with id: $id")

        return branchRepository.delete(branchId)
    }

    // ============= Category Operations =============

    suspend fun createCategory(request: CreateCategoryRequest): CategoryResponse {
        // Check if name already exists
        categoryRepository.findByName(request.name)?.let {
            throw InventoryException.CategoryAlreadyExists("Category with name ${request.name} already exists")
        }

        val parentCategoryId = request.parentCategoryId?.let { UUID.fromString(it) }

        val category = categoryRepository.create(
            name = request.name,
            description = request.description,
            parentCategoryId = parentCategoryId
        )

        return category.toResponse()
    }

    suspend fun getCategoryById(id: String): CategoryResponse {
        val categoryId = UUID.fromString(id)
        val category = categoryRepository.findById(categoryId)
            ?: throw InventoryException.CategoryNotFound("Category not found with id: $id")
        return category.toResponse()
    }

    suspend fun getAllCategories(limit: Int = 100, offset: Long = 0): PaginatedResponse<CategoryResponse> {
        val categories = categoryRepository.findAll(limit, offset)
        val total = categoryRepository.count()
        return PaginatedResponse(
            data = categories.map { it.toResponse() },
            total = total,
            page = (offset / limit).toInt() + 1,
            pageSize = limit,
            totalPages = ((total + limit - 1) / limit).toInt()
        )
    }

    suspend fun updateCategory(id: String, request: UpdateCategoryRequest): CategoryResponse {
        val categoryId = UUID.fromString(id)
        categoryRepository.findById(categoryId)
            ?: throw InventoryException.CategoryNotFound("Category not found with id: $id")

        val parentCategoryId = request.parentCategoryId?.let { UUID.fromString(it) }

        categoryRepository.update(
            id = categoryId,
            name = request.name,
            description = request.description,
            parentCategoryId = parentCategoryId,
            isActive = request.isActive
        )

        return getCategoryById(id)
    }

    suspend fun deleteCategory(id: String): Boolean {
        val categoryId = UUID.fromString(id)
        categoryRepository.findById(categoryId)
            ?: throw InventoryException.CategoryNotFound("Category not found with id: $id")

        return categoryRepository.delete(categoryId)
    }

    // ============= Inventory Item Operations =============

    suspend fun createInventoryItem(request: CreateInventoryItemRequest): InventoryItemResponse {
        // Check if SKU already exists
        inventoryItemRepository.findBySku(request.sku)?.let {
            throw InventoryException.ItemAlreadyExists("Item with SKU ${request.sku} already exists")
        }

        val categoryId = request.categoryId?.let { UUID.fromString(it) }
        val unitPrice = BigDecimal(request.unitPrice)

        val item = inventoryItemRepository.create(
            sku = request.sku,
            name = request.name,
            description = request.description,
            categoryId = categoryId,
            unitOfMeasure = request.unitOfMeasure,
            unitPrice = unitPrice,
            reorderLevel = request.reorderLevel,
            reorderQuantity = request.reorderQuantity,
            barcode = request.barcode,
            imageUrl = request.imageUrl
        )

        return item.toResponse(categoryRepository, stockLevelRepository)
    }

    suspend fun getInventoryItemById(id: String): InventoryItemResponse {
        val itemId = UUID.fromString(id)
        val item = inventoryItemRepository.findById(itemId)
            ?: throw InventoryException.ItemNotFound("Inventory item not found with id: $id")
        return item.toResponse(categoryRepository, stockLevelRepository)
    }

    suspend fun getAllInventoryItems(limit: Int = 100, offset: Long = 0): PaginatedResponse<InventoryItemResponse> {
        val items = inventoryItemRepository.findAll(limit, offset)
        val total = inventoryItemRepository.count()
        return PaginatedResponse(
            data = items.map { it.toResponse(categoryRepository, stockLevelRepository) },
            total = total,
            page = (offset / limit).toInt() + 1,
            pageSize = limit,
            totalPages = ((total + limit - 1) / limit).toInt()
        )
    }

    suspend fun updateInventoryItem(id: String, request: UpdateInventoryItemRequest): InventoryItemResponse {
        val itemId = UUID.fromString(id)
        inventoryItemRepository.findById(itemId)
            ?: throw InventoryException.ItemNotFound("Inventory item not found with id: $id")

        val categoryId = request.categoryId?.let { UUID.fromString(it) }
        val unitPrice = request.unitPrice?.let { BigDecimal(it) }

        inventoryItemRepository.update(
            id = itemId,
            name = request.name,
            description = request.description,
            categoryId = categoryId,
            unitOfMeasure = request.unitOfMeasure,
            unitPrice = unitPrice,
            reorderLevel = request.reorderLevel,
            reorderQuantity = request.reorderQuantity,
            barcode = request.barcode,
            imageUrl = request.imageUrl,
            isActive = request.isActive
        )

        return getInventoryItemById(id)
    }

    suspend fun deleteInventoryItem(id: String): Boolean {
        val itemId = UUID.fromString(id)
        inventoryItemRepository.findById(itemId)
            ?: throw InventoryException.ItemNotFound("Inventory item not found with id: $id")

        return inventoryItemRepository.delete(itemId)
    }

    // ============= Stock Movement Operations =============

    suspend fun createStockMovement(request: CreateStockMovementRequest, performedBy: UUID?): StockMovementResponse {
        val inventoryItemId = UUID.fromString(request.inventoryItemId)
        val fromBranchId = request.fromBranchId?.let { UUID.fromString(it) }
        val toBranchId = request.toBranchId?.let { UUID.fromString(it) }
        val movementType = MovementType.valueOf(request.movementType)
        val unitPrice = request.unitPrice?.let { BigDecimal(it) }

        // Validate inventory item exists
        inventoryItemRepository.findById(inventoryItemId)
            ?: throw InventoryException.ItemNotFound("Inventory item not found")

        // Validate branches exist
        fromBranchId?.let { id ->
            branchRepository.findById(id)
                ?: throw InventoryException.BranchNotFound("From branch not found")
        }
        toBranchId?.let { id ->
            branchRepository.findById(id)
                ?: throw InventoryException.BranchNotFound("To branch not found")
        }

        // Process stock movement based on type
        when (movementType) {
            MovementType.IN -> {
                // Stock coming into a branch
                toBranchId?.let {
                    stockLevelRepository.adjustQuantity(inventoryItemId, it, request.quantity)
                } ?: throw InventoryException.InvalidMovement("TO branch required for IN movement")
            }
            MovementType.OUT -> {
                // Stock going out of a branch
                fromBranchId?.let {
                    stockLevelRepository.adjustQuantity(inventoryItemId, it, -request.quantity)
                } ?: throw InventoryException.InvalidMovement("FROM branch required for OUT movement")
            }
            MovementType.TRANSFER -> {
                // Stock transfer between branches
                if (fromBranchId == null || toBranchId == null) {
                    throw InventoryException.InvalidMovement("Both FROM and TO branches required for TRANSFER")
                }
                stockLevelRepository.adjustQuantity(inventoryItemId, fromBranchId, -request.quantity)
                stockLevelRepository.adjustQuantity(inventoryItemId, toBranchId, request.quantity)
            }
            MovementType.ADJUSTMENT, MovementType.RETURN -> {
                // Stock adjustment or return
                toBranchId?.let {
                    stockLevelRepository.adjustQuantity(inventoryItemId, it, request.quantity)
                }
            }
        }

        // Record the movement
        val movement = stockMovementRepository.create(
            inventoryItemId = inventoryItemId,
            fromBranchId = fromBranchId,
            toBranchId = toBranchId,
            movementType = movementType,
            quantity = request.quantity,
            unitPrice = unitPrice,
            referenceNumber = request.referenceNumber,
            notes = request.notes,
            performedBy = performedBy
        )

        return movement.toResponse(inventoryItemRepository, branchRepository)
    }

    suspend fun getStockMovementById(id: String): StockMovementResponse {
        val movementId = UUID.fromString(id)
        val movement = stockMovementRepository.findById(movementId)
            ?: throw InventoryException.MovementNotFound("Stock movement not found with id: $id")
        return movement.toResponse(inventoryItemRepository, branchRepository)
    }

    suspend fun getAllStockMovements(limit: Int = 100, offset: Long = 0): PaginatedResponse<StockMovementResponse> {
        val movements = stockMovementRepository.findAll(limit, offset)
        val total = stockMovementRepository.count()
        return PaginatedResponse(
            data = movements.map { it.toResponse(inventoryItemRepository, branchRepository) },
            total = total,
            page = (offset / limit).toInt() + 1,
            pageSize = limit,
            totalPages = ((total + limit - 1) / limit).toInt()
        )
    }

    // ============= Stock Level Operations =============

    suspend fun getStockLevelsByBranch(branchId: String, limit: Int = 100, offset: Long = 0): PaginatedResponse<StockLevelResponse> {
        val branchUUID = UUID.fromString(branchId)
        branchRepository.findById(branchUUID)
            ?: throw InventoryException.BranchNotFound("Branch not found with id: $branchId")

        val stockLevels = stockLevelRepository.findByBranchId(branchUUID, limit, offset)
        val total = stockLevelRepository.count()

        return PaginatedResponse(
            data = stockLevels.map { it.toResponse(inventoryItemRepository, branchRepository) },
            total = total,
            page = (offset / limit).toInt() + 1,
            pageSize = limit,
            totalPages = ((total + limit - 1) / limit).toInt()
        )
    }

    suspend fun getLowStockItems(): List<StockLevelResponse> {
        val lowStock = stockLevelRepository.findLowStock()
        return lowStock.map { it.toResponse(inventoryItemRepository, branchRepository) }
    }
}

// Extension functions to convert entities to DTOs

private suspend fun Branch.toResponse() = BranchResponse(
    id = id.toString(),
    name = name,
    code = code,
    address = address,
    city = city,
    country = country,
    phoneNumber = phoneNumber,
    email = email,
    managerName = managerName,
    isActive = isActive,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

private suspend fun Category.toResponse() = CategoryResponse(
    id = id.toString(),
    name = name,
    description = description,
    parentCategoryId = parentCategoryId?.toString(),
    isActive = isActive,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

private suspend fun InventoryItem.toResponse(
    categoryRepository: CategoryRepository,
    stockLevelRepository: StockLevelRepository
): InventoryItemResponse {
    val categoryName = categoryId?.let { categoryRepository.findById(it)?.name }
    val totalStock = stockLevelRepository.getTotalStockForItem(id)

    return InventoryItemResponse(
        id = id.toString(),
        sku = sku,
        name = name,
        description = description,
        categoryId = categoryId?.toString(),
        categoryName = categoryName,
        unitOfMeasure = unitOfMeasure,
        unitPrice = unitPrice.toString(),
        reorderLevel = reorderLevel,
        reorderQuantity = reorderQuantity,
        barcode = barcode,
        imageUrl = imageUrl,
        isActive = isActive,
        totalStock = totalStock,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}

private suspend fun StockLevel.toResponse(
    inventoryItemRepository: InventoryItemRepository,
    branchRepository: BranchRepository
): StockLevelResponse {
    val item = inventoryItemRepository.findById(inventoryItemId)!!
    val branch = branchRepository.findById(branchId)!!

    return StockLevelResponse(
        id = id.toString(),
        inventoryItemId = inventoryItemId.toString(),
        inventoryItemName = item.name,
        sku = item.sku,
        branchId = branchId.toString(),
        branchName = branch.name,
        quantity = quantity,
        reservedQuantity = reservedQuantity,
        availableQuantity = quantity - reservedQuantity,
        lastCountedAt = lastCountedAt?.toString(),
        updatedAt = updatedAt.toString()
    )
}

private suspend fun StockMovement.toResponse(
    inventoryItemRepository: InventoryItemRepository,
    branchRepository: BranchRepository
): StockMovementResponse {
    val item = inventoryItemRepository.findById(inventoryItemId)!!
    val fromBranch = fromBranchId?.let { branchRepository.findById(it) }
    val toBranch = toBranchId?.let { branchRepository.findById(it) }

    return StockMovementResponse(
        id = id.toString(),
        inventoryItemId = inventoryItemId.toString(),
        inventoryItemName = item.name,
        sku = item.sku,
        fromBranchId = fromBranchId?.toString(),
        fromBranchName = fromBranch?.name,
        toBranchId = toBranchId?.toString(),
        toBranchName = toBranch?.name,
        movementType = movementType.name,
        quantity = quantity,
        unitPrice = unitPrice?.toString(),
        referenceNumber = referenceNumber,
        notes = notes,
        performedBy = performedBy?.toString(),
        createdAt = createdAt.toString()
    )
}
