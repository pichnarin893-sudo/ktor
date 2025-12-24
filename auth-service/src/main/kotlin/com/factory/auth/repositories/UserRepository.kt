package com.factory.auth.repositories

import com.factory.auth.models.entity.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Repository interface for User operations
 */
interface UserRepository {
    suspend fun create(
        firstName: String,
        lastName: String,
        roleId: UUID,
        dob: LocalDate?,
        gender: String?
    ): User

    suspend fun findById(id: UUID): User?
    suspend fun findByRoleId(roleId: UUID): List<User>
    suspend fun findAll(limit: Int = 100, offset: Int = 0): List<User>
    suspend fun findAllByStatus(isActive: Boolean, limit: Int = 100, offset: Int = 0): List<User>
    suspend fun update(id: UUID, firstName: String?, lastName: String?, dob: LocalDate?, gender: String?): User?
    suspend fun updateStatus(id: UUID, isActive: Boolean): User?
    suspend fun delete(id: UUID): Boolean
    suspend fun count(): Int
    suspend fun countByStatus(isActive: Boolean): Int
    suspend fun getUserWithCredentialsAndRole(userId: UUID): UserWithCredentialsAndRole?
}

/**
 * Implementation of UserRepository using Exposed ORM
 */
class UserRepositoryImpl : UserRepository {

    private fun resultRowToUser(row: ResultRow): User {
        return User(
            id = row[Users.id],
            firstName = row[Users.firstName],
            lastName = row[Users.lastName],
            dob = row[Users.dob],
            gender = row[Users.gender],
            roleId = row[Users.roleId],
            isActive = row[Users.isActive],
            createdAt = row[Users.createdAt],
            updatedAt = row[Users.updatedAt]
        )
    }

    override suspend fun create(
        firstName: String,
        lastName: String,
        roleId: UUID,
        dob: LocalDate?,
        gender: String?
    ): User = transaction {
        val userId = UUID.randomUUID()
        val now = LocalDateTime.now()

        Users.insert {
            it[id] = userId
            it[Users.firstName] = firstName
            it[Users.lastName] = lastName
            it[Users.roleId] = roleId
            it[Users.dob] = dob
            it[Users.gender] = gender
            it[isActive] = true
            it[createdAt] = now
            it[updatedAt] = now
        }

        User(userId, firstName, lastName, dob, gender, roleId, true, now, now)
    }

    override suspend fun findById(id: UUID): User? = transaction {
        Users.select { Users.id eq id }
            .map { resultRowToUser(it) }
            .singleOrNull()
    }

    override suspend fun findByRoleId(roleId: UUID): List<User> = transaction {
        Users.select { Users.roleId eq roleId }
            .map { resultRowToUser(it) }
    }

    override suspend fun findAll(limit: Int, offset: Int): List<User> = transaction {
        Users.select { Users.id.isNotNull() }
            .limit(limit, offset.toLong())
            .map { resultRowToUser(it) }
    }

    override suspend fun findAllByStatus(isActive: Boolean, limit: Int, offset: Int): List<User> = transaction {
        Users.select { Users.isActive eq isActive }
            .limit(limit, offset.toLong())
            .map { resultRowToUser(it) }
    }

    override suspend fun update(
        id: UUID,
        firstName: String?,
        lastName: String?,
        dob: LocalDate?,
        gender: String?
    ): User? {
        transaction {
            Users.update({ Users.id eq id }) { stmt ->
                firstName?.let { name -> stmt[Users.firstName] = name }
                lastName?.let { name -> stmt[Users.lastName] = name }
                dob?.let { date -> stmt[Users.dob] = date }
                gender?.let { g -> stmt[Users.gender] = g }
                stmt[updatedAt] = LocalDateTime.now()
            }
        }
        return findById(id)
    }

    override suspend fun updateStatus(id: UUID, isActive: Boolean): User? {
        transaction {
            Users.update({ Users.id eq id }) { stmt ->
                stmt[Users.isActive] = isActive
                stmt[updatedAt] = LocalDateTime.now()
            }
        }
        return findById(id)
    }

    override suspend fun delete(id: UUID): Boolean = transaction {
        Users.deleteWhere { Users.id eq id } > 0
    }

    override suspend fun count(): Int = transaction {
        Users.select { Users.id.isNotNull() }.count().toInt()
    }

    override suspend fun countByStatus(isActive: Boolean): Int = transaction {
        Users.select { Users.isActive eq isActive }.count().toInt()
    }

    override suspend fun getUserWithCredentialsAndRole(userId: UUID): UserWithCredentialsAndRole? = transaction {
        (Users innerJoin Credentials innerJoin Roles)
            .select { Users.id eq userId }
            .map { row ->
                UserWithCredentialsAndRole(
                    user = resultRowToUser(row),
                    credential = Credential(
                        id = row[Credentials.id],
                        userId = row[Credentials.userId],
                        email = row[Credentials.email],
                        username = row[Credentials.username],
                        phoneNumber = row[Credentials.phoneNumber],
                        password = row[Credentials.password],
                        otp = row[Credentials.otp],
                        otpExpiry = row[Credentials.otpExpiry],
                        isVerified = row[Credentials.isVerified],
                        createdAt = row[Credentials.createdAt],
                        updatedAt = row[Credentials.updatedAt]
                    ),
                    role = Role(
                        id = row[Roles.id],
                        name = row[Roles.name],
                        createdAt = row[Roles.createdAt],
                        updatedAt = row[Roles.updatedAt]
                    )
                )
            }
            .singleOrNull()
    }
}
