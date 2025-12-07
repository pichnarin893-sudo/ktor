package com.natjoub.auth.repositories

import com.natjoub.auth.models.entity.Credential
import com.natjoub.auth.models.entity.Credentials
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

/**
 * Repository interface for Credential operations
 */
interface CredentialRepository {
    suspend fun create(
        userId: UUID,
        email: String,
        username: String?,
        phoneNumber: String?,
        password: String
    ): Credential

    suspend fun findById(id: UUID): Credential?
    suspend fun findByUserId(userId: UUID): Credential?
    suspend fun findByEmail(email: String): Credential?
    suspend fun findByUsername(username: String): Credential?
    suspend fun findByPhoneNumber(phoneNumber: String): Credential?
    suspend fun findByIdentifier(identifier: String): Credential?
    suspend fun updatePassword(userId: UUID, newPassword: String): Boolean
    suspend fun updateOTP(userId: UUID, otp: String, otpExpiry: LocalDateTime): Boolean
    suspend fun clearOTP(userId: UUID): Boolean
    suspend fun updateVerificationStatus(userId: UUID, isVerified: Boolean): Boolean
    suspend fun updateUsername(userId: UUID, username: String): Boolean
    suspend fun updatePhoneNumber(userId: UUID, phoneNumber: String): Boolean
    suspend fun emailExists(email: String): Boolean
    suspend fun usernameExists(username: String): Boolean
    suspend fun phoneNumberExists(phoneNumber: String): Boolean
}

/**
 * Implementation of CredentialRepository using Exposed ORM
 */
class CredentialRepositoryImpl : CredentialRepository {

    private fun resultRowToCredential(row: ResultRow): Credential {
        return Credential(
            id = row[Credentials.id],
            userId = row[Credentials.userId],
            email = row[Credentials.email],
            username = row[Credentials.username],
            phoneNumber = row[Credentials.phoneNumber],
            password = row[Credentials.password],
            otp = row[Credentials.otp],
            otpExpiry = row[Credentials.otpExpiry],
            isVerified = row[Credentials.isVerified],
            createdAt = row[Credentials.createdAt],
            updatedAt = row[Credentials.updatedAt]
        )
    }

    override suspend fun create(
        userId: UUID,
        email: String,
        username: String?,
        phoneNumber: String?,
        password: String
    ): Credential = transaction {
        val credentialId = UUID.randomUUID()
        val now = LocalDateTime.now()

        Credentials.insert {
            it[id] = credentialId
            it[Credentials.userId] = userId
            it[Credentials.email] = email
            it[Credentials.username] = username
            it[Credentials.phoneNumber] = phoneNumber
            it[Credentials.password] = password
            it[otp] = null
            it[otpExpiry] = null
            it[isVerified] = false
            it[createdAt] = now
            it[updatedAt] = now
        }

        Credential(credentialId, userId, email, username, phoneNumber, password, null, null, false, now, now)
    }

    override suspend fun findById(id: UUID): Credential? = transaction {
        Credentials.select { Credentials.id eq id }
            .map { resultRowToCredential(it) }
            .singleOrNull()
    }

    override suspend fun findByUserId(userId: UUID): Credential? = transaction {
        Credentials.select { Credentials.userId eq userId }
            .map { resultRowToCredential(it) }
            .singleOrNull()
    }

    override suspend fun findByEmail(email: String): Credential? = transaction {
        Credentials.select { Credentials.email eq email }
            .map { resultRowToCredential(it) }
            .singleOrNull()
    }

    override suspend fun findByUsername(username: String): Credential? = transaction {
        Credentials.select { Credentials.username eq username }
            .map { resultRowToCredential(it) }
            .singleOrNull()
    }

    override suspend fun findByPhoneNumber(phoneNumber: String): Credential? = transaction {
        Credentials.select { Credentials.phoneNumber eq phoneNumber }
            .map { resultRowToCredential(it) }
            .singleOrNull()
    }

    override suspend fun findByIdentifier(identifier: String): Credential? = transaction {
        Credentials.select {
            (Credentials.email eq identifier) or
                    (Credentials.username eq identifier) or
                    (Credentials.phoneNumber eq identifier)
        }
            .map { resultRowToCredential(it) }
            .singleOrNull()
    }

    override suspend fun updatePassword(userId: UUID, newPassword: String): Boolean = transaction {
        Credentials.update({ Credentials.userId eq userId }) {
            it[password] = newPassword
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    override suspend fun updateOTP(userId: UUID, otp: String, otpExpiry: LocalDateTime): Boolean = transaction {
        Credentials.update({ Credentials.userId eq userId }) {
            it[Credentials.otp] = otp
            it[Credentials.otpExpiry] = otpExpiry
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    override suspend fun clearOTP(userId: UUID): Boolean = transaction {
        Credentials.update({ Credentials.userId eq userId }) {
            it[otp] = null
            it[otpExpiry] = null
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    override suspend fun updateVerificationStatus(userId: UUID, isVerified: Boolean): Boolean = transaction {
        Credentials.update({ Credentials.userId eq userId }) {
            it[Credentials.isVerified] = isVerified
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    override suspend fun updateUsername(userId: UUID, username: String): Boolean = transaction {
        Credentials.update({ Credentials.userId eq userId }) {
            it[Credentials.username] = username
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    override suspend fun updatePhoneNumber(userId: UUID, phoneNumber: String): Boolean = transaction {
        Credentials.update({ Credentials.userId eq userId }) {
            it[Credentials.phoneNumber] = phoneNumber
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    override suspend fun emailExists(email: String): Boolean = transaction {
        Credentials.select { Credentials.email eq email }.count() > 0
    }

    override suspend fun usernameExists(username: String): Boolean = transaction {
        Credentials.select { Credentials.username eq username }.count() > 0
    }

    override suspend fun phoneNumberExists(phoneNumber: String): Boolean = transaction {
        Credentials.select { Credentials.phoneNumber eq phoneNumber }.count() > 0
    }
}
