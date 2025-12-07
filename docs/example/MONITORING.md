# Monitoring & Observability Guide

This guide explains how to monitor your NatJoub Microservices (Auth Service and Inventory Service) using Prometheus and Grafana.

## 📊 Overview

The monitoring stack includes:
- **Prometheus**: Metrics collection and storage
- **Grafana**: Metrics visualization and dashboards
- **Micrometer**: Application metrics instrumentation

## 🚀 Quick Start

### 1. Start the Monitoring Stack

```bash
# Start all services including monitoring
docker-compose up -d

# Verify services are running
docker-compose ps
```

### 2. Access Monitoring Tools

- **Grafana Dashboard**: http://localhost:3000
  - Username: `admin`
  - Password: `admin`

- **Prometheus**: http://localhost:9090

- **Application Metrics**: http://localhost:8080/metrics

### 3. View Pre-configured Dashboard

The Grafana dashboard is automatically provisioned with the following panels:

1. **Auth Service - Request Rate**: HTTP requests per second
2. **Inventory Service - Request Rate**: HTTP requests per second
3. **Auth Service - Response Time (p95)**: 95th percentile response time
4. **Inventory Service - Response Time (p95)**: 95th percentile response time
5. **Auth Service - Custom Metrics**: Login attempts, registrations, token refreshes
6. **Inventory Service - Custom Metrics**: Items created, stock movements, low stock alerts
7. **JVM Memory Usage**: Heap and non-heap memory
8. **JVM Threads**: Thread count and state
9. **CPU Usage**: System and process CPU utilization
10. **Garbage Collection Time**: GC pause duration

## 📈 Available Metrics

### Auth Service Metrics

#### HTTP Metrics
- `http_server_requests_seconds_count{service="auth-service"}` - Request count
- `http_server_requests_seconds_sum{service="auth-service"}` - Total request duration
- `http_server_requests_seconds_bucket{service="auth-service"}` - Response time histogram

#### Custom Metrics
- `auth_login_attempts_total{success="true|false"}` - Login attempts by success status
- `auth_registration_total` - User registrations
- `auth_token_refresh_total` - Token refresh operations
- `auth_password_reset_total` - Password reset requests

### Inventory Service Metrics

#### HTTP Metrics
- `http_server_requests_seconds_count{service="inventory-service"}` - Request count
- `http_server_requests_seconds_sum{service="inventory-service"}` - Total request duration
- `http_server_requests_seconds_bucket{service="inventory-service"}` - Response time histogram

#### Custom Metrics
- `inventory_items_created_total` - Inventory items created
- `inventory_stock_movements_total{movement_type="IN|OUT|TRANSFER|ADJUSTMENT|RETURN"}` - Stock movements by type
- `inventory_branches_created_total` - Branches created
- `inventory_categories_created_total` - Categories created
- `inventory_low_stock_alerts_total{item_sku="..."}` - Low stock alerts by SKU

### JVM Metrics

#### Memory
- `jvm_memory_used_bytes{area="heap|nonheap", id="..."}` - Memory usage
- `jvm_memory_max_bytes{area="heap|nonheap", id="..."}` - Maximum memory
- `jvm_memory_committed_bytes{area="heap|nonheap", id="..."}` - Committed memory

#### Threads
- `jvm_threads_live_threads` - Current live thread count
- `jvm_threads_peak_threads` - Peak thread count
- `jvm_threads_daemon_threads` - Daemon thread count

#### Garbage Collection
- `jvm_gc_pause_seconds_count{action="...", cause="..."}` - GC pause count
- `jvm_gc_pause_seconds_sum{action="...", cause="..."}` - Total GC pause time
- `jvm_gc_memory_allocated_bytes_total` - Total allocated memory
- `jvm_gc_memory_promoted_bytes_total` - Total promoted memory

#### System
- `system_cpu_usage` - System CPU usage (0-1)
- `process_cpu_usage` - Process CPU usage (0-1)
- `process_uptime_seconds` - Application uptime

## 🔍 Common Queries

### Auth Service Queries

```promql
# Request rate (requests per second)
rate(http_server_requests_seconds_count{service="auth-service"}[5m])

# Average response time
rate(http_server_requests_seconds_sum{service="auth-service"}[5m]) /
rate(http_server_requests_seconds_count{service="auth-service"}[5m])

# 95th percentile response time
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{service="auth-service"}[5m]))

# Success rate (assuming 2xx status codes)
sum(rate(http_server_requests_seconds_count{service="auth-service",status=~"2.."}[5m])) /
sum(rate(http_server_requests_seconds_count{service="auth-service"}[5m]))

# Failed login attempts per minute
rate(auth_login_attempts_total{success="false"}[1m]) * 60
```

### Inventory Service Queries

```promql
# Request rate
rate(http_server_requests_seconds_count{service="inventory-service"}[5m])

# Stock movements by type
sum by (movement_type) (rate(inventory_stock_movements_total[5m]))

# Low stock alerts rate
rate(inventory_low_stock_alerts_total[5m])

# Top items with low stock alerts
topk(10, sum by (item_sku) (inventory_low_stock_alerts_total))
```

### JVM Queries

```promql
# Heap memory usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# GC pressure (time spent in GC per second)
rate(jvm_gc_pause_seconds_sum[5m])

# Thread count trend
jvm_threads_live_threads
```

## 🎯 Alerting (Future Implementation)

Consider setting up alerts for:

### Auth Service
- High failed login rate (potential attack)
- Slow response times (> 1s p95)
- High error rate (> 5%)

### Inventory Service
- Increasing low stock alerts
- Slow database queries
- High stock movement errors

### System
- High memory usage (> 80%)
- High CPU usage (> 80%)
- Long GC pauses (> 1s)

### Example Alert Rules (prometheus.yml)

```yaml
groups:
  - name: auth_service
    interval: 30s
    rules:
      - alert: HighFailedLoginRate
        expr: rate(auth_login_attempts_total{success="false"}[5m]) > 10
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High failed login rate detected"
          description: "Failed login attempts exceed 10 per second"

      - alert: SlowResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{service="auth-service"}[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Slow auth service response time"
          description: "p95 response time is above 1 second"

  - name: inventory_service
    interval: 30s
    rules:
      - alert: HighLowStockAlerts
        expr: rate(inventory_low_stock_alerts_total[5m]) > 5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High number of low stock alerts"
          description: "Low stock alerts exceed 5 per second"

  - name: system
    interval: 30s
    rules:
      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High JVM heap memory usage"
          description: "Heap memory usage is above 80%"

      - alert: HighCPUUsage
        expr: process_cpu_usage * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High CPU usage"
          description: "CPU usage is above 80%"
```

## 🛠️ Custom Metrics Integration

### Adding Metrics to Your Code

The `Metrics` object in `MonitoringPlugin.kt` provides helper methods:

#### Auth Service Example

```kotlin
// In AuthService.kt
import com.natjoub.core.plugins.Metrics

class AuthService {
    suspend fun login(credentials: LoginRequest): LoginResponse {
        val result = try {
            // Login logic
            performLogin(credentials)
        } catch (e: Exception) {
            Metrics.recordLoginAttempt(success = false)
            throw e
        }

        Metrics.recordLoginAttempt(success = true)
        return result
    }

    suspend fun register(request: RegisterRequest): User {
        val user = createUser(request)
        Metrics.recordRegistration()
        return user
    }
}
```

#### Inventory Service Example

```kotlin
// In InventoryService.kt
import com.natjoub.core.plugins.Metrics

class InventoryService {
    suspend fun createItem(item: CreateItemRequest): InventoryItem {
        val created = repository.create(item)
        Metrics.recordInventoryItemCreated()
        return created
    }

    suspend fun recordStockMovement(movement: StockMovementRequest): StockMovement {
        val recorded = repository.createMovement(movement)
        Metrics.recordStockMovement(movement.movementType.name)

        // Check for low stock
        val stockLevel = repository.getStockLevel(movement.itemId, movement.branchId)
        if (stockLevel.quantity <= stockLevel.reorderLevel) {
            Metrics.recordLowStockAlert(stockLevel.item.sku)
        }

        return recorded
    }
}
```

## 📊 Dashboard Customization

### Editing the Dashboard

1. Open Grafana at http://localhost:3000
2. Navigate to the "NatJoub Microservices - Auth & Inventory Services" dashboard
3. Click the settings icon (⚙️) in the top right
4. Make your changes
5. Save the dashboard

### Exporting Dashboard

To save your changes permanently:

```bash
# Export from Grafana UI and save to:
grafana/provisioning/dashboards/microservices-dashboard.json
```

### Creating New Panels

1. Click "Add Panel" in the dashboard
2. Select visualization type
3. Add Prometheus query
4. Configure display options
5. Save panel

## 🔧 Troubleshooting

### Metrics Not Showing

1. Verify application is exposing metrics:
   ```bash
   curl http://localhost:8080/metrics
   ```

2. Check Prometheus is scraping:
   - Open http://localhost:9090/targets
   - Verify `ktor-microservice` target is UP

3. Check Prometheus configuration:
   ```bash
   docker exec ktor-microservice_prometheus_1 cat /etc/prometheus/prometheus.yml
   ```

### Grafana Dashboard Empty

1. Verify Prometheus datasource is configured:
   - Go to Configuration → Data Sources
   - Check Prometheus URL is `http://prometheus:9090`

2. Test datasource connectivity:
   - Click "Save & Test" on datasource configuration

3. Check dashboard queries:
   - Edit panel → View query
   - Verify metric names match your application

### High Memory Usage

1. Check JVM memory settings in docker-compose.yml:
   ```yaml
   environment:
     - JAVA_OPTS=-Xms512m -Xmx1024m
   ```

2. Monitor memory metrics in Grafana
3. Consider increasing container memory limits

## 📚 Additional Resources

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Ktor Metrics Documentation](https://ktor.io/docs/micrometer-metrics.html)

## 🎓 Best Practices

1. **Set appropriate scrape intervals** - Balance between data granularity and storage (default: 15s)
2. **Use labels wisely** - Don't create high-cardinality labels (avoid user IDs, request IDs)
3. **Create meaningful dashboards** - Group related metrics together
4. **Set up alerts** - Proactive monitoring prevents incidents
5. **Regular review** - Review metrics to understand normal behavior
6. **Document custom metrics** - Keep this file updated with new metrics

## 🔐 Security Considerations

1. **Change default passwords** - Update Grafana admin password in production
2. **Restrict access** - Use firewalls or authentication for Prometheus/Grafana
3. **Sensitive data** - Don't include sensitive information in metric labels
4. **HTTPS** - Use TLS for Grafana in production
