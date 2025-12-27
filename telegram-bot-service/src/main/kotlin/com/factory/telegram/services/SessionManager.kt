package com.factory.telegram.services

import com.factory.telegram.models.UserSession
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory session manager for Telegram bot users.
 * Stores access and refresh tokens in memory (lost on restart).
 * For production, consider using Redis for distributed caching.
 */
class SessionManager {
    private val logger = LoggerFactory.getLogger(SessionManager::class.java)
    private val sessions = ConcurrentHashMap<Long, UserSession>()

    /**
     * Save or update a user session
     */
    fun saveSession(
        telegramId: Long,
        userId: String,
        accessToken: String,
        refreshToken: String,
        accessExpiresAt: Long,
        refreshExpiresAt: Long
    ) {
        val session = UserSession(
            telegramId = telegramId,
            userId = userId,
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessExpiresAt = accessExpiresAt,
            refreshExpiresAt = refreshExpiresAt
        )
        sessions[telegramId] = session
        logger.info("Session saved for telegram user $telegramId")
    }

    /**
     * Get a user session by telegram ID
     */
    fun getSession(telegramId: Long): UserSession? {
        val session = sessions[telegramId]

        // Check if refresh token is expired
        if (session != null && System.currentTimeMillis() > session.refreshExpiresAt) {
            logger.info("Session expired for telegram user $telegramId, removing...")
            sessions.remove(telegramId)
            return null
        }

        return session
    }

    /**
     * Delete a user session
     */
    fun deleteSession(telegramId: Long) {
        sessions.remove(telegramId)
        logger.info("Session deleted for telegram user $telegramId")
    }

    /**
     * Check if access token is still valid (with buffer)
     */
    fun isAccessTokenValid(telegramId: Long, bufferMinutes: Long = 5): Boolean {
        val session = sessions[telegramId] ?: return false
        val bufferMillis = bufferMinutes * 60 * 1000
        return System.currentTimeMillis() < (session.accessExpiresAt - bufferMillis)
    }

    /**
     * Get total number of active sessions
     */
    fun getActiveSessionCount(): Int = sessions.size

    /**
     * Clear all sessions (for testing or maintenance)
     */
    fun clearAllSessions() {
        val count = sessions.size
        sessions.clear()
        logger.info("Cleared all sessions (count: $count)")
    }

    /**
     * Clean up expired sessions (periodic cleanup)
     */
    fun cleanupExpiredSessions(): Int {
        val now = System.currentTimeMillis()
        val expired = sessions.filter { (_, session) ->
            now > session.refreshExpiresAt
        }

        expired.keys.forEach { telegramId ->
            sessions.remove(telegramId)
            logger.debug("Removed expired session for telegram user $telegramId")
        }

        if (expired.isNotEmpty()) {
            logger.info("Cleaned up ${expired.size} expired sessions")
        }

        return expired.size
    }
}
