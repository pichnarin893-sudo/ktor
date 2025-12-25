-- Seed data for users
-- Auth service owns the entire auth_db database - no schema prefix needed

INSERT INTO users (
    id, first_name, last_name, dob, gender, role_id, is_active, created_at, updated_at
)
VALUES
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'System', 'Admin',
        '1990-01-01',
        'male',
        '11111111-1111-1111-1111-111111111111',
        true,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'John', 'Seller',
        '1995-05-10',
        'male',
        '22222222-2222-2222-2222-222222222222',
        true,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'Alice', 'Customer',
        '2005-03-15',
        'female',
        '33333333-3333-3333-3333-333333333333',
        true,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    )
ON CONFLICT (id) DO NOTHING;
