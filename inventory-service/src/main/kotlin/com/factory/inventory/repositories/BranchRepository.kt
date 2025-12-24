package com.factory.inventory.repositories

import com.factory.inventory.models.entity.Branch
import com.factory.inventory.models.entity.Branches
import com.factory.inventory.config.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.util.UUID

/**
 * Repository for branch operations
 */
class BranchRepository {

    suspend fun create(
        name: String,
        code: String,
        address: String? = null,
        city: String? = null,
        country: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
        managerName: String? = null
    ): Branch = dbQuery {
        val branchId = Branches.insert {
            it[Branches.name] = name
            it[Branches.code] = code
            it[Branches.address] = address
            it[Branches.city] = city
            it[Branches.country] = country
            it[Branches.phoneNumber] = phoneNumber
            it[Branches.email] = email
            it[Branches.managerName] = managerName
        } get Branches.id

        findById(branchId)!!
    }

    suspend fun findById(id: UUID): Branch? = dbQuery {
        Branches.select { Branches.id eq id }
            .map { toBranch(it) }
            .singleOrNull()
    }

    suspend fun findByCode(code: String): Branch? = dbQuery {
        Branches.select { Branches.code eq code }
            .map { toBranch(it) }
            .singleOrNull()
    }

    suspend fun findAll(limit: Int = 100, offset: Long = 0): List<Branch> = dbQuery {
        Branches.selectAll()
            .limit(limit, offset)
            .map { toBranch(it) }
    }

    suspend fun findActive(): List<Branch> = dbQuery {
        Branches.select { Branches.isActive eq true }
            .map { toBranch(it) }
    }

    suspend fun update(
        id: UUID,
        name: String? = null,
        address: String? = null,
        city: String? = null,
        country: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
        managerName: String? = null,
        isActive: Boolean? = null
    ): Boolean = dbQuery {
        val updated = Branches.update({ Branches.id eq id }) {
            name?.let { value -> it[Branches.name] = value }
            address?.let { value -> it[Branches.address] = value }
            city?.let { value -> it[Branches.city] = value }
            country?.let { value -> it[Branches.country] = value }
            phoneNumber?.let { value -> it[Branches.phoneNumber] = value }
            email?.let { value -> it[Branches.email] = value }
            managerName?.let { value -> it[Branches.managerName] = value }
            isActive?.let { value -> it[Branches.isActive] = value }
            it[updatedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    suspend fun delete(id: UUID): Boolean = dbQuery {
        Branches.deleteWhere { Branches.id eq id } > 0
    }

    suspend fun count(): Long = dbQuery {
        Branches.selectAll().count()
    }

    private fun toBranch(row: ResultRow): Branch = Branch(
        id = row[Branches.id],
        name = row[Branches.name],
        code = row[Branches.code],
        address = row[Branches.address],
        city = row[Branches.city],
        country = row[Branches.country],
        phoneNumber = row[Branches.phoneNumber],
        email = row[Branches.email],
        managerName = row[Branches.managerName],
        isActive = row[Branches.isActive],
        createdAt = row[Branches.createdAt],
        updatedAt = row[Branches.updatedAt]
    )
}
