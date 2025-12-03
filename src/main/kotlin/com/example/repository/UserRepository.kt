package com.example.repository

import com.example.model.CreateUserRequest
import com.example.model.UpdateUserRequest
import com.example.model.User

interface UserRepository {
    suspend fun findAll(limit: Int, offset: Int): List<User>
    suspend fun findById(id: Long): User?
    suspend fun findByEmail(email: String): User?
    suspend fun create(request: CreateUserRequest): User
    suspend fun update(id: Long, request: UpdateUserRequest): User?
    suspend fun delete(id: Long): Boolean
}