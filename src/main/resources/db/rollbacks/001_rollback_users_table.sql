-- Rollback: Drop users table
-- Version: 001
-- Date: 2024-11-28

-- Drop indexes
DROP INDEX IF EXISTS idx_users_created_at;
DROP INDEX IF EXISTS idx_users_username;
DROP INDEX IF EXISTS idx_users_email;

-- Drop table
DROP TABLE IF EXISTS users CASCADE;

-- Log rollback
DO $$
BEGIN
    RAISE NOTICE 'Rollback 001: Users table dropped successfully';
END $$;