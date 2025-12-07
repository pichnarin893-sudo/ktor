-- Seed data for categories
SET search_path TO inventory_schema;

-- Root categories
INSERT INTO categories (id, name, description, parent_category_id, is_active)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Electronics', 'Electronic devices and accessories', NULL, true),
    ('22222222-2222-2222-2222-222222222222', 'Furniture', 'Office and home furniture', NULL, true),
    ('33333333-3333-3333-3333-333333333333', 'Office Supplies', 'Stationery and office equipment', NULL, true),
    ('44444444-4444-4444-4444-444444444444', 'Clothing', 'Apparel and accessories', NULL, true);

-- Sub-categories for Electronics
INSERT INTO categories (id, name, description, parent_category_id, is_active)
VALUES
    (gen_random_uuid(), 'Computers', 'Laptops, desktops, and accessories', '11111111-1111-1111-1111-111111111111', true),
    (gen_random_uuid(), 'Mobile Phones', 'Smartphones and tablets', '11111111-1111-1111-1111-111111111111', true),
    (gen_random_uuid(), 'Audio Equipment', 'Headphones, speakers, and microphones', '11111111-1111-1111-1111-111111111111', true);

-- Sub-categories for Furniture
INSERT INTO categories (id, name, description, parent_category_id, is_active)
VALUES
    (gen_random_uuid(), 'Desks', 'Office and study desks', '22222222-2222-2222-2222-222222222222', true),
    (gen_random_uuid(), 'Chairs', 'Office and dining chairs', '22222222-2222-2222-2222-222222222222', true),
    (gen_random_uuid(), 'Storage', 'Cabinets and shelving units', '22222222-2222-2222-2222-222222222222', true);

SET search_path TO public;
