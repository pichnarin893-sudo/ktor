package com.factory.order.plugins

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

/**
 * Configures monitoring and metrics for the Order Service
 * Provides Prometheus metrics endpoint with proper service tagging
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
            tag("service", "order-service")
            tag("uri", call.request.local.uri)
            tag("method", call.request.local.method.value)
            tag("status", exception?.let { "error" } ?: "success")
        }
    }

    // Expose metrics endpoint
    routing {
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }

    environment.log.info("Monitoring configured - Prometheus metrics available at /metrics")
}

/**
 * Metrics helper object for recording order-specific business metrics
 */
object OrderMetrics {
    private var registry: PrometheusMeterRegistry? = null

    fun initialize(reg: PrometheusMeterRegistry) {
        registry = reg
    }

    fun recordOrderCreated() {
        registry?.let {
            Counter.builder("order_created_total")
                .description("Total number of orders created")
                .tag("service", "order-service")
                .register(it)
                .increment()
        }
    }

    fun recordOrderUpdated() {
        registry?.let {
            Counter.builder("order_updated_total")
                .description("Total number of orders updated")
                .tag("service", "order-service")
                .register(it)
                .increment()
        }
    }

    fun recordOrderCancelled() {
        registry?.let {
            Counter.builder("order_cancelled_total")
                .description("Total number of orders cancelled")
                .tag("service", "order-service")
                .register(it)
                .increment()
        }
    }

    fun recordOrderStatusChange(status: String) {
        registry?.let {
            Counter.builder("order_status_change_total")
                .description("Total number of order status changes")
                .tag("service", "order-service")
                .tag("status", status)
                .register(it)
                .increment()
        }
    }
}
