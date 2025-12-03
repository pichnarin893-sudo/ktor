package com.example.domain.service

import com.example.exception.NotFoundException
import com.example.exception.ValidationException
import com.example.model.CreateUserRequest
import com.example.model.User
import com.example.repository.UserRepository
import com.example.service.UserService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserServiceTest {
    
    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService
    
    @Before
    fun setup() {
        userRepository = mockk()
        userService = UserService(userRepository)
    }
    
    @Test
    fun `getUserById should return user when user exists`() = runBlocking {
        // Given
        val userId = 1L
        val expectedUser = User(
            id = userId,
            email = "test@example.com",
            username = "testuser",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        coEvery { userRepository.findById(userId) } returns expectedUser
        
        // When
        val result = userService.getUserById(userId)
        
        // Then
        assertEquals(expectedUser, result)
        coVerify { userRepository.findById(userId) }
    }
    
    @Test
    fun `getUserById should throw NotFoundException when user does not exist`() = runBlocking {
        // Given
        val userId = 999L
        coEvery { userRepository.findById(userId) } returns null
        
        // When & Then
        assertFailsWith<NotFoundException> {
            userService.getUserById(userId)
        }
    }
    
    @Test
    fun `createUser should validate email format`() = runBlocking {
        // Given
        val request = CreateUserRequest(
            email = "invalid-email",
            username = "testuser",
            password = "password123"
        )
        
        // When & Then
        assertFailsWith<ValidationException> {
            userService.createUser(request)
        }
    }
    
    @Test
    fun `createUser should validate password length`() = runBlocking {
        // Given
        val request = CreateUserRequest(
            email = "test@example.com",
            username = "testuser",
            password = "short"
        )
        
        // When & Then
        assertFailsWith<ValidationException> {
            userService.createUser(request)
        }
    }
}