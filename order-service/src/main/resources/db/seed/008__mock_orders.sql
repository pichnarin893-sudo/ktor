-- Demo seed data for Order Service
-- References customer IDs from auth-service and product IDs from inventory-service

-- Order 1: Alice Cooper - Electronics order (DELIVERED)
INSERT INTO orders (id, customer_id, total_amount, status, delivery_address, notes, created_at, updated_at) VALUES
    ('a50e8400-e29b-41d4-a716-446655440001',
     '650e8400-e29b-41d4-a716-446655440011', -- Alice Cooper
     1649.97,
     'DELIVERED',
     '123 Maple Street, Apt 4B, New York, NY 10001, USA',
     'Please call before delivery',
     CURRENT_TIMESTAMP - INTERVAL '25 days',
     CURRENT_TIMESTAMP - INTERVAL '18 days')
ON CONFLICT (id) DO NOTHING;

-- Order 1 items
INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, subtotal, created_at) VALUES
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440001',
     '950e8400-e29b-41d4-a716-446655440001', -- Dell XPS 13 Laptop
     'Dell XPS 13 Laptop',
     1,
     1299.99,
     1299.99,
     CURRENT_TIMESTAMP - INTERVAL '25 days'),
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440001',
     '950e8400-e29b-41d4-a716-446655440002', -- Logitech Mouse
     'Logitech MX Master 3 Mouse',
     2,
     99.99,
     199.98,
     CURRENT_TIMESTAMP - INTERVAL '25 days'),
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440001',
     '950e8400-e29b-41d4-a716-446655440003', -- Corsair Keyboard
     'Corsair K95 Mechanical Keyboard',
     1,
     149.99,
     149.99,
     CURRENT_TIMESTAMP - INTERVAL '25 days')
ON CONFLICT (id) DO NOTHING;

-- Order 2: Bob Smith - Office supplies (SHIPPED)
INSERT INTO orders (id, customer_id, total_amount, status, delivery_address, notes, created_at, updated_at) VALUES
    ('a50e8400-e29b-41d4-a716-446655440002',
     '650e8400-e29b-41d4-a716-446655440012', -- Bob Smith
     365.90,
     'SHIPPED',
     '456 Oak Avenue, Suite 200, Los Angeles, CA 90001, USA',
     'Business address - office hours delivery only',
     CURRENT_TIMESTAMP - INTERVAL '10 days',
     CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- Order 2 items
INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, subtotal, created_at) VALUES
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440002',
     '950e8400-e29b-41d4-a716-446655440005', -- A4 Paper
     'A4 Copy Paper',
     5,
     49.99,
     249.95,
     CURRENT_TIMESTAMP - INTERVAL '10 days'),
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440002',
     '950e8400-e29b-41d4-a716-446655440006', -- Pilot Pens
     'Pilot G2 Gel Pen',
     3,
     15.99,
     47.97,
     CURRENT_TIMESTAMP - INTERVAL '10 days'),
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440002',
     '950e8400-e29b-41d4-a716-446655440007', -- Moleskine Notebook
     'Moleskine A5 Notebook',
     4,
     16.99,
     67.96,
     CURRENT_TIMESTAMP - INTERVAL '10 days')
ON CONFLICT (id) DO NOTHING;

-- Order 3: Carol Davis - Tools and safety equipment (PROCESSING)
INSERT INTO orders (id, customer_id, total_amount, status, delivery_address, notes, created_at, updated_at) VALUES
    ('a50e8400-e29b-41d4-a716-446655440003',
     '650e8400-e29b-41d4-a716-446655440013', -- Carol Davis
     557.91,
     'PROCESSING',
     '789 Pine Road, Chicago, IL 60601, USA',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '5 days',
     CURRENT_TIMESTAMP - INTERVAL '1 days')
ON CONFLICT (id) DO NOTHING;

-- Order 3 items
INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, subtotal, created_at) VALUES
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440003',
     '950e8400-e29b-41d4-a716-446655440011', -- DeWalt Drill
     'DeWalt 20V Cordless Drill',
     2,
     179.99,
     359.98,
     CURRENT_TIMESTAMP - INTERVAL '5 days'),
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440003',
     '950e8400-e29b-41d4-a716-446655440013', -- Hard Hat
     'White Hard Hat',
     5,
     24.99,
     124.95,
     CURRENT_TIMESTAMP - INTERVAL '5 days'),
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440003',
     '950e8400-e29b-41d4-a716-446655440014', -- Safety Gloves
     'Safety Gloves Size L',
     3,
     12.99,
     38.97,
     CURRENT_TIMESTAMP - INTERVAL '5 days'),
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440003',
     '950e8400-e29b-41d4-a716-446655440015', -- Safety Goggles
     'Safety Goggles',
     4,
     8.49,
     33.96,
     CURRENT_TIMESTAMP - INTERVAL '5 days')
ON CONFLICT (id) DO NOTHING;

-- Order 4: Alice Cooper - Monitor order (PENDING)
INSERT INTO orders (id, customer_id, total_amount, status, delivery_address, notes, created_at, updated_at) VALUES
    ('a50e8400-e29b-41d4-a716-446655440004',
     '650e8400-e29b-41d4-a716-446655440011', -- Alice Cooper
     1099.98,
     'PENDING',
     '123 Maple Street, Apt 4B, New York, NY 10001, USA',
     'Same address as previous order',
     CURRENT_TIMESTAMP - INTERVAL '2 days',
     CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- Order 4 items
INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, subtotal, created_at) VALUES
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440004',
     '950e8400-e29b-41d4-a716-446655440004', -- Dell Monitor
     'Dell 27" 4K Monitor',
     2,
     549.99,
     1099.98,
     CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- Order 5: Bob Smith - Raw materials order (PENDING)
INSERT INTO orders (id, customer_id, total_amount, status, delivery_address, notes, created_at, updated_at) VALUES
    ('a50e8400-e29b-41d4-a716-446655440005',
     '650e8400-e29b-41d4-a716-446655440012', -- Bob Smith
     589.89,
     'PENDING',
     '456 Oak Avenue, Suite 200, Los Angeles, CA 90001, USA',
     'Loading dock access required',
     CURRENT_TIMESTAMP - INTERVAL '1 days',
     CURRENT_TIMESTAMP - INTERVAL '1 days')
ON CONFLICT (id) DO NOTHING;

-- Order 5 items
INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, subtotal, created_at) VALUES
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440005',
     '950e8400-e29b-41d4-a716-446655440008', -- Steel Sheet
     'Steel Sheet 4x8ft',
     5,
     89.99,
     449.95,
     CURRENT_TIMESTAMP - INTERVAL '1 days'),
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440005',
     '950e8400-e29b-41d4-a716-446655440009', -- Aluminum Rod
     'Aluminum Rod 1" Diameter',
     3,
     45.99,
     137.97,
     CURRENT_TIMESTAMP - INTERVAL '1 days'),
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440005',
     '950e8400-e29b-41d4-a716-446655440010', -- PVC Pipe
     'PVC Pipe 2" Diameter',
     10,
     1.99,
     19.90,
     CURRENT_TIMESTAMP - INTERVAL '1 days')
ON CONFLICT (id) DO NOTHING;

-- Order 6: Carol Davis - Cancelled order
INSERT INTO orders (id, customer_id, total_amount, status, delivery_address, notes, created_at, updated_at) VALUES
    ('a50e8400-e29b-41d4-a716-446655440006',
     '650e8400-e29b-41d4-a716-446655440013', -- Carol Davis
     129.99,
     'CANCELLED',
     '789 Pine Road, Chicago, IL 60601, USA',
     'Customer requested cancellation - wrong item ordered',
     CURRENT_TIMESTAMP - INTERVAL '8 days',
     CURRENT_TIMESTAMP - INTERVAL '7 days')
ON CONFLICT (id) DO NOTHING;

-- Order 6 items
INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, subtotal, created_at) VALUES
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440006',
     '950e8400-e29b-41d4-a716-446655440012', -- Circular Saw
     'Circular Saw 7.25"',
     1,
     129.99,
     129.99,
     CURRENT_TIMESTAMP - INTERVAL '8 days')
ON CONFLICT (id) DO NOTHING;

-- Order 7: Large corporate order (PROCESSING)
INSERT INTO orders (id, customer_id, total_amount, status, delivery_address, notes, created_at, updated_at) VALUES
    ('a50e8400-e29b-41d4-a716-446655440007',
     '650e8400-e29b-41d4-a716-446655440011', -- Alice Cooper
     4299.85,
     'PROCESSING',
     '123 Maple Street, Apt 4B, New York, NY 10001, USA',
     'Large corporate order - priority shipping requested',
     CURRENT_TIMESTAMP - INTERVAL '3 days',
     CURRENT_TIMESTAMP - INTERVAL '1 days')
ON CONFLICT (id) DO NOTHING;

-- Order 7 items
INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, subtotal, created_at) VALUES
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440007',
     '950e8400-e29b-41d4-a716-446655440001', -- Laptops
     'Dell XPS 13 Laptop',
     3,
     1299.99,
     3899.97,
     CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440007',
     '950e8400-e29b-41d4-a716-446655440002', -- Mice
     'Logitech MX Master 3 Mouse',
     3,
     99.99,
     299.97,
     CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (gen_random_uuid(),
     'a50e8400-e29b-41d4-a716-446655440007',
     '950e8400-e29b-41d4-a716-446655440007', -- Notebooks
     'Moleskine A5 Notebook',
     5,
     19.99,
     99.95,
     CURRENT_TIMESTAMP - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;
