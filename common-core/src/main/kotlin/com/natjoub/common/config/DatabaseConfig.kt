package com.natjoub.common.config

/**
 * Database configuration data class
 * Shared by all services
 */
data class DatabaseConfig(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int
)
