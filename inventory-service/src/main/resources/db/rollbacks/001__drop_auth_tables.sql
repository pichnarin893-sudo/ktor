-- Rollback script for auth tables
-- This script drops all tables and the schema created in 001__create_auth_tables.sql

-- Set search path to auth schema
SET search_path TO auth_schema;

-- Drop triggers first
DROP TRIGGER IF EXISTS update_roles_updated_at ON roles;
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
DROP TRIGGER IF EXISTS update_credentials_updated_at ON credentials;

-- Drop function
DROP FUNCTION IF EXISTS auth_schema.update_updated_at_column();

-- Drop tables in reverse order of dependencies
DROP TABLE IF EXISTS token_blacklist CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS credentials CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- Reset search path
SET search_path TO public;

-- Drop schema
DROP SCHEMA IF EXISTS auth_schema CASCADE;
