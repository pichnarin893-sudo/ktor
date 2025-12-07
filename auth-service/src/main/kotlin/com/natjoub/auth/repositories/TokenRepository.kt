package com.natjoub.auth.repositories

import com.natjoub.auth.models.entity.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

/**
 * Repository interface for Token operations (refresh tokens and blacklist)
 */
interface TokenRepository {
    // Refresh token operations
    suspend fun createRefreshToken(userId: UUID, token: String, expiresAt: LocalDateTime): RefreshToken
    suspend fun findRefreshTokenByToken(token: String): RefreshToken?
    suspend fun revokeRefreshToken(token: String): Boolean
    suspend fun revokeAllUserRefreshTokens(userId: UUID): Boolean
    suspend fun deleteExpiredRefreshTokens(): Int

    // Token blacklist operations
    suspend fun addToBlacklist(token: String, expiresAt: LocalDateTime): BlacklistedToken
    suspend fun isTokenBlacklisted(token: String): Boolean
    suspend fun deleteExpiredBlacklistedTokens(): Int
}

/**
 * Implementation of TokenRepository using Exposed ORM
 */
class TokenRepositoryImpl : TokenRepository {

    private fun resultRowToRefreshToken(row: ResultRow): RefreshToken {
        return RefreshToken(
            id = row[RefreshTokens.id],
            userId = row[RefreshTokens.userId],
            token = row[RefreshTokens.token],
            expiresAt = row[RefreshTokens.expiresAt],
            isRevoked = row[RefreshTokens.isRevoked],
            createdAt = row[RefreshTokens.createdAt]
        )
    }

    private fun resultRowToBlacklistedToken(row: ResultRow): BlacklistedToken {
        return BlacklistedToken(
            id = row[TokenBlacklist.id],
            token = row[TokenBlacklist.token],
            expiresAt = row[TokenBlacklist.expiresAt],
            createdAt = row[TokenBlacklist.createdAt]
        )
    }

    override suspend fun createRefreshToken(
        userId: UUID,
        token: String,
        expiresAt: LocalDateTime
    ): RefreshToken = transaction {
        val tokenId = UUID.randomUUID()
        val now = LocalDateTime.now()

        RefreshTokens.insert {
            it[id] = tokenId
            it[RefreshTokens.userId] = userId
            it[RefreshTokens.token] = token
            it[RefreshTokens.expiresAt] = expiresAt
            it[isRevoked] = false
            it[createdAt] = now
        }

        RefreshToken(tokenId, userId, token, expiresAt, false, now)
    }

    override suspend fun findRefreshTokenByToken(token: String): RefreshToken? = transaction {
        RefreshTokens.select { RefreshTokens.token eq token }
            .map { resultRowToRefreshToken(it) }
            .singleOrNull()
    }

    override suspend fun revokeRefreshToken(token: String): Boolean = transaction {
        RefreshTokens.update({ RefreshTokens.token eq token }) {
            it[isRevoked] = true
        } > 0
    }

    override suspend fun revokeAllUserRefreshTokens(userId: UUID): Boolean = transaction {
        RefreshTokens.update({ RefreshTokens.userId eq userId }) {
            it[isRevoked] = true
        } > 0
    }

    override suspend fun deleteExpiredRefreshTokens(): Int = transaction {
        RefreshTokens.deleteWhere {
            expiresAt less LocalDateTime.now()
        }
    }

    override suspend fun addToBlacklist(token: String, expiresAt: LocalDateTime): BlacklistedToken = transaction {
        val blacklistId = UUID.randomUUID()
        val now = LocalDateTime.now()

        TokenBlacklist.insert {
            it[id] = blacklistId
            it[TokenBlacklist.token] = token
            it[TokenBlacklist.expiresAt] = expiresAt
            it[createdAt] = now
        }

        BlacklistedToken(blacklistId, token, expiresAt, now)
    }

    override suspend fun isTokenBlacklisted(token: String): Boolean = transaction {
        TokenBlacklist.select {
            (TokenBlacklist.token eq token) and (TokenBlacklist.expiresAt greater LocalDateTime.now())
        }.count() > 0
    }

    override suspend fun deleteExpiredBlacklistedTokens(): Int = transaction {
        TokenBlacklist.deleteWhere {
            expiresAt less LocalDateTime.now()
        }
    }
}
