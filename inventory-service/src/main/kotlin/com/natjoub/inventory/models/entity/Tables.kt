package com.natjoub.inventory.models.entity

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * Branches table definition
 * Stores physical locations/stores
 */
object Branches : Table("inventory_schema.branches") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255)
    val code = varchar("code", 50).uniqueIndex()
    val address = text("address").nullable()
    val city = varchar("city", 100).nullable()
    val country = varchar("country", 100).nullable()
    val phoneNumber = varchar("phone_number", 20).nullable()
    val email = varchar("email", 255).nullable()
    val managerName = varchar("manager_name", 255).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

/**
 * Categories table definition
 * Stores product categories with hierarchical support
 */
object Categories : Table("inventory_schema.categories") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255).uniqueIndex()
    val description = text("description").nullable()
    val parentCategoryId = uuid("parent_category_id").references(id).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

/**
 * Inventory items table definition
 * Stores products/items in inventory
 */
object InventoryItems : Table("inventory_schema.inventory_items") {
    val id = uuid("id").autoGenerate()
    val sku = varchar("sku", 100).uniqueIndex()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val categoryId = uuid("category_id").references(Categories.id).nullable()
    val unitOfMeasure = varchar("unit_of_measure", 50)
    val unitPrice = decimal("unit_price", 10, 2)
    val reorderLevel = integer("reorder_level").default(10)
    val reorderQuantity = integer("reorder_quantity").default(50)
    val barcode = varchar("barcode", 255).nullable()
    val imageUrl = varchar("image_url", 500).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

/**
 * Stock levels table definition
 * Current stock per branch
 */
object StockLevels : Table("inventory_schema.stock_levels") {
    val id = uuid("id").autoGenerate()
    val inventoryItemId = uuid("inventory_item_id").references(InventoryItems.id)
    val branchId = uuid("branch_id").references(Branches.id)
    val quantity = integer("quantity").default(0)
    val reservedQuantity = integer("reserved_quantity").default(0)
    val lastCountedAt = datetime("last_counted_at").nullable()
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(inventoryItemId, branchId)
    }
}

/**
 * Stock movements table definition
 * Track inventory in/out movements
 */
object StockMovements : Table("inventory_schema.stock_movements") {
    val id = uuid("id").autoGenerate()
    val inventoryItemId = uuid("inventory_item_id").references(InventoryItems.id)
    val fromBranchId = uuid("from_branch_id").references(Branches.id).nullable()
    val toBranchId = uuid("to_branch_id").references(Branches.id).nullable()
    val movementType = varchar("movement_type", 50) // 'IN', 'OUT', 'TRANSFER', 'ADJUSTMENT', 'RETURN'
    val quantity = integer("quantity")
    val unitPrice = decimal("unit_price", 10, 2).nullable()
    val referenceNumber = varchar("reference_number", 100).nullable()
    val notes = text("notes").nullable()
    val performedBy = uuid("performed_by").nullable() // Reference to user from auth schema
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}
