package com.natjoub.inventory.repositories

import com.natjoub.inventory.models.entity.*
import com.natjoub.inventory.config.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Repository for stock movement operations
 */
class StockMovementRepository {

    suspend fun create(
        inventoryItemId: UUID,
        fromBranchId: UUID? = null,
        toBranchId: UUID? = null,
        movementType: MovementType,
        quantity: Int,
        unitPrice: BigDecimal? = null,
        referenceNumber: String? = null,
        notes: String? = null,
        performedBy: UUID? = null
    ): StockMovement = dbQuery {
        val movementId = StockMovements.insert {
            it[StockMovements.inventoryItemId] = inventoryItemId
            it[StockMovements.fromBranchId] = fromBranchId
            it[StockMovements.toBranchId] = toBranchId
            it[StockMovements.movementType] = movementType.name
            it[StockMovements.quantity] = quantity
            it[StockMovements.unitPrice] = unitPrice
            it[StockMovements.referenceNumber] = referenceNumber
            it[StockMovements.notes] = notes
            it[StockMovements.performedBy] = performedBy
        } get StockMovements.id

        findById(movementId)!!
    }

    suspend fun findById(id: UUID): StockMovement? = dbQuery {
        StockMovements.select { StockMovements.id eq id }
            .map { toStockMovement(it) }
            .singleOrNull()
    }

    suspend fun findByInventoryItemId(
        inventoryItemId: UUID,
        limit: Int = 100,
        offset: Long = 0
    ): List<StockMovement> = dbQuery {
        StockMovements.select { StockMovements.inventoryItemId eq inventoryItemId }
            .orderBy(StockMovements.createdAt to SortOrder.DESC)
            .limit(limit, offset)
            .map { toStockMovement(it) }
    }

    suspend fun findByBranchId(
        branchId: UUID,
        limit: Int = 100,
        offset: Long = 0
    ): List<StockMovement> = dbQuery {
        StockMovements.select {
            (StockMovements.fromBranchId eq branchId) or (StockMovements.toBranchId eq branchId)
        }
            .orderBy(StockMovements.createdAt to SortOrder.DESC)
            .limit(limit, offset)
            .map { toStockMovement(it) }
    }

    suspend fun findByMovementType(
        movementType: MovementType,
        limit: Int = 100,
        offset: Long = 0
    ): List<StockMovement> = dbQuery {
        StockMovements.select { StockMovements.movementType eq movementType.name }
            .orderBy(StockMovements.createdAt to SortOrder.DESC)
            .limit(limit, offset)
            .map { toStockMovement(it) }
    }

    suspend fun findByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        limit: Int = 100,
        offset: Long = 0
    ): List<StockMovement> = dbQuery {
        StockMovements.select {
            (StockMovements.createdAt greaterEq startDate) and
                    (StockMovements.createdAt lessEq endDate)
        }
            .orderBy(StockMovements.createdAt to SortOrder.DESC)
            .limit(limit, offset)
            .map { toStockMovement(it) }
    }

    suspend fun findByReferenceNumber(referenceNumber: String): List<StockMovement> = dbQuery {
        StockMovements.select { StockMovements.referenceNumber eq referenceNumber }
            .orderBy(StockMovements.createdAt to SortOrder.DESC)
            .map { toStockMovement(it) }
    }

    suspend fun findAll(limit: Int = 100, offset: Long = 0): List<StockMovement> = dbQuery {
        StockMovements.selectAll()
            .orderBy(StockMovements.createdAt to SortOrder.DESC)
            .limit(limit, offset)
            .map { toStockMovement(it) }
    }

    suspend fun delete(id: UUID): Boolean = dbQuery {
        StockMovements.deleteWhere { StockMovements.id eq id } > 0
    }

    suspend fun count(): Long = dbQuery {
        StockMovements.selectAll().count()
    }

    suspend fun countByType(movementType: MovementType): Long = dbQuery {
        StockMovements.select { StockMovements.movementType eq movementType.name }.count()
    }

    private fun toStockMovement(row: ResultRow): StockMovement = StockMovement(
        id = row[StockMovements.id],
        inventoryItemId = row[StockMovements.inventoryItemId],
        fromBranchId = row[StockMovements.fromBranchId],
        toBranchId = row[StockMovements.toBranchId],
        movementType = MovementType.valueOf(row[StockMovements.movementType]),
        quantity = row[StockMovements.quantity],
        unitPrice = row[StockMovements.unitPrice],
        referenceNumber = row[StockMovements.referenceNumber],
        notes = row[StockMovements.notes],
        performedBy = row[StockMovements.performedBy],
        createdAt = row[StockMovements.createdAt]
    )
}
