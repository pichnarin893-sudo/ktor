package com.natjoub.auth

import com.natjoub.auth.config.DatabaseFactory
import com.natjoub.auth.di.appModule
import com.natjoub.auth.models.dto.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthFlowTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            startKoin {
                modules(appModule)
            }
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            stopKoin()
            DatabaseFactory.close()
        }
    }

    @Test
    fun `test user registration with seller role`() = testApplication {
        application {
            module()
        }

        val response = client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john.seller@example.com",
                    "username": "johnseller",
                    "password": "Test@1234",
                    "role": "seller"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("accessToken"))
        assertTrue(responseBody.contains("refreshToken"))
    }

    @Test
    fun `test user registration with customer role`() = testApplication {
        application {
            module()
        }

        val response = client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "firstName": "Jane",
                    "lastName": "Smith",
                    "email": "jane.customer@example.com",
                    "username": "janesmith",
                    "password": "Test@1234",
                    "role": "customer",
                    "phoneNumber": "+1234567890"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("accessToken"))
        assertTrue(responseBody.contains("user"))
    }

    @Test
    fun `test user registration with admin role should fail`() = testApplication {
        application {
            module()
        }

        val response = client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "firstName": "Admin",
                    "lastName": "User",
                    "email": "admin@example.com",
                    "password": "Test@1234",
                    "role": "admin"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Cannot register as admin"))
    }

    @Test
    fun `test user login with valid credentials`() = testApplication {
        application {
            module()
        }

        // First register a user
        client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "firstName": "Test",
                    "lastName": "Login",
                    "email": "test.login@example.com",
                    "password": "Test@1234",
                    "role": "customer"
                }
                """.trimIndent()
            )
        }

        // Then try to login
        val loginResponse = client.post("/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "identifier": "test.login@example.com",
                    "password": "Test@1234"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status)
        assertTrue(loginResponse.bodyAsText().contains("accessToken"))
        assertTrue(loginResponse.bodyAsText().contains("refreshToken"))
    }

    @Test
    fun `test user login with invalid credentials`() = testApplication {
        application {
            module()
        }

        val response = client.post("/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "identifier": "nonexistent@example.com",
                    "password": "WrongPassword123"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test registration with weak password should fail`() = testApplication {
        application {
            module()
        }

        val response = client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "firstName": "Weak",
                    "lastName": "Password",
                    "email": "weak@example.com",
                    "password": "123",
                    "role": "customer"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("password"))
    }

    @Test
    fun `test duplicate email registration should fail`() = testApplication {
        application {
            module()
        }

        val registerRequest = """
            {
                "firstName": "Duplicate",
                "lastName": "Email",
                "email": "duplicate@example.com",
                "password": "Test@1234",
                "role": "seller"
            }
        """.trimIndent()

        // First registration
        client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerRequest)
        }

        // Second registration with same email
        val response = client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerRequest)
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertTrue(response.bodyAsText().contains("already"))
    }
}
