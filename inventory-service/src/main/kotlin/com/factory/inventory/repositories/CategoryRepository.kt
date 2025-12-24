package com.factory.inventory.repositories

import com.factory.inventory.models.entity.Category
import com.factory.inventory.models.entity.Categories
import com.factory.inventory.config.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.util.UUID

/**
 * Repository for category operations
 */
class CategoryRepository {

    suspend fun create(
        name: String,
        description: String? = null,
        parentCategoryId: UUID? = null
    ): Category = dbQuery {
        val categoryId = Categories.insert {
            it[Categories.name] = name
            it[Categories.description] = description
            it[Categories.parentCategoryId] = parentCategoryId
        } get Categories.id

        findById(categoryId)!!
    }

    suspend fun findById(id: UUID): Category? = dbQuery {
        Categories.select { Categories.id eq id }
            .map { toCategory(it) }
            .singleOrNull()
    }

    suspend fun findByName(name: String): Category? = dbQuery {
        Categories.select { Categories.name eq name }
            .map { toCategory(it) }
            .singleOrNull()
    }

    suspend fun findAll(limit: Int = 100, offset: Long = 0): List<Category> = dbQuery {
        Categories.selectAll()
            .limit(limit, offset)
            .map { toCategory(it) }
    }

    suspend fun findByParentId(parentId: UUID): List<Category> = dbQuery {
        Categories.select { Categories.parentCategoryId eq parentId }
            .map { toCategory(it) }
    }

    suspend fun findRootCategories(): List<Category> = dbQuery {
        Categories.select { Categories.parentCategoryId.isNull() }
            .map { toCategory(it) }
    }

    suspend fun findActive(): List<Category> = dbQuery {
        Categories.select { Categories.isActive eq true }
            .map { toCategory(it) }
    }

    suspend fun update(
        id: UUID,
        name: String? = null,
        description: String? = null,
        parentCategoryId: UUID? = null,
        isActive: Boolean? = null
    ): Boolean = dbQuery {
        val updated = Categories.update({ Categories.id eq id }) {
            name?.let { value -> it[Categories.name] = value }
            description?.let { value -> it[Categories.description] = value }
            parentCategoryId?.let { value -> it[Categories.parentCategoryId] = value }
            isActive?.let { value -> it[Categories.isActive] = value }
            it[updatedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    suspend fun delete(id: UUID): Boolean = dbQuery {
        Categories.deleteWhere { Categories.id eq id } > 0
    }

    suspend fun count(): Long = dbQuery {
        Categories.selectAll().count()
    }

    private fun toCategory(row: ResultRow): Category = Category(
        id = row[Categories.id],
        name = row[Categories.name],
        description = row[Categories.description],
        parentCategoryId = row[Categories.parentCategoryId],
        isActive = row[Categories.isActive],
        createdAt = row[Categories.createdAt],
        updatedAt = row[Categories.updatedAt]
    )
}
