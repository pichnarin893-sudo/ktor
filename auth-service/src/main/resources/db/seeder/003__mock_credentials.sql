-- Seed data for credentials
SET search_path TO auth_schema;

INSERT INTO credentials (
    user_id, email, username, phone_number, password, is_verified
)
VALUES
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'admin@example.com',
        'admin',
        '0123456789',
        public.crypt('admin123', public.gen_salt('bf')),
        true
    ),
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'seller@example.com',
        'seller1',
        '0987654321',
        public.crypt('seller123', public.gen_salt('bf')),
        true
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'customer1@example.com',
        'customer1',
        '0112233445',
        public.crypt('customer123', public.gen_salt('bf')),
        true
    )
ON CONFLICT (user_id) DO NOTHING;

SET search_path TO public;
