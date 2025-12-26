package com.factory.telegram.bot

import com.factory.telegram.client.AuthServiceClient
import com.factory.telegram.client.InventoryServiceClient
import com.factory.telegram.client.OrderServiceClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("TelegramBot")
private val json = Json { ignoreUnknownKeys = true }

class SimpleTelegramBot(
    private val token: String,
    private val httpClient: HttpClient,
    private val authClient: AuthServiceClient,
    private val inventoryClient: InventoryServiceClient,
    private val orderClient: OrderServiceClient
) {
    private val baseUrl = "https://api.telegram.org/bot$token"
    private var offset: Long = 0
    private var isRunning = false
    private var pollingJob: Job? = null

    fun start() {
        logger.info("Starting Telegram bot polling...")
        isRunning = true
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isRunning) {
                try {
                    pollUpdates()
                    delay(1000) // Poll every second
                } catch (e: Exception) {
                    logger.error("Error in polling loop", e)
                    delay(5000) // Wait longer on error
                }
            }
        }
    }

    fun stop() {
        logger.info("Stopping Telegram bot...")
        isRunning = false
        pollingJob?.cancel()
    }

    private suspend fun pollUpdates() {
        try {
            val response: HttpResponse = httpClient.get("$baseUrl/getUpdates") {
                parameter("offset", offset)
                parameter("timeout", 30)
            }

            val body = response.bodyAsText()
            val updates = json.decodeFromString<UpdatesResponse>(body)

            updates.result.forEach { update ->
                offset = update.update_id + 1
                update.message?.let { message ->
                    handleMessage(message)
                }
            }
        } catch (e: Exception) {
            logger.error("Error polling updates", e)
        }
    }

    private suspend fun handleMessage(message: Message) {
        val text = message.text ?: return
        val chatId = message.chat.id
        val telegramId = message.from?.id ?: return

        logger.info("Received message: $text from user $telegramId")

        when {
            text.startsWith("/start") -> handleStart(chatId, telegramId, message.from)
            text.startsWith("/products") -> handleProducts(chatId, telegramId)
            text.startsWith("/categories") -> handleCategories(chatId)
            text.startsWith("/category") -> handleCategoryProducts(chatId, text)
            text.startsWith("/order") -> handleOrder(chatId, telegramId, text)
            text.startsWith("/myorders") -> handleMyOrders(chatId, telegramId)
            text.startsWith("/help") -> handleHelp(chatId)
            else -> sendMessage(chatId, "Unknown command. Use /help to see available commands.")
        }
    }

    private suspend fun handleStart(chatId: Long, telegramId: Long, from: User?) {
        val firstName = from?.first_name ?: "Customer"
        val lastName = from?.last_name ?: ""

        sendMessage(chatId, "Welcome to Factory Store! 🏭\n\nRegistering you as a customer...")

        try {
            authClient.registerCustomer(
                telegramId = telegramId,
                firstName = firstName,
                lastName = lastName,
                email = "customer_$telegramId@telegram.bot"
            )
            logger.info("Customer $telegramId registered successfully")
        } catch (e: Exception) {
            logger.warn("Customer $telegramId may already be registered: ${e.message}")
        }

        sendMessage(
            chatId,
            """
            ✅ You're all set!

            Available commands:
            /products - Browse all products
            /categories - View product categories
            /category <name> - Browse by category
            /order <id> <qty> - Place an order
            /myorders - View your orders
            /help - Show help

            Use /categories to start shopping!
            """.trimIndent()
        )
    }

    private suspend fun handleProducts(chatId: Long, telegramId: Long) {
        sendMessage(chatId, "Loading products... ⏳")

        try {
            val products = inventoryClient.getProducts()
            if (products.isEmpty()) {
                sendMessage(chatId, "No products available at the moment.")
            } else {
                val productList = products.take(10).joinToString("\n\n") { product ->
                    "📦 ${product.name}\n💰 $$${product.unitPrice}\n🆔 ${product.id}"
                }

                sendMessage(
                    chatId,
                    """
                    🛍️ Available Products:

                    $productList

                    To order: /order <product_id> <quantity>
                    """.trimIndent()
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching products", e)
            sendMessage(chatId, "❌ Error loading products: ${e.message}")
        }
    }

    private suspend fun handleCategories(chatId: Long) {
        sendMessage(chatId, "Loading categories... ⏳")

        try {
            val categories = inventoryClient.getCategories()
            if (categories.isEmpty()) {
                sendMessage(chatId, "No categories available at the moment.")
            } else {
                val categoryList = categories.joinToString("\n") { category ->
                    "📁 ${category.name}\n   🆔 ${category.id}"
                }

                sendMessage(
                    chatId,
                    """
                    📂 Product Categories:

                    $categoryList

                    To browse: /category <category_id>
                    Example: /category ${categories.first().id}
                    """.trimIndent()
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching categories", e)
            sendMessage(chatId, "❌ Error loading categories: ${e.message}")
        }
    }

    private suspend fun handleCategoryProducts(chatId: Long, text: String) {
        val parts = text.split(" ", limit = 2)
        if (parts.size < 2) {
            sendMessage(chatId, "Usage: /category <category_id>\n\nUse /categories to see all categories.")
            return
        }

        val categoryId = parts[1].trim()
        sendMessage(chatId, "Loading products... ⏳")

        try {
            val products = inventoryClient.getProductsByCategory(categoryId)
            if (products.isEmpty()) {
                sendMessage(chatId, "No products found in this category.")
            } else {
                val productList = products.take(10).joinToString("\n\n") { product ->
                    "📦 ${product.name}\n💰 \$${product.unitPrice}\n🆔 ${product.id}"
                }

                sendMessage(
                    chatId,
                    """
                    🛍️ Products in Category:

                    $productList

                    To order: /order <product_id> <quantity>
                    """.trimIndent()
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching products by category", e)
            sendMessage(chatId, "❌ Error loading products: ${e.message}")
        }
    }

    private suspend fun handleOrder(chatId: Long, telegramId: Long, text: String) {
        val parts = text.split(" ")
        if (parts.size < 3) {
            sendMessage(chatId, "Usage: /order <product_id> <quantity>")
            return
        }

        val productId = parts[1]
        val quantity = parts[2].toIntOrNull() ?: 1

        sendMessage(chatId, "Creating your order... ⏳")

        try {
            val token = authClient.loginCustomer(telegramId)
            val order = orderClient.createOrder(
                token = token,
                productId = productId,
                quantity = quantity,
                deliveryAddress = "Telegram Delivery"
            )

            sendMessage(
                chatId,
                """
                ✅ Order created!

                Order ID: ${order.id.take(8)}...
                Total: $$${order.totalAmount}
                Status: ${order.status}

                Track: /myorders
                """.trimIndent()
            )
        } catch (e: Exception) {
            logger.error("Error creating order", e)
            sendMessage(chatId, "❌ Error: ${e.message}")
        }
    }

    private suspend fun handleMyOrders(chatId: Long, telegramId: Long) {
        sendMessage(chatId, "Loading your orders... ⏳")

        try {
            val token = authClient.loginCustomer(telegramId)
            val orders = orderClient.getMyOrders(token)

            if (orders.isEmpty()) {
                sendMessage(chatId, "You haven't placed any orders yet. Use /products to shop!")
            } else {
                val orderList = orders.joinToString("\n\n") { order ->
                    "📋 Order #${order.id.take(8)}\n💰 $$${order.totalAmount}\n📦 ${order.status}"
                }
                sendMessage(chatId, "📦 Your Orders:\n\n$orderList")
            }
        } catch (e: Exception) {
            logger.error("Error fetching orders", e)
            sendMessage(chatId, "❌ Error: ${e.message}")
        }
    }

    private suspend fun handleHelp(chatId: Long) {
        sendMessage(
            chatId,
            """
            🏭 Factory Store Bot

            📋 Commands:
            /start - Register/Start
            /products - Browse all products
            /categories - View categories
            /category <id> - Browse by category
            /order <id> <qty> - Place order
            /myorders - View your orders
            /help - Show this help

            💡 Example:
            1. /categories
            2. /category abc-123
            3. /order product-id 2
            """.trimIndent()
        )
    }

    private suspend fun sendMessage(chatId: Long, text: String) {
        try {
            httpClient.post("$baseUrl/sendMessage") {
                parameter("chat_id", chatId)
                parameter("text", text)
            }
        } catch (e: Exception) {
            logger.error("Error sending message", e)
        }
    }
}

@Serializable
data class UpdatesResponse(
    val ok: Boolean,
    val result: List<Update>
)

@Serializable
data class Update(
    val update_id: Long,
    val message: Message? = null
)

@Serializable
data class Message(
    val message_id: Long,
    val from: User? = null,
    val chat: Chat,
    val text: String? = null
)

@Serializable
data class User(
    val id: Long,
    val first_name: String,
    val last_name: String? = null,
    val username: String? = null
)

@Serializable
data class Chat(
    val id: Long,
    val type: String
)
