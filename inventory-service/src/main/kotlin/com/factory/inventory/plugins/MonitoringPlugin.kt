package com.factory.inventory.plugins

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

/**
 * Configures monitoring and metrics for the microservices
 * Provides Prometheus metrics endpoint and custom metrics for Auth and Inventory services
 */
fun Application.configureMonitoring() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry

        // JVM Metrics
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            JvmThreadMetrics(),
            ClassLoaderMetrics(),
            ProcessorMetrics(),
            UptimeMetrics()
        )

        // Custom metric tags for proper service identification
        timers { call, exception ->
            tag("service", "inventory-service")
            tag("uri", call.request.local.uri)
            tag("method", call.request.local.method.value)
            tag("status", exception?.let { "error" } ?: "success")
        }
    }

    // Custom counters for services
    val authLoginCounter = Counter.builder("auth_login_attempts_total")
        .description("Total number of login attempts")
        .tag("service", "auth-service")
        .register(appMicrometerRegistry)

    val authRegistrationCounter = Counter.builder("auth_registration_total")
        .description("Total number of user registrations")
        .tag("service", "auth-service")
        .register(appMicrometerRegistry)

    val inventoryItemsCreated = Counter.builder("inventory_items_created_total")
        .description("Total number of inventory items created")
        .tag("service", "inventory-service")
        .register(appMicrometerRegistry)

    val inventoryStockMovements = Counter.builder("inventory_stock_movements_total")
        .description("Total number of stock movements")
        .tag("service", "inventory-service")
        .register(appMicrometerRegistry)

    val inventoryBranchesCreated = Counter.builder("inventory_branches_created_total")
        .description("Total number of branches created")
        .tag("service", "inventory-service")
        .register(appMicrometerRegistry)

    // Expose metrics endpoint
    routing {
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }

    environment.log.info("Monitoring configured - Prometheus metrics available at /metrics")
}

/**
 * Metrics helper object for accessing counters throughout the application
 */
object Metrics {
    private var registry: PrometheusMeterRegistry? = null

    fun initialize(reg: PrometheusMeterRegistry) {
        registry = reg
    }

    // Auth Service Metrics
    fun recordLoginAttempt(success: Boolean) {
        registry?.let {
            Counter.builder("auth_login_attempts_total")
                .tag("service", "auth-service")
                .tag("success", success.toString())
                .register(it)
                .increment()
        }
    }

    fun recordRegistration() {
        registry?.let {
            Counter.builder("auth_registration_total")
                .tag("service", "auth-service")
                .register(it)
                .increment()
        }
    }

    fun recordTokenRefresh() {
        registry?.let {
            Counter.builder("auth_token_refresh_total")
                .tag("service", "auth-service")
                .register(it)
                .increment()
        }
    }

    fun recordPasswordReset() {
        registry?.let {
            Counter.builder("auth_password_reset_total")
                .tag("service", "auth-service")
                .register(it)
                .increment()
        }
    }

    // Inventory Service Metrics
    fun recordInventoryItemCreated() {
        registry?.let {
            Counter.builder("inventory_items_created_total")
                .tag("service", "inventory-service")
                .register(it)
                .increment()
        }
    }

    fun recordStockMovement(movementType: String) {
        registry?.let {
            Counter.builder("inventory_stock_movements_total")
                .tag("service", "inventory-service")
                .tag("movement_type", movementType)
                .register(it)
                .increment()
        }
    }

    fun recordBranchCreated() {
        registry?.let {
            Counter.builder("inventory_branches_created_total")
                .tag("service", "inventory-service")
                .register(it)
                .increment()
        }
    }

    fun recordCategoryCreated() {
        registry?.let {
            Counter.builder("inventory_categories_created_total")
                .tag("service", "inventory-service")
                .register(it)
                .increment()
        }
    }

    fun recordLowStockAlert(itemSku: String) {
        registry?.let {
            Counter.builder("inventory_low_stock_alerts_total")
                .tag("service", "inventory-service")
                .tag("item_sku", itemSku)
                .register(it)
                .increment()
        }
    }
}
