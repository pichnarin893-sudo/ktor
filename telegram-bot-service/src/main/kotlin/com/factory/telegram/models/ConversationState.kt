package com.factory.telegram.models

data class ConversationState(
    val telegramId: Long,
    val step: RegistrationStep,
    val data: MutableMap<String, String> = mutableMapOf(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class RegistrationStep {
    IDLE,
    AWAITING_FIRST_NAME,
    AWAITING_LAST_NAME,
    AWAITING_EMAIL,
    AWAITING_PHONE,
    AWAITING_USERNAME,
    AWAITING_PASSWORD,
    AWAITING_OTP
}

data class UserSession(
    val telegramId: Long,
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresAt: Long,
    val refreshExpiresAt: Long
)
