-- Seed data for credentials
-- Auth service owns the entire auth_db database - no schema prefix needed

INSERT INTO credentials (
    id, user_id, email, username, phone_number, password, is_verified, created_at, updated_at
)
VALUES
    (
        '11111111-1111-1111-1111-111111111111',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'pichnarin893@gmail.com',
        'admin',
        '0975812400',
        crypt('admin@123', gen_salt('bf')),
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '22222222-2222-2222-2222-222222222222',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'tleanghour67@gmail.com',
        'manager01',
        '017963338',
        crypt('manager@123', gen_salt('bf')),
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '33333333-3333-3333-3333-333333333333',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'moonlightfriday61@gmail.com',
        'staff01',
        '0123478321',
        crypt('staff@123', gen_salt('bf')),
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (user_id) DO NOTHING;
