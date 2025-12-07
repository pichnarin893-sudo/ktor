package com.natjoub.auth.exceptions

/**
 * Base exception class for authentication-related errors
 */
sealed class AuthException(
    message: String,
    val errorCode: String,
    val statusCode: Int
) : Exception(message)

/**
 * Exception thrown when user credentials are invalid
 */
class InvalidCredentialsException(
    message: String = "Invalid email/username/phone or password"
) : AuthException(message, "INVALID_CREDENTIALS", 401)

/**
 * Exception thrown when a user is not found
 */
class UserNotFoundException(
    message: String = "User not found"
) : AuthException(message, "USER_NOT_FOUND", 404)

/**
 * Exception thrown when attempting to register a user that already exists
 */
class UserAlreadyExistsException(
    message: String = "User with this email, username, or phone number already exists"
) : AuthException(message, "USER_ALREADY_EXISTS", 409)

/**
 * Exception thrown when a JWT token is invalid
 */
class InvalidTokenException(
    message: String = "Invalid or malformed token"
) : AuthException(message, "INVALID_TOKEN", 401)

/**
 * Exception thrown when a JWT token has expired
 */
class ExpiredTokenException(
    message: String = "Token has expired"
) : AuthException(message, "EXPIRED_TOKEN", 401)

/**
 * Exception thrown when a user lacks sufficient permissions
 */
class InsufficientPermissionsException(
    message: String = "You do not have permission to perform this action"
) : AuthException(message, "INSUFFICIENT_PERMISSIONS", 403)

/**
 * Exception thrown when an OTP is invalid or expired
 */
class InvalidOTPException(
    message: String = "Invalid or expired OTP"
) : AuthException(message, "INVALID_OTP", 400)

/**
 * Exception thrown when account is not verified
 */
class AccountNotVerifiedException(
    message: String = "Account is not verified. Please verify your account first."
) : AuthException(message, "ACCOUNT_NOT_VERIFIED", 403)

/**
 * Exception thrown when account is inactive/deactivated
 */
class AccountDeactivatedException(
    message: String = "Account has been deactivated"
) : AuthException(message, "ACCOUNT_DEACTIVATED", 403)

/**
 * Exception thrown when input validation fails
 */
class ValidationException(
    message: String,
    val details: Map<String, String>? = null
) : AuthException(message, "VALIDATION_ERROR", 400)

/**
 * Exception thrown when a refresh token is invalid or revoked
 */
class InvalidRefreshTokenException(
    message: String = "Invalid or revoked refresh token"
) : AuthException(message, "INVALID_REFRESH_TOKEN", 401)

/**
 * Exception thrown when role is invalid
 */
class InvalidRoleException(
    message: String = "Invalid role specified"
) : AuthException(message, "INVALID_ROLE", 400)

/**
 * Exception thrown when token is blacklisted
 */
class TokenBlacklistedException(
    message: String = "Token has been revoked"
) : AuthException(message, "TOKEN_BLACKLISTED", 401)

/**
 * Exception thrown when rate limit is exceeded
 */
class RateLimitExceededException(
    message: String = "Too many requests. Please try again later."
) : AuthException(message, "RATE_LIMIT_EXCEEDED", 429)
