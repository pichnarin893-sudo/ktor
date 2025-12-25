package com.factory.auth.models.entity

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * Roles table definition
 * Stores user roles: admin, manager, staff
 * No schema prefix - auth-service owns the entire auth_db database
 */
object Roles : Table("roles") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 50).uniqueIndex()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

/**
 * Users table definition
 * Stores core user information
 */
object Users : Table("users") {
    val id = uuid("id").autoGenerate()
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val dob = date("dob").nullable()
    val gender = varchar("gender", 20).nullable()
    val roleId = uuid("role_id").references(Roles.id)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

/**
 * Credentials table definition
 * Stores authentication credentials and verification data
 */
object Credentials : Table("credentials") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").uniqueIndex().references(Users.id)
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 100).uniqueIndex().nullable()
    val phoneNumber = varchar("phone_number", 20).uniqueIndex().nullable()
    val password = varchar("password", 255)
    val otp = varchar("otp", 6).nullable()
    val otpExpiry = datetime("otp_expiry").nullable()
    val isVerified = bool("is_verified").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

/**
 * Refresh tokens table definition
 * Stores refresh tokens for token rotation and revocation
 */
object RefreshTokens : Table("refresh_tokens") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(Users.id)
    val token = text("token").uniqueIndex()
    val expiresAt = datetime("expires_at")
    val isRevoked = bool("is_revoked").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

/**
 * Token blacklist table definition
 * Stores invalidated access tokens for logout functionality
 */
object TokenBlacklist : Table("token_blacklist") {
    val id = uuid("id").autoGenerate()
    val token = text("token").uniqueIndex()
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}
