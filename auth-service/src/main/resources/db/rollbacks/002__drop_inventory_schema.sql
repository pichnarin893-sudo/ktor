-- Rollback script for inventory schema
-- This script drops all tables and the schema created in 002__create_inventory_schema.sql

-- Set search path to inventory schema
SET search_path TO inventory_schema;

-- Drop triggers first
DROP TRIGGER IF EXISTS update_branches_updated_at ON branches;
DROP TRIGGER IF EXISTS update_categories_updated_at ON categories;
DROP TRIGGER IF EXISTS update_inventory_items_updated_at ON inventory_items;
DROP TRIGGER IF EXISTS update_stock_levels_updated_at ON stock_levels;

-- Drop function
DROP FUNCTION IF EXISTS inventory_schema.update_updated_at_column();

-- Drop tables in reverse order of dependencies
DROP TABLE IF EXISTS stock_movements CASCADE;
DROP TABLE IF EXISTS stock_levels CASCADE;
DROP TABLE IF EXISTS inventory_items CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS branches CASCADE;

-- Reset search path
SET search_path TO public;

-- Drop schema
DROP SCHEMA IF EXISTS inventory_schema CASCADE;
