package com.factory.telegram.utils

object ValidationUtils {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PHONE_REGEX = Regex("^\\+?[1-9]\\d{1,14}$") // E.164 international format
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]{3,20}$")

    fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }

    fun isValidPhone(phone: String): Boolean {
        return PHONE_REGEX.matches(phone)
    }

    fun isValidUsername(username: String): Boolean {
        return USERNAME_REGEX.matches(username)
    }

    fun isValidPassword(password: String): Boolean {
        // Minimum 8 characters
        if (password.length < 8) return false

        // At least one uppercase, one lowercase, one digit
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }

        return hasUppercase && hasLowercase && hasDigit
    }

    fun getPasswordStrengthMessage(password: String): String {
        val issues = mutableListOf<String>()

        if (password.length < 8) {
            issues.add("at least 8 characters")
        }
        if (!password.any { it.isUpperCase() }) {
            issues.add("one uppercase letter")
        }
        if (!password.any { it.isLowerCase() }) {
            issues.add("one lowercase letter")
        }
        if (!password.any { it.isDigit() }) {
            issues.add("one number")
        }

        return if (issues.isEmpty()) {
            "Password is strong!"
        } else {
            "Password needs: ${issues.joinToString(", ")}"
        }
    }
}
