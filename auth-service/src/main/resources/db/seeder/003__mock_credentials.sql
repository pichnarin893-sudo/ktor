SET search_path TO auth_schema;

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
        public.crypt('admin@123', public.gen_salt('bf')), 
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
        public.crypt('manager@123', public.gen_salt('bf')),
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
        public.crypt('staff@123', public.gen_salt('bf')),
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (user_id) DO NOTHING;

SET search_path TO public;
