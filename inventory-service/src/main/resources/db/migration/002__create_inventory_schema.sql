-- Inventory Service Database Schema
-- This database is owned entirely by inventory-service
-- No schema prefix needed - we own the whole database

-- Branches table: Stores physical locations/stores
CREATE TABLE IF NOT EXISTS branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    manager_name VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Categories table: Product categories
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Inventory items table: Products/items in inventory
CREATE TABLE IF NOT EXISTS inventory_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(100) UNIQUE NOT NULL, -- Stock Keeping Unit
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    unit_of_measure VARCHAR(50) NOT NULL, -- e.g., 'piece', 'kg', 'liter', 'box'
    unit_price DECIMAL(10, 2) NOT NULL,
    reorder_level INTEGER DEFAULT 10, -- Minimum stock level before reordering
    reorder_quantity INTEGER DEFAULT 50, -- Quantity to order when restocking
    barcode VARCHAR(255),
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Stock levels table: Current stock per branch
CREATE TABLE IF NOT EXISTS stock_levels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_item_id UUID NOT NULL REFERENCES inventory_items(id) ON DELETE CASCADE,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER DEFAULT 0, -- Quantity reserved for orders
    last_counted_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(inventory_item_id, branch_id)
);

-- Stock movements table: Track inventory in/out movements
CREATE TABLE IF NOT EXISTS stock_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_item_id UUID NOT NULL REFERENCES inventory_items(id) ON DELETE CASCADE,
    from_branch_id UUID REFERENCES branches(id) ON DELETE SET NULL,
    to_branch_id UUID REFERENCES branches(id) ON DELETE SET NULL,
    movement_type VARCHAR(50) NOT NULL, -- 'IN', 'OUT', 'TRANSFER', 'ADJUSTMENT', 'RETURN'
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2),
    reference_number VARCHAR(100), -- PO number, invoice number, etc.
    notes TEXT,
    performed_by UUID, -- Reference to user from auth-service (NO FK - different database!)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (quantity > 0),
    CHECK (from_branch_id IS NOT NULL OR to_branch_id IS NOT NULL)
);

-- Create indexes for performance
CREATE INDEX idx_stock_levels_item_id ON stock_levels(inventory_item_id);
CREATE INDEX idx_stock_levels_branch_id ON stock_levels(branch_id);
CREATE INDEX idx_stock_movements_item_id ON stock_movements(inventory_item_id);
CREATE INDEX idx_stock_movements_from_branch ON stock_movements(from_branch_id);
CREATE INDEX idx_stock_movements_to_branch ON stock_movements(to_branch_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements(created_at);
CREATE INDEX idx_stock_movements_performed_by ON stock_movements(performed_by);
CREATE INDEX idx_inventory_items_category_id ON inventory_items(category_id);
CREATE INDEX idx_inventory_items_sku ON inventory_items(sku);
CREATE INDEX idx_categories_parent ON categories(parent_category_id);

-- Function to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers to auto-update updated_at
CREATE TRIGGER update_branches_updated_at BEFORE UPDATE ON branches
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_inventory_items_updated_at BEFORE UPDATE ON inventory_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_stock_levels_updated_at BEFORE UPDATE ON stock_levels
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
