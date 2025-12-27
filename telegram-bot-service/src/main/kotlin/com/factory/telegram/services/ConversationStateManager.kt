package com.factory.telegram.services

import com.factory.telegram.models.ConversationState
import com.factory.telegram.models.RegistrationStep
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class ConversationStateManager {
    private val logger = LoggerFactory.getLogger(ConversationStateManager::class.java)
    private val states = ConcurrentHashMap<Long, ConversationState>()

    fun setState(telegramId: Long, step: RegistrationStep) {
        val existing = states[telegramId]
        val newState = if (existing != null) {
            existing.copy(step = step, timestamp = System.currentTimeMillis())
        } else {
            ConversationState(telegramId, step)
        }
        states[telegramId] = newState
        logger.debug("Set state for user $telegramId to $step")
    }

    fun getState(telegramId: Long): ConversationState? {
        val state = states[telegramId]

        // Clear stale states (older than 30 minutes)
        if (state != null && System.currentTimeMillis() - state.timestamp > 30 * 60 * 1000) {
            logger.info("Clearing stale conversation state for user $telegramId")
            states.remove(telegramId)
            return null
        }

        return state
    }

    fun saveData(telegramId: Long, key: String, value: String) {
        states[telegramId]?.data?.put(key, value)
        logger.debug("Saved data for user $telegramId: $key")
    }

    fun getData(telegramId: Long): Map<String, String> {
        return states[telegramId]?.data ?: emptyMap()
    }

    fun clearState(telegramId: Long) {
        states.remove(telegramId)
        logger.debug("Cleared state for user $telegramId")
    }
}
