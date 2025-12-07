package com.natjoub.auth.repositories

import com.natjoub.auth.models.entity.Role
import com.natjoub.auth.models.entity.Roles
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * Repository interface for Role operations
 */
interface RoleRepository {
    suspend fun findById(id: UUID): Role?
    suspend fun findByName(name: String): Role?
    suspend fun findAll(): List<Role>
}

/**
 * Implementation of RoleRepository using Exposed ORM
 */
class RoleRepositoryImpl : RoleRepository {

    private fun resultRowToRole(row: ResultRow): Role {
        return Role(
            id = row[Roles.id],
            name = row[Roles.name],
            createdAt = row[Roles.createdAt],
            updatedAt = row[Roles.updatedAt]
        )
    }

    override suspend fun findById(id: UUID): Role? = transaction {
        Roles.select { Roles.id eq id }
            .map { resultRowToRole(it) }
            .singleOrNull()
    }

    override suspend fun findByName(name: String): Role? = transaction {
        Roles.select { Roles.name eq name }
            .map { resultRowToRole(it) }
            .singleOrNull()
    }

    override suspend fun findAll(): List<Role> = transaction {
        Roles.select { Roles.id.isNotNull() }
            .map { resultRowToRole(it) }
    }
}
