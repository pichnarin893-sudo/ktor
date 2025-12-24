SET search_path TO auth_schema;

INSERT INTO credentials (
    id, user_id, email, username, phone_number, password, is_verified, created_at, updated_at
)
VALUES
    (
                '11111111-1111-1111-1111-111111111111',  -- user_id

        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',  -- id
        'admin@example.com',                      -- email
        'admin',                                  -- username
        '0123456789',                             -- phone_number
        public.crypt('admin123', public.gen_salt('bf')), -- password
        true,                                     -- is_verified
        CURRENT_TIMESTAMP,                        -- created_at
        CURRENT_TIMESTAMP                         -- updated_at
    ),
    (
                '22222222-2222-2222-2222-222222222222',

        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'seller@example.com',
        'seller1',
        '0987654321',
        public.crypt('seller123', public.gen_salt('bf')),
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
                '33333333-3333-3333-3333-333333333333',

        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'customer1@example.com',
        'customer1',
        '0112233445',
        public.crypt('customer123', public.gen_salt('bf')),
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (user_id) DO NOTHING;

SET search_path TO public;
