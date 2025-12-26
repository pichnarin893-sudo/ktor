# API Endpoints Documentation

Complete reference for all REST API endpoints in the Factory Management Microservices System.

## Table of Contents
- [Authentication Endpoints](#authentication-endpoints)
- [Inventory Endpoints](#inventory-endpoints)
- [Order Endpoints](#order-endpoints)
- [Telegram Bot Commands](#telegram-bot-commands)
- [Response Format](#response-format)
- [Error Codes](#error-codes)

---

## Base URLs

| Service | Base URL | Description |
|---------|----------|-------------|
| Auth Service | `http://localhost:8081` | Authentication and user management |
| Inventory Service | `http://localhost:8082` | Product catalog and stock management |
| Order Service | `http://localhost:8083` | Order processing and tracking |

---

## Authentication Endpoints

### Auth Service (Port 8081)

#### 1. Register User
**POST** `/v1/auth/register`

**Access**: 🔓 Public

**Description**: Register a new user (employee or customer)

**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@factory.com",
  "password": "Password123@",
  "role": "employee",
  "telegramId": null
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "uuid",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@factory.com",
      "role": "employee"
    }
  },
  "message": "User registered successfully"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8081/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@factory.com",
    "password": "Password123@",
    "role": "employee"
  }'
```

---

#### 2. Login
**POST** `/v1/auth/login`

**Access**: 🔓 Public

**Description**: Authenticate user and receive JWT token

**Request Body**:
```json
{
  "identifier": "john.anderson@factory.com",
  "password": "Password123@"
}
```

**Note**: `identifier` can be email, username, or phone number

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "650e8400-e29b-41d4-a716-446655440001",
      "firstName": "John",
      "lastName": "Anderson",
      "email": "john.anderson@factory.com",
      "role": "employee"
    }
  },
  "message": "Login successful"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "john.anderson@factory.com",
    "password": "Password123@"
  }'

# Extract token
export TOKEN=$(curl -s -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"john.anderson@factory.com","password":"Password123@"}' \
  | jq -r '.data.token')
```

---

## Inventory Endpoints

### Inventory Service (Port 8082)

---

### Public Endpoints (No Authentication)

#### 1. Get All Categories
**GET** `/api/v1/inventory/categories`

**Access**: 🔓 Public

**Description**: Browse all product categories

**Query Parameters**:
- `limit` (optional, default: 100) - Number of results
- `offset` (optional, default: 0) - Pagination offset

**Response** (200 OK):
```json
{
  "data": [
    {
      "id": "850e8400-e29b-41d4-a716-446655440001",
      "name": "Electronics",
      "description": "Electronic devices and components",
      "parentCategoryId": null,
      "isActive": true,
      "createdAt": "2025-12-25T12:47:10.087268",
      "updatedAt": "2025-12-25T12:47:10.087268"
    }
  ],
  "total": 10,
  "page": 1,
  "pageSize": 100,
  "totalPages": 1
}
```

**cURL Example**:
```bash
# Get all categories
curl http://localhost:8082/api/v1/inventory/categories

# With pagination
curl "http://localhost:8082/api/v1/inventory/categories?limit=5&offset=0"
```

---

#### 2. Get Category by ID
**GET** `/api/v1/inventory/categories/{id}`

**Access**: 🔓 Public

**Description**: Get details of a specific category

**Response** (200 OK):
```json
{
  "id": "850e8400-e29b-41d4-a716-446655440001",
  "name": "Electronics",
  "description": "Electronic devices and components",
  "parentCategoryId": null,
  "isActive": true,
  "createdAt": "2025-12-25T12:47:10.087268",
  "updatedAt": "2025-12-25T12:47:10.087268"
}
```

**cURL Example**:
```bash
curl http://localhost:8082/api/v1/inventory/categories/850e8400-e29b-41d4-a716-446655440001
```

---

#### 3. Get All Products
**GET** `/api/v1/inventory/items`

**Access**: 🔓 Public

**Description**: Browse all products

**Query Parameters**:
- `limit` (optional, default: 100) - Number of results
- `offset` (optional, default: 0) - Pagination offset
- `categoryId` (optional) - Filter by category

**Response** (200 OK):
```json
{
  "data": [
    {
      "id": "950e8400-e29b-41d4-a716-446655440005",
      "sku": "PAPER-A4-5000",
      "name": "A4 Copy Paper",
      "description": "White A4 copy paper, 500 sheets per ream",
      "categoryId": "850e8400-e29b-41d4-a716-446655440002",
      "categoryName": "Office Supplies",
      "unitOfMeasure": "box",
      "unitPrice": "49.99",
      "reorderLevel": 50,
      "reorderQuantity": 100,
      "barcode": "7891234567894",
      "imageUrl": null,
      "isActive": true,
      "totalStock": 600,
      "createdAt": "2025-12-25T12:47:10.096455",
      "updatedAt": "2025-12-25T12:47:10.096455"
    }
  ],
  "total": 50,
  "page": 1,
  "pageSize": 100,
  "totalPages": 1
}
```

**cURL Examples**:
```bash
# Get all products
curl http://localhost:8082/api/v1/inventory/items

# Filter by category
curl "http://localhost:8082/api/v1/inventory/items?categoryId=850e8400-e29b-41d4-a716-446655440002"

# With pagination
curl "http://localhost:8082/api/v1/inventory/items?limit=10&offset=0"
```

---

#### 4. Get Product by ID
**GET** `/api/v1/inventory/items/{id}`

**Access**: 🔓 Public

**Description**: Get details of a specific product

**Response** (200 OK):
```json
{
  "id": "950e8400-e29b-41d4-a716-446655440005",
  "sku": "PAPER-A4-5000",
  "name": "A4 Copy Paper",
  "description": "White A4 copy paper, 500 sheets per ream",
  "categoryId": "850e8400-e29b-41d4-a716-446655440002",
  "categoryName": "Office Supplies",
  "unitOfMeasure": "box",
  "unitPrice": "49.99",
  "totalStock": 600,
  "isActive": true
}
```

**cURL Example**:
```bash
curl http://localhost:8082/api/v1/inventory/items/950e8400-e29b-41d4-a716-446655440005
```

---

### Protected Endpoints (Employee Only)

#### 5. Create Category
**POST** `/api/v1/inventory/categories`

**Access**: 🔐 Employee Only

**Authorization**: `Bearer <jwt-token>`

**Request Body**:
```json
{
  "name": "New Category",
  "description": "Category description",
  "parentCategoryId": null
}
```

**Response** (201 Created):
```json
{
  "id": "uuid",
  "name": "New Category",
  "description": "Category description",
  "parentCategoryId": null,
  "isActive": true,
  "createdAt": "2025-12-25T12:47:10.087268",
  "updatedAt": "2025-12-25T12:47:10.087268"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8082/api/v1/inventory/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Category",
    "description": "Category description"
  }'
```

---

#### 6. Create Product
**POST** `/api/v1/inventory/items`

**Access**: 🔐 Employee Only

**Authorization**: `Bearer <jwt-token>`

**Request Body**:
```json
{
  "sku": "PROD-001",
  "name": "New Product",
  "description": "Product description",
  "categoryId": "850e8400-e29b-41d4-a716-446655440001",
  "unitOfMeasure": "piece",
  "unitPrice": "99.99",
  "reorderLevel": 10,
  "reorderQuantity": 50,
  "barcode": "1234567890123"
}
```

**Response** (201 Created):
```json
{
  "id": "uuid",
  "sku": "PROD-001",
  "name": "New Product",
  "description": "Product description",
  "categoryId": "850e8400-e29b-41d4-a716-446655440001",
  "categoryName": "Electronics",
  "unitOfMeasure": "piece",
  "unitPrice": "99.99",
  "totalStock": 0,
  "isActive": true
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8082/api/v1/inventory/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "name": "New Product",
    "categoryId": "850e8400-e29b-41d4-a716-446655440001",
    "unitPrice": "99.99",
    "unitOfMeasure": "piece"
  }'
```

---

#### 7. Get All Branches
**GET** `/api/v1/inventory/branches`

**Access**: 🔐 Employee Only

**Authorization**: `Bearer <jwt-token>`

**Response** (200 OK):
```json
{
  "data": [
    {
      "id": "uuid",
      "name": "Main Warehouse",
      "code": "WH-001",
      "address": "123 Factory Street",
      "isActive": true
    }
  ],
  "total": 5,
  "page": 1,
  "pageSize": 100,
  "totalPages": 1
}
```

**cURL Example**:
```bash
curl http://localhost:8082/api/v1/inventory/branches \
  -H "Authorization: Bearer $TOKEN"
```

---

## Order Endpoints

### Order Service (Port 8083)

---

### Customer Endpoints

#### 1. Create Order
**POST** `/v1/customer/orders`

**Access**: 🔐 Customer or Employee

**Authorization**: `Bearer <jwt-token>`

**Description**: Place a new order

**Request Body**:
```json
{
  "items": [
    {
      "productId": "950e8400-e29b-41d4-a716-446655440005",
      "quantity": 2
    },
    {
      "productId": "950e8400-e29b-41d4-a716-446655440006",
      "quantity": 1
    }
  ],
  "deliveryAddress": "123 Main Street, City, Country",
  "notes": "Please deliver in the morning"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "customerId": "uuid",
    "items": [
      {
        "productId": "950e8400-e29b-41d4-a716-446655440005",
        "productName": "A4 Copy Paper",
        "quantity": 2,
        "unitPrice": "49.99",
        "subtotal": "99.98"
      }
    ],
    "totalAmount": "149.97",
    "status": "PENDING",
    "deliveryAddress": "123 Main Street, City, Country",
    "notes": "Please deliver in the morning",
    "createdAt": "2025-12-25T17:45:00.000000"
  },
  "message": "Order created successfully"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8083/v1/customer/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productId": "950e8400-e29b-41d4-a716-446655440005",
        "quantity": 2
      }
    ],
    "deliveryAddress": "123 Main Street",
    "notes": "Morning delivery preferred"
  }'
```

---

#### 2. Get My Orders
**GET** `/v1/customer/orders`

**Access**: 🔐 Customer or Employee

**Authorization**: `Bearer <jwt-token>`

**Description**: View own orders

**Query Parameters**:
- `limit` (optional, default: 100)
- `offset` (optional, default: 0)
- `status` (optional) - Filter by status: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "customerId": "uuid",
      "totalAmount": "149.97",
      "status": "PENDING",
      "deliveryAddress": "123 Main Street",
      "notes": "Morning delivery preferred",
      "createdAt": "2025-12-25T17:45:00.000000",
      "updatedAt": "2025-12-25T17:45:00.000000"
    }
  ]
}
```

**cURL Example**:
```bash
# Get all my orders
curl http://localhost:8083/v1/customer/orders \
  -H "Authorization: Bearer $TOKEN"

# Filter by status
curl "http://localhost:8083/v1/customer/orders?status=PENDING" \
  -H "Authorization: Bearer $TOKEN"
```

---

### Employee Endpoints

#### 3. Get All Orders
**GET** `/v1/employee/orders`

**Access**: 🔐 Employee Only

**Authorization**: `Bearer <jwt-token>`

**Description**: View all orders in the system

**Query Parameters**:
- `limit` (optional, default: 100)
- `offset` (optional, default: 0)
- `status` (optional)
- `customerId` (optional)

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "customerId": "uuid",
      "customerName": "Alice Cooper",
      "totalAmount": "149.97",
      "status": "PENDING",
      "deliveryAddress": "123 Main Street",
      "createdAt": "2025-12-25T17:45:00.000000"
    }
  ],
  "total": 50,
  "page": 1,
  "pageSize": 100
}
```

**cURL Example**:
```bash
curl http://localhost:8083/v1/employee/orders \
  -H "Authorization: Bearer $TOKEN"
```

---

#### 4. Update Order Status
**PUT** `/v1/employee/orders/{orderId}/status`

**Access**: 🔐 Employee Only

**Authorization**: `Bearer <jwt-token>`

**Description**: Update order status

**Request Body**:
```json
{
  "status": "PROCESSING",
  "notes": "Order is being prepared"
}
```

**Valid Statuses**:
- `PENDING` - Order placed, not yet processed
- `PROCESSING` - Order is being prepared
- `SHIPPED` - Order has been shipped
- `DELIVERED` - Order delivered to customer
- `CANCELLED` - Order cancelled

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "status": "PROCESSING",
    "updatedAt": "2025-12-25T18:00:00.000000"
  },
  "message": "Order status updated successfully"
}
```

**cURL Example**:
```bash
curl -X PUT http://localhost:8083/v1/employee/orders/{orderId}/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PROCESSING",
    "notes": "Order is being prepared"
  }'
```

---

## Telegram Bot Commands

### Customer Interface via Telegram

**Bot Name**: Search for your bot in Telegram app

**Available Commands**:

#### 1. /start
**Description**: Register as a customer and get started

**Response**:
```
✅ You're all set!

Available commands:
/products - Browse all products
/categories - View product categories
/category <id> - Browse by category
/order <id> <qty> - Place an order
/myorders - View your orders
/help - Show help

Use /categories to start shopping!
```

---

#### 2. /categories
**Description**: Browse all product categories

**Response**:
```
📂 Product Categories:

📁 Electronics
   🆔 850e8400-e29b-41d4-a716-446655440001
📁 Office Supplies
   🆔 850e8400-e29b-41d4-a716-446655440002
...

To browse: /category <category_id>
Example: /category 850e8400-e29b-41d4-a716-446655440001
```

---

#### 3. /category <category_id>
**Description**: View products in a specific category

**Example**: `/category 850e8400-e29b-41d4-a716-446655440002`

**Response**:
```
🛍️ Products in Category:

📦 A4 Copy Paper
💰 $49.99
🆔 950e8400-e29b-41d4-a716-446655440005

📦 Stapler
💰 $15.99
🆔 950e8400-e29b-41d4-a716-446655440006

To order: /order <product_id> <quantity>
```

---

#### 4. /products
**Description**: Browse all products

**Response**:
```
🛍️ Available Products:

📦 A4 Copy Paper
💰 $49.99
🆔 950e8400-e29b-41d4-a716-446655440005

📦 Desk Lamp
💰 $35.99
🆔 950e8400-e29b-41d4-a716-446655440007
...
```

---

#### 5. /order <product_id> <quantity>
**Description**: Place an order (auto-login happens automatically)

**Example**: `/order 950e8400-e29b-41d4-a716-446655440005 2`

**Response**:
```
✅ Order created!

Order ID: 12345678...
Total: $99.98
Status: PENDING

Track: /myorders
```

---

#### 6. /myorders
**Description**: View your order history

**Response**:
```
📦 Your Orders:

📋 Order #12345678
💰 $99.98
📦 PENDING

📋 Order #87654321
💰 $149.97
📦 DELIVERED
```

---

#### 7. /help
**Description**: Show all available commands

**Response**:
```
🏭 Factory Store Bot

📋 Commands:
/start - Register/Start
/products - Browse all products
/categories - View categories
/category <id> - Browse by category
/order <id> <qty> - Place order
/myorders - View your orders
/help - Show this help

💡 Example:
1. /categories
2. /category abc-123
3. /order product-id 2
```

---

## Response Format

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

### Paginated Response
```json
{
  "data": [ ... ],
  "total": 100,
  "page": 1,
  "pageSize": 20,
  "totalPages": 5
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "error": {
    "code": "ERROR_CODE",
    "details": "Additional error details"
  }
}
```

---

## Error Codes

### HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request format or parameters |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists |
| 500 | Internal Server Error | Server-side error |

### Application Error Codes

| Code | Description |
|------|-------------|
| `INVALID_CREDENTIALS` | Wrong email/password |
| `USER_EXISTS` | Email already registered |
| `INVALID_TOKEN` | JWT token is invalid or expired |
| `INSUFFICIENT_PERMISSIONS` | User doesn't have required role |
| `ITEM_NOT_FOUND` | Product not found |
| `CATEGORY_NOT_FOUND` | Category not found |
| `ORDER_NOT_FOUND` | Order not found |
| `INSUFFICIENT_STOCK` | Not enough stock available |
| `INVALID_STATUS` | Invalid order status transition |

---

## Rate Limiting

Currently no rate limiting is implemented. In production, consider:
- 100 requests per minute per IP for public endpoints
- 1000 requests per minute for authenticated endpoints

---

## Versioning

All APIs are currently at version `v1`. Future versions will be released as `v2`, `v3`, etc.

---

## Support

For issues or questions:
1. Check service logs: `docker logs <service_name>`
2. Review this documentation
3. Check [README.md](./README.md) for troubleshooting

---

## Testing Examples

### Complete Customer Order Flow (REST API)

```bash
# 1. Browse products (no auth)
curl http://localhost:8082/api/v1/inventory/categories

# 2. View products in category
curl "http://localhost:8082/api/v1/inventory/items?categoryId=850e8400-e29b-41d4-a716-446655440002"

# 3. Register customer
curl -X POST http://localhost:8081/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane@example.com",
    "password": "Password123@",
    "role": "customer"
  }'

# 4. Login and get token
TOKEN=$(curl -s -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"jane@example.com","password":"Password123@"}' \
  | jq -r '.data.token')

# 5. Place order
curl -X POST http://localhost:8083/v1/customer/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [{"productId":"950e8400-e29b-41d4-a716-446655440005","quantity":2}],
    "deliveryAddress": "123 Main St"
  }'

# 6. View my orders
curl http://localhost:8083/v1/customer/orders \
  -H "Authorization: Bearer $TOKEN"
```

### Complete Employee Flow

```bash
# 1. Login as employee
TOKEN=$(curl -s -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"john.anderson@factory.com","password":"Password123@"}' \
  | jq -r '.data.token')

# 2. Create new product
curl -X POST http://localhost:8082/api/v1/inventory/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "NEW-001",
    "name": "New Product",
    "categoryId": "850e8400-e29b-41d4-a716-446655440001",
    "unitPrice": "99.99",
    "unitOfMeasure": "piece"
  }'

# 3. View all orders
curl http://localhost:8083/v1/employee/orders \
  -H "Authorization: Bearer $TOKEN"

# 4. Update order status
curl -X PUT http://localhost:8083/v1/employee/orders/{orderId}/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"PROCESSING"}'
```

---

**Last Updated**: December 25, 2025
**API Version**: v1
**Documentation Version**: 1.0.0
