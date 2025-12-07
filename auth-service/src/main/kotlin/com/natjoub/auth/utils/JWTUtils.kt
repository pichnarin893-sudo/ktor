package com.natjoub.auth.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Utility object for JWT token generation, verification, and extraction
 */
object JWTUtils {
    // Token expiry durations
    private const val ACCESS_TOKEN_EXPIRY_MINUTES = 15L
    private const val REFRESH_TOKEN_EXPIRY_DAYS = 7L

    /**
     * Generate an access token for a user
     * @param userId The user's UUID
     * @param role The user's role
     * @param secret The JWT secret key
     * @param issuer The JWT issuer
     * @param audience The JWT audience
     * @return The generated JWT access token
     */
    fun generateAccessToken(
        userId: UUID,
        role: String,
        secret: String,
        issuer: String,
        audience: String
    ): String {
        val expiresAt = Instant.now().plus(ACCESS_TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES)

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("user_id", userId.toString())
            .withClaim("role", role)
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(expiresAt))
            .sign(Algorithm.HMAC256(secret))
    }

    /**
     * Generate a refresh token for a user
     * @param userId The user's UUID
     * @param secret The JWT secret key
     * @param issuer The JWT issuer
     * @param audience The JWT audience
     * @return The generated JWT refresh token
     */
    fun generateRefreshToken(
        userId: UUID,
        secret: String,
        issuer: String,
        audience: String
    ): String {
        val expiresAt = Instant.now().plus(REFRESH_TOKEN_EXPIRY_DAYS, ChronoUnit.DAYS)

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("user_id", userId.toString())
            .withClaim("type", "refresh")
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(expiresAt))
            .sign(Algorithm.HMAC256(secret))
    }

    /**
     * Create a JWT verifier
     * @param secret The JWT secret key
     * @param issuer The JWT issuer
     * @param audience The JWT audience
     * @return JWTVerifier instance
     */
    fun createVerifier(secret: String, issuer: String, audience: String): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(secret))
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
    }

    /**
     * Verify and decode a JWT token
     * @param token The JWT token to verify
     * @param secret The JWT secret key
     * @param issuer The JWT issuer
     * @param audience The JWT audience
     * @return DecodedJWT if valid, null otherwise
     */
    fun verifyToken(token: String, secret: String, issuer: String, audience: String): DecodedJWT? {
        return try {
            val verifier = createVerifier(secret, issuer, audience)
            verifier.verify(token)
        } catch (e: JWTVerificationException) {
            null
        }
    }

    /**
     * Extract user ID from a JWT token
     * @param token The decoded JWT token
     * @return The user UUID, or null if claim doesn't exist
     */
    fun extractUserId(token: DecodedJWT): UUID? {
        return try {
            val userIdString = token.getClaim("user_id").asString()
            UUID.fromString(userIdString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract role from a JWT token
     * @param token The decoded JWT token
     * @return The user's role, or null if claim doesn't exist
     */
    fun extractRole(token: DecodedJWT): String? {
        return token.getClaim("role").asString()
    }

    /**
     * Extract token type from a JWT token
     * @param token The decoded JWT token
     * @return The token type (access/refresh), or null if claim doesn't exist
     */
    fun extractTokenType(token: DecodedJWT): String? {
        return token.getClaim("type").asString()
    }

    /**
     * Check if a token is expired
     * @param token The decoded JWT token
     * @return true if expired, false otherwise
     */
    fun isTokenExpired(token: DecodedJWT): Boolean {
        return token.expiresAt?.before(Date()) ?: true
    }

    /**
     * Get access token expiry duration in seconds
     * @return Expiry duration in seconds
     */
    fun getAccessTokenExpirySeconds(): Long {
        return ACCESS_TOKEN_EXPIRY_MINUTES * 60
    }

    /**
     * Get refresh token expiry duration in seconds
     * @return Expiry duration in seconds
     */
    fun getRefreshTokenExpirySeconds(): Long {
        return REFRESH_TOKEN_EXPIRY_DAYS * 24 * 60 * 60
    }

    /**
     * Calculate expiry datetime for refresh token storage
     * @return Expiry instant for refresh token
     */
    fun calculateRefreshTokenExpiry(): Instant {
        return Instant.now().plus(REFRESH_TOKEN_EXPIRY_DAYS, ChronoUnit.DAYS)
    }
}
