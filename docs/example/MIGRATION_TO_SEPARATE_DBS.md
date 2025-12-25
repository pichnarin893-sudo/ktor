# Migration to Separate Databases - Complete

## Summary

Successfully migrated from a **shared database with schema separation** to **truly separate databases** following microservice best practices: **one service → one database**.

## What Changed

### Before (Shared Database)
```
┌─────────────────────────────────────┐
│   Single PostgreSQL Instance        │
│   (factory_postgres_db)              │
│                                      │
│   ├─ auth_schema                    │ ← Both services
│   └─ inventory_schema                │ ← connected here
└─────────────────────────────────────┘
```

### After (Separate Databases)
```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│ auth-service │────────▶│  auth_db     │         │              │
│ (port 8081)  │         │ (PostgreSQL) │         │              │
└──────────────┘         │ :5432        │         │              │
                         └──────────────┘         │              │
                                                  │              │
┌──────────────┐         ┌──────────────┐         │              │
│inventory-svc │────────▶│ inventory_db │         │              │
│ (port 8082)  │         │ (PostgreSQL) │         │              │
└──────────────┘         │ :5433        │         │              │
                         └──────────────┘         │              │
                                                  │              │
┌──────────────┐         ┌──────────────┐         │              │
│telegram-svc  │────────▶│ telegram_db  │         │              │
│ (future)     │         │ (PostgreSQL) │         │              │
└──────────────┘         │ :5434        │         │              │
                         └──────────────┘         │              │
```

## Files Modified

### 1. Infrastructure Configuration

**docker-compose.yml**
- ✅ Added 3 separate PostgreSQL containers: `auth-db`, `inventory-db`, `telegram-db`
- ✅ Each database runs on its own port: 5432, 5433, 5434
- ✅ Each has its own credentials and database name
- ✅ Services now connect to their own dedicated databases

**Removed:**
- ❌ `init-schemas.sql` - No longer needed (no shared schema setup)
- ❌ `docker-compose.yml.backup` - Backup of old shared config

### 2. Database Migration Scripts

**auth-service/src/main/resources/db/migration/001__create_auth_schema.sql**
- ✅ Removed `CREATE SCHEMA IF NOT EXISTS auth_schema;`
- ✅ Removed `SET search_path TO auth_schema;`
- ✅ All table definitions now in public schema
- ✅ Functions no longer prefixed with `auth_schema.`

**inventory-service/src/main/resources/db/migration/002__create_inventory_schema.sql**
- ✅ Removed `CREATE SCHEMA IF NOT EXISTS inventory_schema;`
- ✅ Removed `SET search_path TO inventory_schema;`
- ✅ All table definitions now in public schema
- ✅ Functions no longer prefixed with `inventory_schema.`
- ✅ Comment added noting `performed_by` references auth-service (NO FK - different database)

### 3. Exposed Table Definitions (ORM)

**auth-service/src/main/kotlin/com/factory/auth/models/entity/Tables.kt**
- Changed: `Table("auth_schema.roles")` → `Table("roles")`
- Changed: `Table("auth_schema.users")` → `Table("users")`
- Changed: `Table("auth_schema.credentials")` → `Table("credentials")`
- Changed: `Table("auth_schema.refresh_tokens")` → `Table("refresh_tokens")`
- Changed: `Table("auth_schema.token_blacklist")` → `Table("token_blacklist")`

**inventory-service/src/main/kotlin/com/factory/inventory/models/entity/Tables.kt**
- Changed: `Table("inventory_schema.branches")` → `Table("branches")`
- Changed: `Table("inventory_schema.categories")` → `Table("categories")`
- Changed: `Table("inventory_schema.inventory_items")` → `Table("inventory_items")`
- Changed: `Table("inventory_schema.stock_levels")` → `Table("stock_levels")`
- Changed: `Table("inventory_schema.stock_movements")` → `Table("stock_movements")`

### 4. Build Configuration

**build.gradle.kts (root)**
- ✅ Added Java compilation target configuration to match Kotlin JVM target (21)
- ✅ Fixed JVM-target compatibility issues

**Cleanup:**
- ❌ Removed `auth-service/bin/` - Contained old compiled code with schema prefixes
- ❌ Removed `inventory-service/bin/` - Contained old compiled code with schema prefixes

## Database Structure Verification

### Auth DB (auth_postgres_db:5432)
```sql
\dt
              List of relations
 Schema |      Name       | Type  |   Owner
--------+-----------------+-------+-----------
 public | credentials     | table | auth_user
 public | refresh_tokens  | table | auth_user
 public | roles           | table | auth_user
 public | token_blacklist | table | auth_user
 public | users           | table | auth_user
```

### Inventory DB (inventory_postgres_db:5433)
```sql
\dt
                 List of relations
 Schema |      Name       | Type  |     Owner
--------+-----------------+-------+----------------
 public | branches        | table | inventory_user
 public | categories      | table | inventory_user
 public | inventory_items | table | inventory_user
 public | stock_levels    | table | inventory_user
 public | stock_movements | table | inventory_user
```

### Telegram DB (telegram_postgres_db:5434)
```sql
-- Ready for future telegram-service implementation
-- Will contain: telegram_users, user_profiles_cache, notification_preferences, etc.
```

## Service Status

```bash
docker-compose ps
```

```
Name                   State      Ports
---------------------------------------------
auth_postgres_db       Up (healthy)   0.0.0.0:5432->5432/tcp
auth_service           Up (healthy)   0.0.0.0:8081->8081/tcp
inventory_postgres_db  Up (healthy)   0.0.0.0:5433->5432/tcp
inventory_service      Up (healthy)   0.0.0.0:8082->8082/tcp
telegram_postgres_db   Up (healthy)   0.0.0.0:5434->5432/tcp
```

**Health Check:**
- Auth Service: http://localhost:8081/health → `OK`
- Inventory Service: http://localhost:8082/health → `OK`

## Connection Strings (for reference)

### Auth Service
```
DATABASE_URL=jdbc:postgresql://auth-db:5432/auth_db
DATABASE_USER=auth_user
DATABASE_PASSWORD=auth_password
```

### Inventory Service
```
DATABASE_URL=jdbc:postgresql://inventory-db:5432/inventory_db
DATABASE_USER=inventory_user
DATABASE_PASSWORD=inventory_password
```

### Telegram Service (Future)
```
DATABASE_URL=jdbc:postgresql://telegram-db:5432/telegram_db
DATABASE_USER=telegram_user
DATABASE_PASSWORD=telegram_password
```

## Benefits Achieved

✅ **True Service Isolation** - Each service owns its own database completely
✅ **Independent Scaling** - Can scale auth_db and inventory_db separately
✅ **Failure Isolation** - One database crash doesn't kill all services
✅ **Technology Flexibility** - Future services can use different databases (MongoDB, Redis, etc.)
✅ **Clear Ownership** - Each team owns their database schema and migrations
✅ **Security Isolation** - Different credentials per database
✅ **Proper Microservice Architecture** - Follows "one service → one database" principle

## Important Notes

1. **No Data Migration Needed** - Started fresh (this is for education/mock data)
2. **Cross-Service References** - Fields like `stock_movements.performed_by` (references users) have NO foreign key constraints because they're in different databases
3. **Inter-Service Communication** - Services must use HTTP APIs to get data from other services (e.g., inventory-service calls auth-service API for user data)
4. **Future Telegram Service** - Database already created and ready for implementation

## Next Steps

To implement telegram-service with its own database, follow the same pattern:
1. Create `telegram-service/` module
2. Configure it to use `jdbc:postgresql://telegram-db:5432/telegram_db`
3. Create migration SQL files (no schema prefix)
4. Define Exposed tables without schema prefix
5. Add service to docker-compose.yml
6. Implement AuthServiceClient for inter-service communication

---

**Migration completed successfully on:** $(date)
**All services verified and healthy**
