-- Demo seed data for Inventory Service

-- Insert branches (physical locations/warehouses)
INSERT INTO branches (id, name, code, address, city, country, phone_number, email, manager_name, is_active, created_at, updated_at) VALUES
    ('750e8400-e29b-41d4-a716-446655440001', 'Main Warehouse', 'WH-MAIN', '123 Industrial Park Ave', 'New York', 'USA', '+1-555-0101', 'warehouse.main@factory.com', 'Michael Chen', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('750e8400-e29b-41d4-a716-446655440002', 'Downtown Store', 'ST-DOWNTOWN', '456 Commerce Street', 'Los Angeles', 'USA', '+1-555-0102', 'store.downtown@factory.com', 'Emma Johnson', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('750e8400-e29b-41d4-a716-446655440003', 'East Distribution Center', 'DC-EAST', '789 Logistics Blvd', 'Chicago', 'USA', '+1-555-0103', 'dc.east@factory.com', 'David Martinez', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Insert categories
INSERT INTO categories (id, name, description, parent_category_id, is_active, created_at, updated_at) VALUES
    ('850e8400-e29b-41d4-a716-446655440001', 'Electronics', 'Electronic devices and components', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('850e8400-e29b-41d4-a716-446655440002', 'Office Supplies', 'General office and stationery items', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('850e8400-e29b-41d4-a716-446655440003', 'Raw Materials', 'Manufacturing raw materials and components', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('850e8400-e29b-41d4-a716-446655440004', 'Tools & Equipment', 'Manufacturing tools and equipment', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('850e8400-e29b-41d4-a716-446655440005', 'Safety Equipment', 'Personal protective equipment and safety gear', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Sub-categories
INSERT INTO categories (id, name, description, parent_category_id, is_active, created_at, updated_at) VALUES
    ('850e8400-e29b-41d4-a716-446655440010', 'Computers', 'Desktop and laptop computers', '850e8400-e29b-41d4-a716-446655440001', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('850e8400-e29b-41d4-a716-446655440011', 'Peripherals', 'Computer accessories and peripherals', '850e8400-e29b-41d4-a716-446655440001', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Insert inventory items
INSERT INTO inventory_items (id, sku, name, description, category_id, unit_of_measure, unit_price, reorder_level, reorder_quantity, barcode, is_active, created_at, updated_at) VALUES
    -- Electronics
    ('950e8400-e29b-41d4-a716-446655440001', 'LAPTOP-DEL-XPS13', 'Dell XPS 13 Laptop', '13-inch premium ultrabook with Intel i7 processor', '850e8400-e29b-41d4-a716-446655440010', 'piece', 1299.99, 5, 10, '7891234567890', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('950e8400-e29b-41d4-a716-446655440002', 'MOUSE-LOG-MX3', 'Logitech MX Master 3 Mouse', 'Wireless ergonomic mouse', '850e8400-e29b-41d4-a716-446655440011', 'piece', 99.99, 20, 50, '7891234567891', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('950e8400-e29b-41d4-a716-446655440003', 'KB-MECH-K95', 'Corsair K95 Mechanical Keyboard', 'RGB mechanical gaming keyboard', '850e8400-e29b-41d4-a716-446655440011', 'piece', 199.99, 15, 30, '7891234567892', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('950e8400-e29b-41d4-a716-446655440004', 'MONITOR-DELL-27', 'Dell 27" 4K Monitor', 'Ultra HD 4K display monitor', '850e8400-e29b-41d4-a716-446655440011', 'piece', 549.99, 10, 20, '7891234567893', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Office Supplies
    ('950e8400-e29b-41d4-a716-446655440005', 'PAPER-A4-5000', 'A4 Copy Paper', 'White A4 copy paper, 500 sheets per ream', '850e8400-e29b-41d4-a716-446655440002', 'box', 49.99, 50, 100, '7891234567894', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('950e8400-e29b-41d4-a716-446655440006', 'PEN-PILOT-G2', 'Pilot G2 Gel Pen', 'Premium gel ink pens, black, box of 12', '850e8400-e29b-41d4-a716-446655440002', 'box', 15.99, 30, 60, '7891234567895', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('950e8400-e29b-41d4-a716-446655440007', 'NOTEBOOK-MOL-A5', 'Moleskine A5 Notebook', 'Classic hardcover notebook', '850e8400-e29b-41d4-a716-446655440002', 'piece', 19.99, 25, 50, '7891234567896', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Raw Materials
    ('950e8400-e29b-41d4-a716-446655440008', 'STEEL-SHEET-4X8', 'Steel Sheet 4x8ft', 'Cold rolled steel sheet, 1/8 inch thickness', '850e8400-e29b-41d4-a716-446655440003', 'sheet', 89.99, 20, 50, '7891234567897', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('950e8400-e29b-41d4-a716-446655440009', 'ALUMINUM-ROD-1IN', 'Aluminum Rod 1" Diameter', '6061 aluminum rod, 12ft length', '850e8400-e29b-41d4-a716-446655440003', 'piece', 45.99, 15, 40, '7891234567898', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('950e8400-e29b-41d4-a716-446655440010', 'PLASTIC-PVC-PIPE', 'PVC Pipe 2" Diameter', 'Schedule 40 PVC pipe, 10ft length', '850e8400-e29b-41d4-a716-446655440003', 'piece', 12.99, 40, 80, '7891234567899', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Tools & Equipment
    ('950e8400-e29b-41d4-a716-446655440011', 'DRILL-DEWALT-20V', 'DeWalt 20V Cordless Drill', 'Professional cordless drill kit', '850e8400-e29b-41d4-a716-446655440004', 'piece', 179.99, 8, 15, '7891234567900', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('950e8400-e29b-41d4-a716-446655440012', 'SAW-CIRCULAR-7.25', 'Circular Saw 7.25"', 'Electric circular saw with laser guide', '850e8400-e29b-41d4-a716-446655440004', 'piece', 129.99, 6, 12, '7891234567901', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Safety Equipment
    ('950e8400-e29b-41d4-a716-446655440013', 'HELMET-HARD-WHITE', 'White Hard Hat', 'ANSI certified safety helmet', '850e8400-e29b-41d4-a716-446655440005', 'piece', 24.99, 30, 60, '7891234567902', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('950e8400-e29b-41d4-a716-446655440014', 'GLOVES-SAFETY-L', 'Safety Gloves Size L', 'Cut-resistant work gloves, size large', '850e8400-e29b-41d4-a716-446655440005', 'pair', 12.99, 50, 100, '7891234567903', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('950e8400-e29b-41d4-a716-446655440015', 'GOGGLES-SAFETY', 'Safety Goggles', 'Anti-fog safety goggles with UV protection', '850e8400-e29b-41d4-a716-446655440005', 'piece', 9.99, 40, 80, '7891234567904', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (sku) DO NOTHING;

-- Insert stock levels for each branch
-- Main Warehouse (large stock)
INSERT INTO stock_levels (id, inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at, updated_at) VALUES
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440001', 25, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440001', 150, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440001', 80, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440004', '750e8400-e29b-41d4-a716-446655440001', 45, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440005', '750e8400-e29b-41d4-a716-446655440001', 500, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440006', '750e8400-e29b-41d4-a716-446655440001', 200, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440007', '750e8400-e29b-41d4-a716-446655440001', 120, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440008', '750e8400-e29b-41d4-a716-446655440001', 100, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440009', '750e8400-e29b-41d4-a716-446655440001', 75, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440010', '750e8400-e29b-41d4-a716-446655440001', 200, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440011', '750e8400-e29b-41d4-a716-446655440001', 30, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440012', '750e8400-e29b-41d4-a716-446655440001', 20, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440013', '750e8400-e29b-41d4-a716-446655440001', 150, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440014', '750e8400-e29b-41d4-a716-446655440001', 250, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440015', '750e8400-e29b-41d4-a716-446655440001', 180, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (inventory_item_id, branch_id) DO NOTHING;

-- Downtown Store (medium stock)
INSERT INTO stock_levels (id, inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at, updated_at) VALUES
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440002', 8, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440002', 45, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440002', 30, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440004', '750e8400-e29b-41d4-a716-446655440002', 15, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440005', '750e8400-e29b-41d4-a716-446655440002', 100, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440006', '750e8400-e29b-41d4-a716-446655440002', 60, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440007', '750e8400-e29b-41d4-a716-446655440002', 40, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (inventory_item_id, branch_id) DO NOTHING;

-- East Distribution Center (varied stock)
INSERT INTO stock_levels (id, inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at, updated_at) VALUES
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440008', '750e8400-e29b-41d4-a716-446655440003', 150, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440009', '750e8400-e29b-41d4-a716-446655440003', 120, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440010', '750e8400-e29b-41d4-a716-446655440003', 300, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440011', '750e8400-e29b-41d4-a716-446655440003', 12, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440012', '750e8400-e29b-41d4-a716-446655440003', 8, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440013', '750e8400-e29b-41d4-a716-446655440003', 80, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440014', '750e8400-e29b-41d4-a716-446655440003', 150, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440015', '750e8400-e29b-41d4-a716-446655440003', 100, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (inventory_item_id, branch_id) DO NOTHING;

-- Insert some stock movements for history (performed by employees)
INSERT INTO stock_movements (id, inventory_item_id, from_branch_id, to_branch_id, movement_type, quantity, unit_price, reference_number, notes, performed_by, created_at) VALUES
    -- Initial stock received (performed by John Anderson - Employee)
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440001', NULL, '750e8400-e29b-41d4-a716-446655440001', 'IN', 30, 1299.99, 'PO-2024-001', 'Initial purchase order from supplier', '650e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP - INTERVAL '30 days'),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440002', NULL, '750e8400-e29b-41d4-a716-446655440001', 'IN', 200, 99.99, 'PO-2024-002', 'Bulk order of mice', '650e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP - INTERVAL '28 days'),

    -- Transfers between branches (performed by Michael Chen - Employee)
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440002', 'TRANSFER', 10, 1299.99, 'TRF-2024-001', 'Transfer to downtown store', '650e8400-e29b-41d4-a716-446655440003', CURRENT_TIMESTAMP - INTERVAL '15 days'),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440002', 'TRANSFER', 50, 99.99, 'TRF-2024-002', 'Restocking downtown store', '650e8400-e29b-41d4-a716-446655440003', CURRENT_TIMESTAMP - INTERVAL '12 days'),

    -- Stock adjustments (performed by Sarah Williams - Employee)
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440005', NULL, '750e8400-e29b-41d4-a716-446655440001', 'IN', 500, 49.99, 'PO-2024-015', 'Paper stock replenishment', '650e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '7 days'),
    (gen_random_uuid(), '950e8400-e29b-41d4-a716-446655440013', NULL, '750e8400-e29b-41d4-a716-446655440001', 'IN', 200, 24.99, 'PO-2024-020', 'Safety equipment order', '650e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '5 days')
ON CONFLICT DO NOTHING;
