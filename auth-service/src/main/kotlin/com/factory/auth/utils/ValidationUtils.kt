package com.natjoub.auth.utils

/**
 * Utility object for input validation
 */
object ValidationUtils {
    // Regex patterns
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PHONE_REGEX = Regex("^\\+?[1-9]\\d{1,14}$") // E.164 format
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]{3,20}$")

    /**
     * Validate email format
     * @param email The email to validate
     * @return true if email is valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        return email.matches(EMAIL_REGEX)
    }

    /**
     * Validate phone number format (E.164 international format)
     * @param phoneNumber The phone number to validate
     * @return true if phone number is valid, false otherwise
     */
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.matches(PHONE_REGEX)
    }

    /**
     * Validate username format
     * Username must be 3-20 characters and contain only letters, numbers, and underscores
     * @param username The username to validate
     * @return true if username is valid, false otherwise
     */
    fun isValidUsername(username: String): Boolean {
        return username.matches(USERNAME_REGEX)
    }

    /**
     * Validate password strength
     * Password must be at least 8 characters long
     * @param password The password to validate
     * @return true if password meets minimum requirements, false otherwise
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    /**
     * Validate name (first name or last name)
     * Name must be 1-100 characters and contain only letters, spaces, hyphens, and apostrophes
     * @param name The name to validate
     * @return true if name is valid, false otherwise
     */
    fun isValidName(name: String): Boolean {
        if (name.isBlank() || name.length > 100) return false
        return name.all { it.isLetter() || it == ' ' || it == '-' || it == '\'' }
    }

    /**
     * Validate OTP format
     * OTP must be exactly 6 digits
     * @param otp The OTP to validate
     * @return true if OTP format is valid, false otherwise
     */
    fun isValidOTP(otp: String): Boolean {
        return otp.length == 6 && otp.all { it.isDigit() }
    }

    /**
     * Validate date format (YYYY-MM-DD)
     * @param dateString The date string to validate
     * @return true if date format is valid, false otherwise
     */
    fun isValidDateFormat(dateString: String): Boolean {
        val dateRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        return dateString.matches(dateRegex)
    }

    /**
     * Validate UUID format
     * @param uuidString The UUID string to validate
     * @return true if UUID format is valid, false otherwise
     */
    fun isValidUUID(uuidString: String): Boolean {
        return try {
            java.util.UUID.fromString(uuidString)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
