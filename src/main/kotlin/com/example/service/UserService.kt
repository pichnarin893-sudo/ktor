package com.example.service

import com.example.exception.ConflictException
import com.example.exception.NotFoundException
import com.example.exception.ValidationException
import com.example.model.CreateUserRequest
import com.example.model.UpdateUserRequest
import com.example.model.User
import com.example.repository.UserRepository
import org.slf4j.LoggerFactory

class UserService(private val userRepository: UserRepository) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    suspend fun getAllUsers(limit: Int = 10, offset: Int = 0): List<User> {
        logger.info("Fetching users with limit: $limit, offset: $offset")
        return userRepository.findAll(limit, offset)
    }
    
    suspend fun getUserById(id: Long): User {
        logger.info("Fetching user with id: $id")
        return userRepository.findById(id)
            ?: throw NotFoundException("User with id $id not found")
    }
    
    suspend fun createUser(request: CreateUserRequest): User {
        logger.info("Creating user with email: ${request.email}")
        
        // Validate input
        validateCreateUserRequest(request)
        
        // Check if user already exists
        userRepository.findByEmail(request.email)?.let {
            throw ConflictException("User with email ${request.email} already exists")
        }
        
        return userRepository.create(request)
    }
    
    suspend fun updateUser(id: Long, request: UpdateUserRequest): User {
        logger.info("Updating user with id: $id")
        
        // Validate input
        validateUpdateUserRequest(request)
        
        // Check if user exists
        userRepository.findById(id)
            ?: throw NotFoundException("User with id $id not found")
        
        // Check email uniqueness if updating email
        request.email?.let { email ->
            userRepository.findByEmail(email)?.let { existingUser ->
                if (existingUser.id != id) {
                    throw ConflictException("Email $email is already taken")
                }
            }
        }
        
        return userRepository.update(id, request)
            ?: throw NotFoundException("User with id $id not found")
    }
    
    suspend fun deleteUser(id: Long) {
        logger.info("Deleting user with id: $id")
        
        val deleted = userRepository.delete(id)
        if (!deleted) {
            throw NotFoundException("User with id $id not found")
        }
    }
    
    private fun validateCreateUserRequest(request: CreateUserRequest) {
        if (request.email.isBlank()) {
            throw ValidationException("Email cannot be blank")
        }
        if (!isValidEmail(request.email)) {
            throw ValidationException("Invalid email format")
        }
        if (request.username.isBlank()) {
            throw ValidationException("Username cannot be blank")
        }
        if (request.username.length < 3) {
            throw ValidationException("Username must be at least 3 characters")
        }
        if (request.password.length < 8) {
            throw ValidationException("Password must be at least 8 characters")
        }
    }
    
    private fun validateUpdateUserRequest(request: UpdateUserRequest) {
        request.email?.let { email ->
            if (email.isBlank()) {
                throw ValidationException("Email cannot be blank")
            }
            if (!isValidEmail(email)) {
                throw ValidationException("Invalid email format")
            }
        }
        
        request.username?.let { username ->
            if (username.isBlank()) {
                throw ValidationException("Username cannot be blank")
            }
            if (username.length < 3) {
                throw ValidationException("Username must be at least 3 characters")
            }
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}