-- Seed data for stock levels
-- Inventory service owns the entire inventory_db database - no schema prefix needed

-- Add initial stock levels for various items across branches
-- This assumes branches and inventory items have been seeded

DO $$
DECLARE
    main_warehouse_id UUID;
    downtown_store_id UUID;
    north_branch_id UUID;
BEGIN
    -- Get branch IDs
    SELECT id INTO main_warehouse_id FROM branches WHERE code = 'WH-MAIN' LIMIT 1;
    SELECT id INTO downtown_store_id FROM branches WHERE code = 'ST-DT' LIMIT 1;
    SELECT id INTO north_branch_id FROM branches WHERE code = 'BR-NORTH' LIMIT 1;

    -- Stock for Main Warehouse (higher quantities)
    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, main_warehouse_id, 100, 0, NOW()
    FROM inventory_items WHERE sku = 'ELEC-LAP-001';

    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, main_warehouse_id, 150, 5, NOW()
    FROM inventory_items WHERE sku = 'ELEC-MON-001';

    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, main_warehouse_id, 200, 10, NOW()
    FROM inventory_items WHERE sku = 'ELEC-KEY-001';

    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, main_warehouse_id, 80, 0, NOW()
    FROM inventory_items WHERE sku = 'FURN-DSK-001';

    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, main_warehouse_id, 500, 50, NOW()
    FROM inventory_items WHERE sku = 'OFFC-PEN-001';

    -- Stock for Downtown Store (medium quantities)
    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, downtown_store_id, 25, 2, NOW()
    FROM inventory_items WHERE sku = 'ELEC-LAP-001';

    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, downtown_store_id, 40, 3, NOW()
    FROM inventory_items WHERE sku = 'ELEC-MON-001';

    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, downtown_store_id, 15, 0, NOW()
    FROM inventory_items WHERE sku = 'FURN-CHR-001';

    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, downtown_store_id, 100, 5, NOW()
    FROM inventory_items WHERE sku = 'CLTH-TSH-001';

    -- Stock for North Branch (lower quantities)
    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, north_branch_id, 10, 1, NOW()
    FROM inventory_items WHERE sku = 'ELEC-LAP-001';

    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, north_branch_id, 20, 2, NOW()
    FROM inventory_items WHERE sku = 'ELEC-KEY-001';

    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, north_branch_id, 50, 5, NOW()
    FROM inventory_items WHERE sku = 'OFFC-NBK-001';

    INSERT INTO stock_levels (inventory_item_id, branch_id, quantity, reserved_quantity, last_counted_at)
    SELECT id, north_branch_id, 30, 0, NOW()
    FROM inventory_items WHERE sku = 'CLTH-PLO-001';
END $$;
