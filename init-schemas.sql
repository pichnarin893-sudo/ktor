-- Initialize database schemas for NatJoub Microservices
-- This script creates the schemas that will be used by each service

-- Create auth schema
CREATE SCHEMA IF NOT EXISTS auth_schema;

-- Create inventory schema
CREATE SCHEMA IF NOT EXISTS inventory_schema;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA auth_schema TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA inventory_schema TO postgres;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA auth_schema GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA inventory_schema GRANT ALL ON TABLES TO postgres;
