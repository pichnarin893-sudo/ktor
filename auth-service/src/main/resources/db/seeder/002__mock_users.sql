-- Seed data for users
SET search_path TO auth_schema;

INSERT INTO users (
    id, first_name, last_name, dob, gender, role_id, is_active
)
VALUES
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'System', 'Admin',
        '1990-01-01',
        'male',
        '11111111-1111-1111-1111-111111111111',
        true
    ),
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'John', 'Seller',
        '1995-05-10',
        'male',
        '22222222-2222-2222-2222-222222222222',
        true
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'Alice', 'Customer',
        '2005-03-15',
        'female',
        '33333333-3333-3333-3333-333333333333',
        true
    )
ON CONFLICT (id) DO NOTHING;

SET search_path TO public;
