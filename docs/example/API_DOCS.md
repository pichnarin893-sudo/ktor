# API Documentation

## Base URL
```
http://localhost:8080/api/v1
```

## Authentication
Most endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

## Common Response Codes
- `200 OK`: Success
- `201 Created`: Resource created successfully
- `204 No Content`: Success with no response body
- `400 Bad Request`: Invalid input
- `401 Unauthorized`: Authentication required
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource already exists
- `500 Internal Server Error`: Server error

## Error Response Format
```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "timestamp": 1234567890
}
```

---

## Users API

### List Users
```
GET /api/v1/users
```

**Query Parameters:**
- `limit` (optional): Number of users to return (default: 10, max: 100)
- `offset` (optional): Number of users to skip (default: 0)

**Response: 200 OK**
```json
[
  {
    "id": 1,
    "email": "user@example.com",
    "username": "johndoe",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

---

### Get User by ID
```
GET /api/v1/users/{id}
```

**Path Parameters:**
- `id`: User ID (integer)

**Response: 200 OK**
```json
{
  "id": 1,
  "email": "user@example.com",
  "username": "johndoe",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Response: 404 Not Found**
```json
{
  "error": "Not Found",
  "message": "User with id 999 not found",
  "timestamp": 1234567890
}
```

---

### Create User
```
POST /api/v1/users
```

**Request Body:**
```json
{
  "email": "newuser@example.com",
  "username": "newuser",
  "password": "securePassword123"
}
```

**Validation Rules:**
- `email`: Required, valid email format
- `username`: Required, minimum 3 characters
- `password`: Required, minimum 8 characters

**Response: 201 Created**
```json
{
  "id": 2,
  "email": "newuser@example.com",
  "username": "newuser",
  "createdAt": "2024-01-15T11:00:00",
  "updatedAt": "2024-01-15T11:00:00"
}
```

**Response: 400 Bad Request**
```json
{
  "error": "Validation Error",
  "message": "Email cannot be blank",
  "timestamp": 1234567890
}
```

**Response: 409 Conflict**
```json
{
  "error": "Conflict",
  "message": "User with email newuser@example.com already exists",
  "timestamp": 1234567890
}
```

---

### Update User
```
PUT /api/v1/users/{id}
```

**Path Parameters:**
- `id`: User ID (integer)

**Request Body:**
```json
{
  "email": "updated@example.com",
  "username": "updatedname"
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Response: 200 OK**
```json
{
  "id": 1,
  "email": "updated@example.com",
  "username": "updatedname",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T12:00:00"
}
```

---

### Delete User
```
DELETE /api/v1/users/{id}
```

**Path Parameters:**
- `id`: User ID (integer)

**Response: 204 No Content**

**Response: 404 Not Found**
```json
{
  "error": "Not Found",
  "message": "User with id 999 not found",
  "timestamp": 1234567890
}
```

---

## Health & Monitoring Endpoints

### Health Check
```
GET /health
```

**Response: 200 OK**
```json
{
  "status": "UP",
  "timestamp": 1234567890,
  "checks": {
    "database": "healthy"
  }
}
```

**Response: 503 Service Unavailable** (if unhealthy)
```json
{
  "status": "DOWN",
  "timestamp": 1234567890,
  "checks": {
    "database": "unhealthy: Connection timeout"
  }
}
```

---

### Readiness Probe
```
GET /health/ready
```

**Response: 200 OK**
```json
{
  "status": "ready"
}
```

---

### Liveness Probe
```
GET /health/live
```

**Response: 200 OK**
```json
{
  "status": "alive"
}
```

---

### Prometheus Metrics
```
GET /metrics
```

**Response: 200 OK**
```
# HELP http_server_requests_seconds  
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="GET",status="200",uri="/api/v1/users",} 42.0
...
```

---

## Example Usage with cURL

### Create a user
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "username": "testuser",
    "password": "password123"
  }'
```

### Get all users
```bash
curl http://localhost:8080/api/v1/users?limit=10&offset=0
```

### Get specific user
```bash
curl http://localhost:8080/api/v1/users/1
```

### Update user
```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newemail@example.com"
  }'
```

### Delete user
```bash
curl -X DELETE http://localhost:8080/api/v1/users/1
```

### Check health
```bash
curl http://localhost:8080/health
```

---

## Rate Limiting (Future Enhancement)
Rate limiting will be implemented with the following limits:
- Anonymous: 100 requests/hour
- Authenticated: 1000 requests/hour
- Headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`
