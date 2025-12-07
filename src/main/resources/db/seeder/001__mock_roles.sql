-- Seed data for roles
SET search_path TO auth_schema;

INSERT INTO roles (id, name)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'admin'),
    ('22222222-2222-2222-2222-222222222222', 'seller'),
    ('33333333-3333-3333-3333-333333333333', 'customer')
ON CONFLICT (id) DO NOTHING;

SET search_path TO public;
