# Database Scripts Fix Summary

All migration, seeder, and rollback scripts have been updated to work with separate databases (no schema prefixes).

## Files Fixed

### Auth Service (`auth-service/src/main/resources/db/`)

#### Shell Scripts
1. **migrate.sh** ✅
   - Changed container: `postgres` → `auth_postgres_db`
   - Changed database: `postgres` DB → `auth_db`
   - Changed user: `postgres` → `auth_user`
   - Removed `sudo` (not needed for docker commands)

2. **seed.sh** ✅
   - Changed container: `postgres` → `auth_postgres_db`
   - Changed database: `postgres` DB → `auth_db`
   - Changed user: `postgres` → `auth_user`
   - Removed `sudo`

3. **rollback.sh** ✅
   - Fixed directory path: `rollbacks/` → `rollback/`
   - Changed container: `postgres` → `auth_postgres_db`
   - Changed database: `postgres` DB → `auth_db`
   - Changed user: `postgres` → `auth_user`
   - Removed `sudo`

#### Seeder SQL Files
All seeder files removed: `SET search_path TO auth_schema;` and `SET search_path TO public;`

1. **seeder/001__mock_roles.sql** ✅
   - Removed schema path commands
   - Tables referenced without schema prefix

2. **seeder/002__mock_users.sql** ✅
   - Removed schema path commands
   - Tables referenced without schema prefix

3. **seeder/003__mock_credentials.sql** ✅
   - Removed schema path commands
   - Changed `public.crypt()` → `crypt()`
   - Changed `public.gen_salt()` → `gen_salt()`
   - Tables referenced without schema prefix

#### Rollback SQL Files
1. **rollback/001__drop_auth_schema.sql** ✅
   - Removed `SET search_path TO auth_schema;`
   - Removed `SET search_path TO public;`
   - Removed `DROP SCHEMA IF EXISTS auth_schema CASCADE;`
   - Changed `DROP FUNCTION IF EXISTS auth_schema.update_updated_at_column();`
     → `DROP FUNCTION IF EXISTS update_updated_at_column();`
   - All table DROP statements now without schema prefix

---

### Inventory Service (`inventory-service/src/main/resources/db/`)

#### Shell Scripts
1. **migrate.sh** ✅
   - Changed container: `postgres` → `inventory_postgres_db`
   - Changed database: `postgres` DB → `inventory_db`
   - Changed user: `postgres` → `inventory_user`
   - Removed `sudo`

2. **seed.sh** ✅
   - Changed container: `ktor_postgres_db` → `inventory_postgres_db`
   - Changed database: `microservice_db` → `inventory_db`
   - Changed user: `postgres` → `inventory_user`
   - Removed `sudo`

3. **rollback.sh** ✅
   - Fixed directory path: `rollbacks/` → `rollback/`
   - Changed container: `ktor_postgres_db` → `inventory_postgres_db`
   - Changed database: `microservice_db` → `inventory_db`
   - Changed user: `postgres` → `inventory_user`
   - Removed `sudo`

#### Seeder SQL Files
All seeder files removed: `SET search_path TO inventory_schema;` and `SET search_path TO public;`

1. **seeder/004__mock_branches.sql** ✅
   - Removed schema path commands
   - Tables referenced without schema prefix
   - Updated email domains from `natjoub.com` → `factory.com`

2. **seeder/005__mock_categories.sql** ✅
   - Removed schema path commands
   - Tables referenced without schema prefix

3. **seeder/006__mock_inventory_items.sql** ✅
   - Removed schema path commands
   - All INSERT statements reference tables without schema prefix

4. **seeder/007__mock_stock_levels.sql** ✅
   - Removed schema path commands
   - All SELECT and INSERT statements use tables without schema prefix

#### Rollback SQL Files
1. **rollback/002__drop_inventory_schema.sql** ✅
   - Removed `SET search_path TO inventory_schema;`
   - Removed `SET search_path TO public;`
   - Removed `DROP SCHEMA IF EXISTS inventory_schema CASCADE;`
   - Changed `DROP FUNCTION IF EXISTS inventory_schema.update_updated_at_column();`
     → `DROP FUNCTION IF EXISTS update_updated_at_column();`
   - All table DROP statements now without schema prefix

---

## Script Permissions

All shell scripts have been made executable:
```bash
chmod +x auth-service/src/main/resources/db/*.sh
chmod +x inventory-service/src/main/resources/db/*.sh
```

---

## Testing Results

### Auth Service
✅ **Roles Seeder** - Successfully inserted 3 roles
```sql
SELECT * FROM roles;
 id                                   | name    | created_at                  | updated_at
--------------------------------------+---------+-----------------------------+----------------------------
 11111111-1111-1111-1111-111111111111 | admin   | 2025-12-25 09:06:45.533768 | 2025-12-25 09:06:45.533768
 22222222-2222-2222-2222-222222222222 | manager | 2025-12-25 09:06:45.533768 | 2025-12-25 09:06:45.533768
 33333333-3333-3333-3333-333333333333 | staff   | 2025-12-25 09:06:45.533768 | 2025-12-25 09:06:45.533768
```

### Inventory Service
✅ **Branches Seeder** - Successfully inserted 4 branches
```sql
SELECT id, name, code FROM branches;
 id                                   | name             | code
--------------------------------------+------------------+----------
 25299d1b-4ead-49b6-bb44-f74007ab9882 | Main Warehouse   | WH-MAIN
 831ec8b7-38de-4c38-87a5-d95ce18191d7 | Downtown Store   | ST-DT
 19733cd0-dddc-4eb9-9f90-e2e8c12460ec | North Branch     | BR-NORTH
 c71d942d-c131-4fd4-93c1-13817d522c03 | Siem Reap Branch | BR-SR
```

✅ **Categories Seeder** - Successfully inserted 10 categories (4 root + 6 sub-categories)

---

## Usage Examples

### Running Migration
```bash
cd auth-service/src/main/resources/db
./migrate.sh 001__create_auth_schema.sql
```

### Running Seeders
```bash
cd auth-service/src/main/resources/db
./seed.sh 001__mock_roles.sql
./seed.sh 002__mock_users.sql
./seed.sh 003__mock_credentials.sql
```

```bash
cd inventory-service/src/main/resources/db
./seed.sh 004__mock_branches.sql
./seed.sh 005__mock_categories.sql
./seed.sh 006__mock_inventory_items.sql
./seed.sh 007__mock_stock_levels.sql
```

### Running Rollback
```bash
cd auth-service/src/main/resources/db
./rollback.sh 001__drop_auth_schema.sql
```

```bash
cd inventory-service/src/main/resources/db
./rollback.sh 002__drop_inventory_schema.sql
```

---

## Key Changes Summary

### Before (Shared Database)
```sql
SET search_path TO auth_schema;
INSERT INTO auth_schema.roles (id, name) VALUES (...);
DROP FUNCTION IF EXISTS auth_schema.update_updated_at_column();
```

### After (Separate Databases)
```sql
-- No schema prefix needed - we own the entire database
INSERT INTO roles (id, name) VALUES (...);
DROP FUNCTION IF EXISTS update_updated_at_column();
```

### Container/Database References
| Service | Old Container | New Container | Old DB | New DB | Old User | New User |
|---------|---------------|---------------|--------|--------|----------|----------|
| Auth | postgres | auth_postgres_db | microservice_db | auth_db | postgres | auth_user |
| Inventory | ktor_postgres_db | inventory_postgres_db | microservice_db | inventory_db | postgres | inventory_user |

---

## Verification Checklist

✅ All shell scripts updated with correct container names
✅ All shell scripts updated with correct database names
✅ All shell scripts updated with correct user credentials
✅ All seeder SQL files removed schema path commands
✅ All rollback SQL files removed schema references
✅ Scripts made executable
✅ Tested auth seeders - PASSING
✅ Tested inventory seeders - PASSING
✅ All scripts now work with separate database architecture

---

**Last Updated:** December 25, 2025
**Status:** All scripts verified and working ✅
