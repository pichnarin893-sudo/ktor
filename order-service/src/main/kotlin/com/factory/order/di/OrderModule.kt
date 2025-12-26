package com.factory.order.di

import com.factory.common.config.JWTConfig
import com.factory.order.client.InventoryServiceClient
import com.factory.order.repositories.OrderRepository
import com.factory.order.repositories.OrderRepositoryImpl
import com.factory.order.services.OrderService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val orderModule = module {
    // JWT Config
    single {
        JWTConfig(
            secret = System.getenv("JWT_SECRET") ?: "your-secret-key-change-this-in-production",
            issuer = System.getenv("JWT_ISSUER") ?: "http://localhost:8083/",
            audience = System.getenv("JWT_AUDIENCE") ?: "http://localhost:8083/api",
            realm = "Order Service"
        )
    }

    // HTTP Client for inter-service communication
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }

    // Clients
    single {
        InventoryServiceClient(
            client = get(),
            inventoryServiceUrl = System.getenv("INVENTORY_SERVICE_URL") ?: "http://localhost:8082"
        )
    }

    // Repositories
    single<OrderRepository> { OrderRepositoryImpl() }

    // Services
    single { OrderService(get(), get()) }
}
