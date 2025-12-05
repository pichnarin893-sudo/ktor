package com.natjoub.auth.utils

import java.time.LocalDateTime
import kotlin.random.Random

/**
 * Utility object for OTP (One-Time Password) generation and validation
 */
object OTPUtils {
    private const val OTP_LENGTH = 6
    private const val OTP_EXPIRY_MINUTES = 10L

    /**
     * Generate a 6-digit numeric OTP
     * @return A 6-digit OTP as a string
     */
    fun generateOTP(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    /**
     * Calculate OTP expiry time
     * @return LocalDateTime representing when the OTP will expire (10 minutes from now)
     */
    fun calculateExpiry(): LocalDateTime {
        return LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
    }

    /**
     * Check if an OTP is valid (not expired)
     * @param otpExpiry The expiry datetime of the OTP
     * @return true if the OTP has not expired, false otherwise
     */
    fun isOTPValid(otpExpiry: LocalDateTime?): Boolean {
        if (otpExpiry == null) return false
        return LocalDateTime.now().isBefore(otpExpiry)
    }

    /**
     * Verify if the provided OTP matches the stored OTP and is not expired
     * @param providedOTP The OTP provided by the user
     * @param storedOTP The OTP stored in the database
     * @param otpExpiry The expiry datetime of the stored OTP
     * @return true if OTP matches and is not expired, false otherwise
     */
    fun verifyOTP(providedOTP: String, storedOTP: String?, otpExpiry: LocalDateTime?): Boolean {
        if (storedOTP == null || otpExpiry == null) return false
        if (!isOTPValid(otpExpiry)) return false
        return providedOTP == storedOTP
    }
}
