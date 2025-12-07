package com.natjoub.inventory.repositories

import com.natjoub.inventory.models.entity.*
import com.natjoub.core.config.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.util.UUID

/**
 * Repository for stock level operations
 */
class StockLevelRepository {

    suspend fun create(
        inventoryItemId: UUID,
        branchId: UUID,
        quantity: Int = 0,
        reservedQuantity: Int = 0
    ): StockLevel = dbQuery {
        val stockLevelId = StockLevels.insert {
            it[StockLevels.inventoryItemId] = inventoryItemId
            it[StockLevels.branchId] = branchId
            it[StockLevels.quantity] = quantity
            it[StockLevels.reservedQuantity] = reservedQuantity
        } get StockLevels.id

        findById(stockLevelId)!!
    }

    suspend fun findById(id: UUID): StockLevel? = dbQuery {
        StockLevels.select { StockLevels.id eq id }
            .map { toStockLevel(it) }
            .singleOrNull()
    }

    suspend fun findByItemAndBranch(inventoryItemId: UUID, branchId: UUID): StockLevel? = dbQuery {
        StockLevels.select {
            (StockLevels.inventoryItemId eq inventoryItemId) and
                    (StockLevels.branchId eq branchId)
        }
            .map { toStockLevel(it) }
            .singleOrNull()
    }

    suspend fun findByInventoryItemId(inventoryItemId: UUID): List<StockLevel> = dbQuery {
        StockLevels.select { StockLevels.inventoryItemId eq inventoryItemId }
            .map { toStockLevel(it) }
    }

    suspend fun findByBranchId(branchId: UUID, limit: Int = 100, offset: Long = 0): List<StockLevel> = dbQuery {
        StockLevels.select { StockLevels.branchId eq branchId }
            .limit(limit, offset)
            .map { toStockLevel(it) }
    }

    suspend fun findAll(limit: Int = 100, offset: Long = 0): List<StockLevel> = dbQuery {
        StockLevels.selectAll()
            .limit(limit, offset)
            .map { toStockLevel(it) }
    }

    suspend fun findLowStock(): List<StockLevel> = dbQuery {
        (StockLevels innerJoin InventoryItems).select {
            StockLevels.quantity lessEq InventoryItems.reorderLevel
        }.map { row ->
            toStockLevel(row)
        }
    }

    suspend fun getTotalStockForItem(inventoryItemId: UUID): Int = dbQuery {
        StockLevels.slice(StockLevels.quantity.sum())
            .select { StockLevels.inventoryItemId eq inventoryItemId }
            .map { it[StockLevels.quantity.sum()] ?: 0 }
            .firstOrNull() ?: 0
    }

    suspend fun updateQuantity(
        inventoryItemId: UUID,
        branchId: UUID,
        quantity: Int
    ): Boolean = dbQuery {
        val updated = StockLevels.update({
            (StockLevels.inventoryItemId eq inventoryItemId) and
                    (StockLevels.branchId eq branchId)
        }) {
            it[StockLevels.quantity] = quantity
            it[lastCountedAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    suspend fun adjustQuantity(
        inventoryItemId: UUID,
        branchId: UUID,
        delta: Int
    ): Boolean = dbQuery {
        val current = findByItemAndBranch(inventoryItemId, branchId)
        if (current != null) {
            val newQuantity = (current.quantity + delta).coerceAtLeast(0)
            updateQuantity(inventoryItemId, branchId, newQuantity)
        } else {
            if (delta > 0) {
                create(inventoryItemId, branchId, delta)
                true
            } else {
                false
            }
        }
    }

    suspend fun reserveStock(
        inventoryItemId: UUID,
        branchId: UUID,
        quantity: Int
    ): Boolean = dbQuery {
        val stockLevel = findByItemAndBranch(inventoryItemId, branchId) ?: return@dbQuery false
        if (stockLevel.quantity - stockLevel.reservedQuantity < quantity) {
            return@dbQuery false
        }

        val updated = StockLevels.update({
            (StockLevels.inventoryItemId eq inventoryItemId) and
                    (StockLevels.branchId eq branchId)
        }) {
            it[reservedQuantity] = stockLevel.reservedQuantity + quantity
            it[updatedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    suspend fun releaseReservedStock(
        inventoryItemId: UUID,
        branchId: UUID,
        quantity: Int
    ): Boolean = dbQuery {
        val stockLevel = findByItemAndBranch(inventoryItemId, branchId) ?: return@dbQuery false

        val updated = StockLevels.update({
            (StockLevels.inventoryItemId eq inventoryItemId) and
                    (StockLevels.branchId eq branchId)
        }) {
            it[reservedQuantity] = (stockLevel.reservedQuantity - quantity).coerceAtLeast(0)
            it[updatedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    suspend fun delete(id: UUID): Boolean = dbQuery {
        StockLevels.deleteWhere { StockLevels.id eq id } > 0
    }

    suspend fun count(): Long = dbQuery {
        StockLevels.selectAll().count()
    }

    private fun toStockLevel(row: ResultRow): StockLevel = StockLevel(
        id = row[StockLevels.id],
        inventoryItemId = row[StockLevels.inventoryItemId],
        branchId = row[StockLevels.branchId],
        quantity = row[StockLevels.quantity],
        reservedQuantity = row[StockLevels.reservedQuantity],
        lastCountedAt = row[StockLevels.lastCountedAt],
        updatedAt = row[StockLevels.updatedAt]
    )
}
