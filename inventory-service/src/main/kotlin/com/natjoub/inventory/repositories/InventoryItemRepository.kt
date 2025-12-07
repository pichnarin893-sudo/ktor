package com.natjoub.inventory.repositories

import com.natjoub.inventory.models.entity.InventoryItem
import com.natjoub.inventory.models.entity.InventoryItems
import com.natjoub.inventory.config.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Repository for inventory item operations
 */
class InventoryItemRepository {

    suspend fun create(
        sku: String,
        name: String,
        description: String? = null,
        categoryId: UUID? = null,
        unitOfMeasure: String,
        unitPrice: BigDecimal,
        reorderLevel: Int = 10,
        reorderQuantity: Int = 50,
        barcode: String? = null,
        imageUrl: String? = null
    ): InventoryItem = dbQuery {
        val itemId = InventoryItems.insert {
            it[InventoryItems.sku] = sku
            it[InventoryItems.name] = name
            it[InventoryItems.description] = description
            it[InventoryItems.categoryId] = categoryId
            it[InventoryItems.unitOfMeasure] = unitOfMeasure
            it[InventoryItems.unitPrice] = unitPrice
            it[InventoryItems.reorderLevel] = reorderLevel
            it[InventoryItems.reorderQuantity] = reorderQuantity
            it[InventoryItems.barcode] = barcode
            it[InventoryItems.imageUrl] = imageUrl
        } get InventoryItems.id

        findById(itemId)!!
    }

    suspend fun findById(id: UUID): InventoryItem? = dbQuery {
        InventoryItems.select { InventoryItems.id eq id }
            .map { toInventoryItem(it) }
            .singleOrNull()
    }

    suspend fun findBySku(sku: String): InventoryItem? = dbQuery {
        InventoryItems.select { InventoryItems.sku eq sku }
            .map { toInventoryItem(it) }
            .singleOrNull()
    }

    suspend fun findByBarcode(barcode: String): InventoryItem? = dbQuery {
        InventoryItems.select { InventoryItems.barcode eq barcode }
            .map { toInventoryItem(it) }
            .singleOrNull()
    }

    suspend fun findAll(limit: Int = 100, offset: Long = 0): List<InventoryItem> = dbQuery {
        InventoryItems.selectAll()
            .limit(limit, offset)
            .orderBy(InventoryItems.name to SortOrder.ASC)
            .map { toInventoryItem(it) }
    }

    suspend fun findByCategoryId(categoryId: UUID): List<InventoryItem> = dbQuery {
        InventoryItems.select { InventoryItems.categoryId eq categoryId }
            .orderBy(InventoryItems.name to SortOrder.ASC)
            .map { toInventoryItem(it) }
    }

    suspend fun findActive(): List<InventoryItem> = dbQuery {
        InventoryItems.select { InventoryItems.isActive eq true }
            .orderBy(InventoryItems.name to SortOrder.ASC)
            .map { toInventoryItem(it) }
    }

    suspend fun searchByName(searchTerm: String): List<InventoryItem> = dbQuery {
        InventoryItems.select {
            InventoryItems.name.lowerCase() like "%${searchTerm.lowercase()}%"
        }
            .orderBy(InventoryItems.name to SortOrder.ASC)
            .map { toInventoryItem(it) }
    }

    suspend fun update(
        id: UUID,
        name: String? = null,
        description: String? = null,
        categoryId: UUID? = null,
        unitOfMeasure: String? = null,
        unitPrice: BigDecimal? = null,
        reorderLevel: Int? = null,
        reorderQuantity: Int? = null,
        barcode: String? = null,
        imageUrl: String? = null,
        isActive: Boolean? = null
    ): Boolean = dbQuery {
        val updated = InventoryItems.update({ InventoryItems.id eq id }) {
            name?.let { value -> it[InventoryItems.name] = value }
            description?.let { value -> it[InventoryItems.description] = value }
            categoryId?.let { value -> it[InventoryItems.categoryId] = value }
            unitOfMeasure?.let { value -> it[InventoryItems.unitOfMeasure] = value }
            unitPrice?.let { value -> it[InventoryItems.unitPrice] = value }
            reorderLevel?.let { value -> it[InventoryItems.reorderLevel] = value }
            reorderQuantity?.let { value -> it[InventoryItems.reorderQuantity] = value }
            barcode?.let { value -> it[InventoryItems.barcode] = value }
            imageUrl?.let { value -> it[InventoryItems.imageUrl] = value }
            isActive?.let { value -> it[InventoryItems.isActive] = value }
            it[updatedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    suspend fun delete(id: UUID): Boolean = dbQuery {
        InventoryItems.deleteWhere { InventoryItems.id eq id } > 0
    }

    suspend fun count(): Long = dbQuery {
        InventoryItems.selectAll().count()
    }

    private fun toInventoryItem(row: ResultRow): InventoryItem = InventoryItem(
        id = row[InventoryItems.id],
        sku = row[InventoryItems.sku],
        name = row[InventoryItems.name],
        description = row[InventoryItems.description],
        categoryId = row[InventoryItems.categoryId],
        unitOfMeasure = row[InventoryItems.unitOfMeasure],
        unitPrice = row[InventoryItems.unitPrice],
        reorderLevel = row[InventoryItems.reorderLevel],
        reorderQuantity = row[InventoryItems.reorderQuantity],
        barcode = row[InventoryItems.barcode],
        imageUrl = row[InventoryItems.imageUrl],
        isActive = row[InventoryItems.isActive],
        createdAt = row[InventoryItems.createdAt],
        updatedAt = row[InventoryItems.updatedAt]
    )
}
