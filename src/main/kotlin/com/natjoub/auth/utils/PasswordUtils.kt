package com.natjoub.auth.utils

import org.mindrot.jbcrypt.BCrypt

/**
 * Utility object for password hashing and verification using BCrypt
 */
object PasswordUtils {
    private const val BCRYPT_ROUNDS = 12

    /**
     * Hash a plain text password using BCrypt with 12 rounds
     * @param plainPassword The plain text password to hash
     * @return The hashed password
     */
    fun hashPassword(plainPassword: String): String {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS))
    }

    /**
     * Verify a plain text password against a hashed password
     * @param plainPassword The plain text password
     * @param hashedPassword The hashed password to verify against
     * @return true if the password matches, false otherwise
     */
    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return try {
            BCrypt.checkpw(plainPassword, hashedPassword)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validate password strength
     * Password must be at least 8 characters long and contain:
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     *
     * @param password The password to validate
     * @return true if password meets requirements, false otherwise
     */
    fun isStrongPassword(password: String): Boolean {
        if (password.length < 8) return false

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar
    }
}
