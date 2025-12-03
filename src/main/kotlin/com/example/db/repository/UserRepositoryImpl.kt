package com.example.db.repository

import com.example.config.DatabaseFactory.dbQuery
import com.example.db.table.Users
import com.example.model.CreateUserRequest
import com.example.model.UpdateUserRequest
import com.example.model.User
import com.example.repository.UserRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class UserRepositoryImpl : UserRepository {
    
    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        email = row[Users.email],
        username = row[Users.username],
        createdAt = row[Users.createdAt],
        updatedAt = row[Users.updatedAt]
    )
    
    override suspend fun findAll(limit: Int, offset: Int): List<User> = dbQuery {
        Users
            .selectAll()
            .limit(limit, offset.toLong())
            .orderBy(Users.createdAt, SortOrder.DESC)
            .map(::resultRowToUser)
    }
    
    override suspend fun findById(id: Long): User? = dbQuery {
        Users
            .selectAll()
            .where { Users.id eq id }
            .map(::resultRowToUser)
            .singleOrNull()
    }
    
    override suspend fun findByEmail(email: String): User? = dbQuery {
        Users
            .selectAll()
            .where { Users.email eq email }
            .map(::resultRowToUser)
            .singleOrNull()
    }
    
    override suspend fun create(request: CreateUserRequest): User = dbQuery {
        val now = LocalDateTime.now()
        
        val id = Users.insert {
            it[email] = request.email
            it[username] = request.username
            it[passwordHash] = hashPassword(request.password)
            it[createdAt] = now
            it[updatedAt] = now
        } get Users.id
        
        User(
            id = id,
            email = request.email,
            username = request.username,
            createdAt = now,
            updatedAt = now
        )
    }
    
    override suspend fun update(id: Long, request: UpdateUserRequest): User? = dbQuery {
        val updated = Users.update({ Users.id eq id }) {
            request.email?.let { email -> it[Users.email] = email }
            request.username?.let { username -> it[Users.username] = username }
            it[updatedAt] = LocalDateTime.now()
        }
        
        if (updated > 0) findById(id) else null
    }
    
    override suspend fun delete(id: Long): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }
    
    private fun hashPassword(password: String): String {
        // In production, use a proper password hashing library like BCrypt
        // This is just a placeholder
        return password.hashCode().toString()
    }
}