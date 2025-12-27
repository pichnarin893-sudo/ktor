package com.factory.telegram

import com.factory.telegram.bot.SimpleTelegramBot
import com.factory.telegram.client.AuthServiceClient
import com.factory.telegram.client.InventoryServiceClient
import com.factory.telegram.client.OrderServiceClient
import com.factory.telegram.services.ConversationStateManager
import com.factory.telegram.services.SessionManager
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("TelegramBotApp")

fun main() {
    logger.info("Starting Telegram Bot Service...")

    val botToken = System.getenv("TELEGRAM_BOT_TOKEN")
        ?: error("TELEGRAM_BOT_TOKEN environment variable is required")

    val authServiceUrl = System.getenv("AUTH_SERVICE_URL") ?: "http://localhost:8081"
    val inventoryServiceUrl = System.getenv("INVENTORY_SERVICE_URL") ?: "http://localhost:8082"
    val orderServiceUrl = System.getenv("ORDER_SERVICE_URL") ?: "http://localhost:8083"

    // Create HTTP client for service communication
    val httpClient = HttpClient(CIO) {
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

    // Create service clients
    val authClient = AuthServiceClient(httpClient, authServiceUrl)
    val inventoryClient = InventoryServiceClient(httpClient, inventoryServiceUrl)
    val orderClient = OrderServiceClient(httpClient, orderServiceUrl)

    // Create in-memory managers
    val stateManager = ConversationStateManager()
    val sessionManager = SessionManager()

    // Initialize and start bot
    val bot = SimpleTelegramBot(
        token = botToken,
        httpClient = httpClient,
        authClient = authClient,
        inventoryClient = inventoryClient,
        orderClient = orderClient,
        stateManager = stateManager,
        sessionManager = sessionManager
    )
    bot.start()

    logger.info("Telegram Bot Service started successfully")
    logger.info("Sessions are stored in-memory (will be lost on restart)")

    // Keep the application running
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info("Shutting down Telegram Bot Service...")
        bot.stop()
        httpClient.close()
        logger.info("Active sessions: ${sessionManager.getActiveSessionCount()}")
    })

    // Block indefinitely
    Thread.currentThread().join()
}
