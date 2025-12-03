package com.example.api.routes

import com.example.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class UserRoutesTest {
    
    @Test
    fun `GET health endpoint should return OK`() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
    }
    
    @Test
    fun `GET root endpoint should return welcome message`() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Welcome to Ktor Microservice!", response.bodyAsText())
    }
    
    // Note: For full integration tests with database, you would need to:
    // 1. Setup test database (H2 in-memory)
    // 2. Run migrations
    // 3. Test full CRUD operations
}