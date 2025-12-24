-- Seed data for branches
SET search_path TO inventory_schema;

INSERT INTO branches (id, name, code, address, city, country, phone_number, email, manager_name, is_active, created_at)
VALUES
    (gen_random_uuid(), 'Main Warehouse', 'WH-MAIN', '123 Industrial Ave', 'Phnom Penh', 'Cambodia', '+855123456789', 'warehouse.main@natjoub.com', 'Sopheak Chan', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Downtown Store', 'ST-DT', '456 Monivong Blvd', 'Phnom Penh', 'Cambodia', '+855987654321', 'store.downtown@natjoub.com', 'Sreymom Keo', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'North Branch', 'BR-NORTH', '789 Russian Blvd', 'Phnom Penh', 'Cambodia', '+855111222333', 'branch.north@natjoub.com', 'Virak Phal', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Siem Reap Branch', 'BR-SR', '321 Airport Rd', 'Siem Reap', 'Cambodia', '+855444555666', 'branch.siemreap@natjoub.com', 'Dara Mom', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

SET search_path TO public;
