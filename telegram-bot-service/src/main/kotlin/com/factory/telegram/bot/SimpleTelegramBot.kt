package com.factory.telegram.bot

import com.factory.telegram.client.AuthServiceClient
import com.factory.telegram.client.InventoryServiceClient
import com.factory.telegram.client.OrderServiceClient
import com.factory.telegram.models.RegistrationStep
import com.factory.telegram.services.SessionManager
import com.factory.telegram.services.ConversationStateManager
import com.factory.telegram.utils.ValidationUtils
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
    private val orderClient: OrderServiceClient,
    private val stateManager: ConversationStateManager,
    private val sessionManager: SessionManager
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

        // Check if user is in a conversation state
        val state = stateManager.getState(telegramId)

        if (state != null && state.step != RegistrationStep.IDLE) {
            // User is in registration flow
            handleRegistrationStep(chatId, telegramId, text, message.from)
        } else {
            // Handle regular commands
            when {
                text.startsWith("/start") -> handleStart(chatId, telegramId, message.from)
                text.startsWith("/register") -> handleRegister(chatId, telegramId)
                text.startsWith("/products") -> handleProducts(chatId, telegramId)
                text.startsWith("/categories") -> handleCategories(chatId)
                text.startsWith("/category") -> handleCategoryProducts(chatId, text)
                text.startsWith("/order") -> handleOrder(chatId, telegramId, text)
                text.startsWith("/myorders") -> handleMyOrders(chatId, telegramId)
                text.startsWith("/help") -> handleHelp(chatId)
                else -> sendMessage(chatId, "Unknown command. Use /help to see available commands.")
            }
        }
    }

    private suspend fun handleStart(chatId: Long, telegramId: Long, from: User?) {
        val firstName = from?.first_name ?: "Customer"

        // Check if user already has a session
        val existingSession = sessionManager.getSession(telegramId)

        if (existingSession != null) {
            sendMessage(
                chatId,
                """
                Welcome back, $firstName! 👋

                You're already registered! Here are your options:

                /products - Browse all products
                /categories - View product categories
                /category <id> - Browse by category
                /order <id> <qty> - Place an order
                /myorders - View your orders
                /help - Show help

                Start shopping with /categories!
                """.trimIndent()
            )
        } else {
            sendMessage(
                chatId,
                """
                Welcome to Factory Store! 🏭

                Hi $firstName! To start shopping, you need to register.

                Use /register to create your account
                Use /help to see all commands
                """.trimIndent()
            )
        }
    }

    private suspend fun handleRegister(chatId: Long, telegramId: Long) {
        // Check if already registered
        val existingSession = sessionManager.getSession(telegramId)
        if (existingSession != null) {
            sendMessage(chatId, "You're already registered! Use /help to see available commands.")
            return
        }

        stateManager.setState(telegramId, RegistrationStep.AWAITING_FIRST_NAME)
        sendMessage(
            chatId,
            """
            Let's get you registered! 📝

            Please enter your first name:
            """.trimIndent()
        )
    }

    private suspend fun handleRegistrationStep(chatId: Long, telegramId: Long, text: String, from: User?) {
        val state = stateManager.getState(telegramId) ?: return

        // Check if user is trying to send a command during registration
        if (text.startsWith("/")) {
            when {
                text.startsWith("/cancel") -> {
                    stateManager.clearState(telegramId)
                    sendMessage(
                        chatId,
                        """
                        ❌ Registration cancelled.

                        Use /register to start again
                        Use /help to see available commands
                        """.trimIndent()
                    )
                    return
                }
                text.startsWith("/register") -> {
                    // User wants to restart registration
                    stateManager.clearState(telegramId)
                    handleRegister(chatId, telegramId)
                    return
                }
                text.startsWith("/resendotp") -> {
                    // Only allow during OTP verification
                    if (state.step == RegistrationStep.AWAITING_OTP) {
                        handleResendOTP(chatId, telegramId)
                        return
                    } else {
                        sendMessage(
                            chatId,
                            """
                            ❌ This command is only available during OTP verification.

                            Please provide the requested information, or use /cancel to exit registration.
                            """.trimIndent()
                        )
                        return
                    }
                }
                else -> {
                    sendMessage(
                        chatId,
                        """
                        ❌ Commands are not allowed during registration.

                        Please provide the requested information, or use /cancel to exit registration.
                        """.trimIndent()
                    )
                    return
                }
            }
        }

        when (state.step) {
            RegistrationStep.AWAITING_FIRST_NAME -> {
                if (text.isBlank() || text.length > 100) {
                    sendMessage(chatId, "❌ Invalid name. Please enter a valid first name (1-100 characters):")
                    return
                }
                if (!text.all { it.isLetter() || it == ' ' || it == '-' || it == '\'' }) {
                    sendMessage(chatId, "❌ First name can only contain letters, spaces, hyphens, and apostrophes. Please try again:")
                    return
                }
                stateManager.saveData(telegramId, "firstName", text.trim())
                stateManager.setState(telegramId, RegistrationStep.AWAITING_LAST_NAME)
                sendMessage(chatId, "✅ First name saved!\n\nNow enter your last name:")
            }

            RegistrationStep.AWAITING_LAST_NAME -> {
                if (text.isBlank() || text.length > 100) {
                    sendMessage(chatId, "❌ Invalid name. Please enter a valid last name (1-100 characters):")
                    return
                }
                if (!text.all { it.isLetter() || it == ' ' || it == '-' || it == '\'' }) {
                    sendMessage(chatId, "❌ Last name can only contain letters, spaces, hyphens, and apostrophes. Please try again:")
                    return
                }
                stateManager.saveData(telegramId, "lastName", text.trim())
                stateManager.setState(telegramId, RegistrationStep.AWAITING_EMAIL)
                sendMessage(chatId, "✅ Last name saved!\n\nEnter your email address:")
            }

            RegistrationStep.AWAITING_EMAIL -> {
                if (!ValidationUtils.isValidEmail(text)) {
                    sendMessage(chatId, "❌ Invalid email format. Please enter a valid email address:")
                    return
                }
                stateManager.saveData(telegramId, "email", text.trim())
                stateManager.setState(telegramId, RegistrationStep.AWAITING_PHONE)
                sendMessage(chatId, "✅ Email saved!\n\nEnter your phone number (international format, e.g., +1234567890):")
            }

            RegistrationStep.AWAITING_PHONE -> {
                if (!ValidationUtils.isValidPhone(text)) {
                    sendMessage(chatId, "❌ Invalid phone number. Use international format (+1234567890):")
                    return
                }
                stateManager.saveData(telegramId, "phone", text.trim())
                stateManager.setState(telegramId, RegistrationStep.AWAITING_USERNAME)
                sendMessage(chatId, "✅ Phone saved!\n\nCreate a username (3-20 characters, letters, numbers, underscore only):")
            }

            RegistrationStep.AWAITING_USERNAME -> {
                if (!ValidationUtils.isValidUsername(text)) {
                    sendMessage(chatId, "❌ Invalid username. Must be 3-20 characters (letters, numbers, underscore only):")
                    return
                }
                stateManager.saveData(telegramId, "username", text.trim())
                stateManager.setState(telegramId, RegistrationStep.AWAITING_PASSWORD)
                sendMessage(
                    chatId,
                    """
                    ✅ Username saved!

                    Create a strong password:
                    • At least 8 characters
                    • Include uppercase and lowercase letters
                    • Include at least one number

                    Enter your password:
                    """.trimIndent()
                )
            }

            RegistrationStep.AWAITING_PASSWORD -> {
                if (!ValidationUtils.isValidPassword(text)) {
                    val message = ValidationUtils.getPasswordStrengthMessage(text)
                    sendMessage(chatId, "❌ $message\n\nPlease try again:")
                    return
                }

                // All data collected, register the user
                val data = stateManager.getData(telegramId)

                sendMessage(chatId, "Creating your account... ⏳")

                try {
                    val response = authClient.registerCustomer(
                        telegramId = telegramId,
                        firstName = data["firstName"]!!,
                        lastName = data["lastName"]!!,
                        email = data["email"]!!,
                        username = data["username"],
                        phoneNumber = data["phone"],
                        password = text
                    )

                    // Store response for later use after OTP verification
                    stateManager.saveData(telegramId, "userId", response.user.id)
                    stateManager.saveData(telegramId, "accessToken", response.accessToken)
                    stateManager.saveData(telegramId, "refreshToken", response.refreshToken)
                    stateManager.saveData(telegramId, "expiresIn", response.expiresIn.toString())
                    stateManager.saveData(telegramId, "userFirstName", response.user.firstName)
                    stateManager.saveData(telegramId, "userLastName", response.user.lastName)

                    // Transition to OTP verification
                    stateManager.setState(telegramId, RegistrationStep.AWAITING_OTP)

                    sendMessage(
                        chatId,
                        """
                        ✅ Account created!

                        📧 A 6-digit OTP has been sent to ${data["email"]}

                        ⚠️ NOTE: Email is not configured yet. Check the auth-service logs for your OTP:
                        Run: docker-compose logs auth-service | grep "OTP generated"

                        Please enter your OTP to verify your account:
                        """.trimIndent()
                    )

                    logger.info("User $telegramId registered, awaiting OTP verification")

                } catch (e: Exception) {
                    logger.error("Registration failed for user $telegramId", e)
                    sendMessage(
                        chatId,
                        """
                        ❌ Registration failed: ${e.message}

                        Please try again with /register
                        """.trimIndent()
                    )
                    stateManager.clearState(telegramId)
                }
            }

            RegistrationStep.AWAITING_OTP -> {
                // Validate OTP format (6 digits)
                if (!text.matches(Regex("^\\d{6}$"))) {
                    sendMessage(chatId, "❌ Invalid OTP format. Please enter a 6-digit code:")
                    return
                }

                val data = stateManager.getData(telegramId)
                val email = data["email"] ?: run {
                    sendMessage(chatId, "❌ Session expired. Please use /register to start again.")
                    stateManager.clearState(telegramId)
                    return
                }

                sendMessage(chatId, "Verifying OTP... ⏳")

                try {
                    // Verify OTP with auth service
                    authClient.verifyOTP(email, text.trim())

                    // OTP verified! Now save the session
                    val userId = data["userId"]!!
                    val accessToken = data["accessToken"]!!
                    val refreshToken = data["refreshToken"]!!
                    val expiresIn = data["expiresIn"]!!.toLong()
                    val firstName = data["userFirstName"]!!
                    val lastName = data["userLastName"]!!

                    sessionManager.saveSession(
                        telegramId = telegramId,
                        userId = userId,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        accessExpiresAt = System.currentTimeMillis() + (expiresIn * 1000),
                        refreshExpiresAt = System.currentTimeMillis() + (7 * 24 * 3600 * 1000L)
                    )

                    stateManager.clearState(telegramId)

                    sendMessage(
                        chatId,
                        """
                        ✅ Account verified successfully! 🎉

                        Welcome $firstName $lastName!

                        Your account is now active and ready to use.

                        Available commands:
                        /products - Browse all products
                        /categories - View categories
                        /order <id> <qty> - Place an order
                        /myorders - View your orders
                        /help - Show all commands

                        Use /categories to start shopping!
                        """.trimIndent()
                    )

                    logger.info("User $telegramId verified successfully via OTP")

                } catch (e: Exception) {
                    logger.error("OTP verification failed for user $telegramId", e)
                    sendMessage(
                        chatId,
                        """
                        ❌ OTP verification failed: ${e.message}

                        Please try again or use /resendotp to get a new code.
                        """.trimIndent()
                    )
                    // Don't clear state - allow user to retry
                }
            }

            else -> {
                // Should not happen
                stateManager.clearState(telegramId)
            }
        }
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
            val token = getValidToken(telegramId)
            if (token == null) {
                sendMessage(chatId, "❌ You need to register first. Use /register to create an account.")
                return
            }

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
            val token = getValidToken(telegramId)
            if (token == null) {
                sendMessage(chatId, "❌ You need to register first. Use /register to create an account.")
                return
            }

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

    // Get valid token for a user (auto-refresh if expired)
    private suspend fun getValidToken(telegramId: Long): String? {
        val session = sessionManager.getSession(telegramId) ?: return null

        // Check if access token is still valid (with 5 minute buffer)
        if (sessionManager.isAccessTokenValid(telegramId, bufferMinutes = 5)) {
            return session.accessToken
        }

        // Access token expired, use refresh token
        try {
            logger.info("Access token expired for user $telegramId, refreshing...")
            val newTokens = authClient.refreshToken(session.refreshToken)

            sessionManager.saveSession(
                telegramId = telegramId,
                userId = session.userId,
                accessToken = newTokens.accessToken,
                refreshToken = newTokens.refreshToken,
                accessExpiresAt = System.currentTimeMillis() + (newTokens.expiresIn * 1000),
                refreshExpiresAt = System.currentTimeMillis() + (7 * 24 * 3600 * 1000L)
            )

            logger.info("Token refreshed successfully for user $telegramId")
            return newTokens.accessToken
        } catch (e: Exception) {
            // Refresh failed, user needs to re-register
            logger.error("Token refresh failed for user $telegramId", e)
            sessionManager.deleteSession(telegramId)
            return null
        }
    }

    private suspend fun handleHelp(chatId: Long) {
        sendMessage(
            chatId,
            """
            🏭 Factory Store Bot

            📋 Commands:
            /start - Welcome message
            /register - Create account
            /products - Browse all products
            /categories - View categories
            /category <id> - Browse by category
            /order <id> <qty> - Place order
            /myorders - View your orders
            /help - Show this help

            💡 Getting Started:
            1. /register - Create your account
            2. /categories - Browse categories
            3. /category <id> - View products
            4. /order <product_id> <qty> - Place order
            5. /myorders - Track orders
            """.trimIndent()
        )
    }

    private suspend fun handleResendOTP(chatId: Long, telegramId: Long) {
        val data = stateManager.getData(telegramId)
        val email = data["email"]

        if (email == null) {
            sendMessage(chatId, "❌ Session expired. Please use /register to start again.")
            stateManager.clearState(telegramId)
            return
        }

        sendMessage(chatId, "Resending OTP... ⏳")

        try {
            authClient.resendOTP(email)

            sendMessage(
                chatId,
                """
                ✅ New OTP sent successfully!

                📧 Check your email: $email

                ⚠️ NOTE: Email is not configured yet. Check the auth-service logs for your OTP:
                Run: docker-compose logs auth-service | grep "OTP resent"

                Please enter your 6-digit OTP:
                """.trimIndent()
            )

            logger.info("OTP resent for user $telegramId")

        } catch (e: Exception) {
            logger.error("Failed to resend OTP for user $telegramId", e)
            sendMessage(
                chatId,
                """
                ❌ Failed to resend OTP: ${e.message}

                Please try again or use /register to start over.
                """.trimIndent()
            )
        }
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
