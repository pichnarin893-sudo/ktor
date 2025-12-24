-- Seed data for inventory items
SET search_path TO inventory_schema;

-- Get category IDs for reference
DO $$
DECLARE
    electronics_id UUID := '11111111-1111-1111-1111-111111111111';
    furniture_id UUID := '22222222-2222-2222-2222-222222222222';
    office_supplies_id UUID := '33333333-3333-3333-3333-333333333333';
    clothing_id UUID := '44444444-4444-4444-4444-444444444444';
BEGIN
    -- Electronics items
    INSERT INTO inventory_items (sku, name, description, category_id, unit_of_measure, unit_price, reorder_level, reorder_quantity, barcode, created_at, updated_at)
    VALUES
        ('ELEC-LAP-001', 'Dell Latitude 5420 Laptop', '14" Intel Core i5, 8GB RAM, 256GB SSD', electronics_id, 'piece', 899.99, 5, 10, '123456789001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        ('ELEC-MON-001', 'Samsung 24" LED Monitor', 'Full HD 1080p display', electronics_id, 'piece', 159.99, 10, 20, '123456789002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        ('ELEC-KEY-001', 'Logitech Wireless Keyboard', 'Bluetooth keyboard with number pad', electronics_id, 'piece', 49.99, 15, 30, '123456789003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        ('ELEC-MOU-001', 'Logitech MX Master Mouse', 'Wireless ergonomic mouse', electronics_id, 'piece', 79.99, 15, 25, '123456789004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

    -- Furniture items
    INSERT INTO inventory_items (sku, name, description, category_id, unit_of_measure, unit_price, reorder_level, reorder_quantity, barcode, created_at, updated_at)
    VALUES
        ('FURN-DSK-001', 'Office Desk - L-Shape', '60" x 48" L-shaped desk with cable management', furniture_id, 'piece', 349.99, 3, 5, '123456789011', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        ('FURN-CHR-001', 'Ergonomic Office Chair', 'Adjustable height with lumbar support', furniture_id, 'piece', 189.99, 5, 10, '123456789012', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        ('FURN-CAB-001', 'Filing Cabinet 3-Drawer', 'Metal filing cabinet with lock', furniture_id, 'piece', 129.99, 5, 8, '123456789013', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

    -- Office supplies
    INSERT INTO inventory_items (sku, name, description, category_id, unit_of_measure, unit_price, reorder_level, reorder_quantity, barcode, created_at, updated_at)
    VALUES
        ('OFFC-PEN-001', 'Ballpoint Pen Blue', 'Pack of 12 ballpoint pens', office_supplies_id, 'pack', 5.99, 50, 100, '123456789021', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP ),
        ('OFFC-NBK-001', 'A4 Spiral Notebook', '200 pages ruled notebook', office_supplies_id, 'piece', 3.99, 30, 50, '123456789022', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        ('OFFC-PRT-001', 'A4 Printing Paper', 'Ream of 500 sheets', office_supplies_id, 'ream', 8.99, 20, 50, '123456789023', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        ('OFFC-STP-001', 'Stapler Standard', 'Heavy duty stapler', office_supplies_id, 'piece', 12.99, 10, 20, '123456789024', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

    -- Clothing items
    INSERT INTO inventory_items (sku, name, description, category_id, unit_of_measure, unit_price, reorder_level, reorder_quantity, barcode, created_at, updated_at)
    VALUES
        ('CLTH-TSH-001', 'Company T-Shirt - Black', 'Cotton t-shirt with logo', clothing_id, 'piece', 19.99, 25, 50, '123456789031', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        ('CLTH-PLO-001', 'Polo Shirt - Navy', 'Professional polo shirt', clothing_id, 'piece', 29.99, 20, 40, '123456789032', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        ('CLTH-JAC-001', 'Fleece Jacket', 'Warm fleece jacket with zipper', clothing_id, 'piece', 49.99, 15, 30, '123456789033', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
END $$;

SET search_path TO public;
