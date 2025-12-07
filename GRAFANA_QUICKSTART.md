# Grafana Monitoring - Quick Start Guide

## ✅ What Has Been Set Up

Your NatJoub Microservices now have complete monitoring configured with:

1. **Prometheus** - Metrics collection (http://localhost:9090)
2. **Grafana** - Visualization dashboards (http://localhost:3000)
3. **Custom Metrics** - Auth and Inventory service metrics
4. **Auto-provisioning** - Dashboard and datasource automatically configured

## 🚀 Access Grafana

1. Open your browser and go to: **http://localhost:3000**

2. Login with:
   - **Username**: `admin`
   - **Password**: `admin`

3. The dashboard "NatJoub Microservices - Auth & Inventory Services" is already loaded!

## 📊 Dashboard Overview

The pre-configured dashboard includes 10 panels:

### Service-Specific Metrics

**Auth Service (Left Column):**
- Request Rate: HTTP requests per second
- Response Time (p95): 95th percentile response time
- Custom Metrics:
  - Login attempts (success/failure)
  - User registrations
  - Token refreshes

**Inventory Service (Right Column):**
- Request Rate: HTTP requests per second
- Response Time (p95): 95th percentile response time
- Custom Metrics:
  - Items created
  - Stock movements by type (IN, OUT, TRANSFER, etc.)
  - Branches created
  - Low stock alerts

### System Metrics (Bottom)

- **JVM Memory Usage**: Heap and non-heap memory consumption
- **JVM Threads**: Thread count and states
- **CPU Usage**: System and process CPU utilization
- **Garbage Collection**: GC pause duration

## 🧪 Generate Some Metrics

To see the dashboard in action, make some API calls:

```bash
# 1. Login (generates auth metrics)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier": "admin@example.com", "password": "admin123"}'

# Save the token from the response
TOKEN="<your-token-here>"

# 2. Get branches (generates inventory metrics)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/inventory/branches

# 3. Get inventory items
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/inventory/items

# 4. Check low stock
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/inventory/stock-levels/low-stock
```

After making these calls, go back to Grafana and you'll see the metrics appear in real-time!

## 📈 View Raw Metrics

You can also view raw Prometheus metrics:

- **Application Metrics**: http://localhost:8080/metrics
- **Prometheus UI**: http://localhost:9090

### Example Prometheus Queries

Try these in the Prometheus UI (http://localhost:9090/graph):

```promql
# Auth service request rate
rate(http_server_requests_seconds_count{service="auth-service"}[5m])

# Inventory service request rate
rate(http_server_requests_seconds_count{service="inventory-service"}[5m])

# Failed login attempts
rate(auth_login_attempts_total{success="false"}[5m])

# Stock movements by type
sum by (movement_type) (rate(inventory_stock_movements_total[5m]))

# JVM memory usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100
```

## 🎨 Customize Your Dashboard

### Edit Panels

1. Click on a panel title → Edit
2. Modify the Prometheus query
3. Change visualization type
4. Adjust display options
5. Click "Apply"

### Add New Panels

1. Click "Add panel" button in dashboard
2. Select visualization type (Graph, Gauge, Stat, etc.)
3. Write your Prometheus query
4. Configure display settings
5. Save the panel

### Export Dashboard

To save your customizations:

1. Click the settings icon (⚙️) at top
2. Select "JSON Model"
3. Copy the JSON
4. Paste into `grafana/provisioning/dashboards/microservices-dashboard.json`
5. Restart Grafana: `docker-compose restart grafana`

## 📚 Available Custom Metrics

### Auth Service

| Metric | Description | Labels |
|--------|-------------|--------|
| `auth_login_attempts_total` | Login attempts | `success=true/false` |
| `auth_registration_total` | User registrations | - |
| `auth_token_refresh_total` | Token refreshes | - |
| `auth_password_reset_total` | Password resets | - |

### Inventory Service

| Metric | Description | Labels |
|--------|-------------|--------|
| `inventory_items_created_total` | Items created | - |
| `inventory_stock_movements_total` | Stock movements | `movement_type=IN/OUT/TRANSFER/ADJUSTMENT/RETURN` |
| `inventory_branches_created_total` | Branches created | - |
| `inventory_categories_created_total` | Categories created | - |
| `inventory_low_stock_alerts_total` | Low stock alerts | `item_sku=...` |

### Standard HTTP Metrics

| Metric | Description | Labels |
|--------|-------------|--------|
| `http_server_requests_seconds_count` | Request count | `service`, `uri`, `method`, `status` |
| `http_server_requests_seconds_sum` | Total duration | `service`, `uri`, `method`, `status` |
| `http_server_requests_seconds_bucket` | Response time histogram | `service`, `uri`, `method`, `status`, `le` |

## 🔧 Troubleshooting

### Dashboard Not Loading

1. Check Grafana is running: `docker-compose ps grafana`
2. Check logs: `docker-compose logs grafana`
3. Verify datasource: Configuration → Data Sources → Prometheus

### No Metrics Showing

1. Verify app is running: `curl http://localhost:8080/metrics`
2. Check Prometheus targets: http://localhost:9090/targets
3. Ensure `ktor-microservice` target status is "UP"

### Grafana Permission Errors

If you see permission denied errors on startup:

```bash
chmod 644 grafana/provisioning/dashboards/*.yml
chmod 644 grafana/provisioning/dashboards/*.json
chmod 644 grafana/provisioning/datasources/*.yml
docker-compose restart grafana
```

## 📖 Next Steps

- Review the full monitoring guide: [MONITORING.md](./MONITORING.md)
- Set up alerting rules (see MONITORING.md)
- Create custom dashboards for specific use cases
- Export dashboards for backup

## 🎯 Pro Tips

1. **Auto-refresh**: Set dashboard auto-refresh (top-right) to 10s for real-time monitoring
2. **Time Range**: Adjust time range (top-right) to view historical data
3. **Variables**: Add template variables for filtering by service, branch, etc.
4. **Annotations**: Mark important events on your graphs
5. **Sharing**: Share dashboards with your team via JSON export or Grafana Cloud

Enjoy your monitoring! 📊✨
