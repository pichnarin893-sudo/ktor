# Telegram Bot Service

A lightweight Telegram bot for the Factory Store that enables customers to register, browse products, and place orders directly through Telegram.

## Features

### Customer Registration
- **Conversational Registration Flow**: Step-by-step guided registration
- **Custom User Data**: Users provide their own email, phone, username, and password
- **Input Validation**: Real-time validation for email, phone, username, and password strength
- **Secure Password Storage**: Passwords are hashed with BCrypt in the auth service
- **Telegram Integration**: Links Telegram account to Factory Store user account

### Shopping Features
- Browse all products
- View product categories
- Filter products by category
- Place orders
- View order history

## Registration Flow

```
User: /register

Bot: "Let's get you registered! 📝
      Please enter your first name:"

User: John

Bot: "✅ First name saved!
      Now enter your last name:"

User: Doe

Bot: "✅ Last name saved!
      Enter your email address:"

User: john.doe@example.com

Bot: "✅ Email saved!
      Enter your phone number (international format, e.g., +1234567890):"

User: +1234567890

Bot: "✅ Phone saved!
      Create a username (3-20 characters, letters, numbers, underscore only):"

User: johndoe123

Bot: "✅ Username saved!
      Create a strong password:
      • At least 8 characters
      • Include uppercase and lowercase letters
      • Include at least one number

      Enter your password:"

User: MySecurePass123

Bot: "✅ Registration successful! 🎉
      Welcome John!
      Your account has been created."
```

## Available Commands

| Command | Description |
|---------|-------------|
| `/start` | Welcome message and registration prompt |
| `/register` | Start the registration process |
| `/products` | Browse all available products |
| `/categories` | View product categories |
| `/category <id>` | Browse products in a specific category |
| `/order <product_id> <quantity>` | Place an order |
| `/myorders` | View your order history |
| `/help` | Show help message |

## Architecture

### In-Memory Session Management

**Session Storage**: Tokens are stored **in-memory** using `ConcurrentHashMap`.

**How it works:**
1. When user registers → Access and refresh tokens stored in memory
2. When user places order → Bot retrieves valid token from memory
3. When access token expires → Bot auto-refreshes using refresh token
4. When bot restarts → All sessions lost (users need to /register again)

**Benefits:**
- ✅ No database dependency for telegram bot
- ✅ Simple architecture
- ✅ Fast token access
- ✅ No duplication with auth service
- ✅ Auth service remains single source of truth

**Trade-offs:**
- ⚠️ Sessions lost on bot restart
- ⚠️ Not suitable for distributed deployments (use Redis for production)

### Token Management

**Auto-Refresh**: When an access token expires, the bot automatically refreshes it:
1. Check if access token is still valid (with 5-minute buffer)
2. If expired, use refresh token to get new tokens from auth service
3. Update in-memory session with new tokens
4. If refresh fails, user needs to /register again

**Security**:
- Access tokens expire based on auth service configuration (typically 15-60 minutes)
- Refresh tokens expire after 7 days
- Tokens stored in memory only (not persisted)

### Conversation State Management

**In-Memory State**: User registration progress tracked in memory using `ConversationStateManager`

**State Cleanup**:
- States automatically cleared on completion or error
- Stale states (older than 30 minutes) automatically removed

**Registration Steps**:
1. `AWAITING_FIRST_NAME`
2. `AWAITING_LAST_NAME`
3. `AWAITING_EMAIL`
4. `AWAITING_PHONE`
5. `AWAITING_USERNAME`
6. `AWAITING_PASSWORD`
7. `AWAITING_OTP` (future: for OTP verification)

### Input Validation

**Email**: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`

**Phone**: E.164 international format `^\\+?[1-9]\\d{1,14}$`

**Username**: `^[a-zA-Z0-9_]{3,20}$` (3-20 chars, alphanumeric + underscore)

**Password**:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit

### Auth Service Integration

**User Data Stored in Auth Service:**
- Telegram bot registers users by calling `POST /api/v1/auth/register`
- Auth service stores `telegram_id` in `credentials` table (links Telegram to user account)
- Auth service manages all refresh tokens in `refresh_tokens` table
- Single source of truth for user authentication

**No Database Duplication:**
- Telegram bot does NOT have its own database
- Auth service owns all user and token data
- Telegram bot only caches tokens in memory for performance

## Environment Variables

```bash
# Required
TELEGRAM_BOT_TOKEN=your_bot_token_here

# Service URLs (defaults shown)
AUTH_SERVICE_URL=http://localhost:8081
INVENTORY_SERVICE_URL=http://localhost:8082
ORDER_SERVICE_URL=http://localhost:8083
```

**Note:** No database configuration needed for telegram bot!

## Running the Service

### Local Development

```bash
# Set environment variables
export TELEGRAM_BOT_TOKEN="your_bot_token"

# Build and run
./gradlew :telegram-bot-service:build
./gradlew :telegram-bot-service:run
```

### Docker

```bash
docker-compose up telegram-bot-service
```

### Build Fat JAR

```bash
./gradlew :telegram-bot-service:buildFatJar
java -jar telegram-bot-service/build/libs/telegram-bot-service-all.jar
```

## Future Enhancements

### OTP Verification (Planned)

Once email/SMS sending is implemented in the auth service:

1. After password entry, auth service generates and sends OTP
2. Bot prompts: "We've sent a verification code to your email"
3. User enters OTP code
4. Bot calls `authClient.verifyOTP(email, otp)`
5. On success, user account is fully verified

Implementation placeholder already exists in `RegistrationStep.AWAITING_OTP`.

### Production-Ready Session Storage

For production deployments, replace in-memory storage with **Redis**:

```kotlin
class RedisSessionManager(private val redis: RedisClient) {
    fun saveSession(...) {
        redis.setex("telegram:session:$telegramId", 7.days, session.toJson())
    }
}
```

Benefits:
- Sessions persist across restarts
- Supports distributed bot instances
- Built-in TTL for automatic cleanup

### Additional Features (Ideas)

- Order cancellation
- Product search by name
- Product favorites/wishlist
- Delivery address management
- Order status notifications (via Telegram notifications)
- Admin commands for inventory management

## Project Structure

```
telegram-bot-service/
├── src/main/kotlin/com/factory/telegram/
│   ├── Application.kt                    # Entry point
│   ├── bot/
│   │   └── SimpleTelegramBot.kt         # Main bot logic
│   ├── client/
│   │   └── ServiceClients.kt            # HTTP clients for services
│   ├── models/
│   │   └── ConversationState.kt         # State management models
│   ├── services/
│   │   ├── ConversationStateManager.kt  # Registration state logic
│   │   └── SessionManager.kt            # In-memory session storage
│   └── utils/
│       └── ValidationUtils.kt           # Input validation
└── build.gradle.kts                     # Dependencies
```

## Dependencies

- **Kotlin**: 1.9.22
- **Ktor Client**: 2.3.7 (HTTP communication)
- **Kotlinx Serialization**: 1.6.2 (JSON)
- **SLF4J + Logback**: Logging
- **Kotlinx Coroutines**: 1.7.3

**No database dependencies!** Keeps the service lightweight.

## Notes

- The bot uses **long polling** (polls Telegram API every 1 second)
- All sessions stored **in-memory** (ephemeral)
- Conversation state stored **in-memory** (ephemeral)
- The bot is **stateless** at the application level
- On restart, users simply /register again (tokens re-issued by auth service)
- Auth service stores `telegram_id` in credentials table for telegram integration

## Advantages of In-Memory Approach

1. **No Database Overhead**: Fast, simple, no connection pool management
2. **Single Source of Truth**: Auth service owns all user data
3. **No Duplication**: Refresh tokens only stored once (in auth service)
4. **Microservices Best Practice**: Each service owns its domain
5. **Lightweight**: Minimal dependencies and resource usage

## When Users Need to Re-Register

- Bot restarts (sessions cleared)
- Refresh token expires (after 7 days)
- User clears their telegram data
- Session evicted from memory due to inactivity

Re-registration is seamless - just run `/register` and the auth service updates the existing account's `telegram_id`.
