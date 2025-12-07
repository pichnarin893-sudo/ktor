package com.natjoub.core.config

import com.natjoub.auth.models.entity.*
import com.natjoub.inventory.models.entity.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Shared database factory for managing database connections
 * Used by all services (Auth, Inventory, etc.)
 */
object DatabaseFactory {
    private lateinit var dataSource: HikariDataSource

    fun init(config: DatabaseConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.url
            driverClassName = config.driver
            username = config.user
            password = config.password
            maximumPoolSize = config.maxPoolSize
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        // Create tables if they don't exist (useful for testing/development)
        transaction {
            // Auth schema tables (auth_schema)
            SchemaUtils.create(
                Roles,
                Users,
                Credentials,
                RefreshTokens,
                TokenBlacklist
            )

            // Inventory schema tables (inventory_schema)
            SchemaUtils.create(
                Branches,
                Categories,
                InventoryItems,
                StockLevels,
                StockMovements
            )
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }
}
