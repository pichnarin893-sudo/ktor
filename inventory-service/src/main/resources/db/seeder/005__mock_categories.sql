-- Seed data for categories
-- Inventory service owns the entire inventory_db database - no schema prefix needed

-- Root categories
INSERT INTO categories (id, name, description, parent_category_id, is_active, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Electronics', 'Electronic devices and accessories', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'Furniture', 'Office and home furniture', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'Office Supplies', 'Stationery and office equipment', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('44444444-4444-4444-4444-444444444444', 'Clothing', 'Apparel and accessories', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sub-categories for Electronics
INSERT INTO categories (id, name, description, parent_category_id, is_active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Computers', 'Laptops, desktops, and accessories', '11111111-1111-1111-1111-111111111111', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Mobile Phones', 'Smartphones and tablets', '11111111-1111-1111-1111-111111111111', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Audio Equipment', 'Headphones, speakers, and microphones', '11111111-1111-1111-1111-111111111111', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sub-categories for Furniture
INSERT INTO categories (id, name, description, parent_category_id, is_active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Desks', 'Office and study desks', '22222222-2222-2222-2222-222222222222', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Chairs', 'Office and dining chairs', '22222222-2222-2222-2222-222222222222', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Storage', 'Cabinets and shelving units', '22222222-2222-2222-2222-222222222222', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
