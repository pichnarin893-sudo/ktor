-- Demo seed data for Auth Service
-- Password for all users: "password123" (bcrypt hashed)
-- Use this for demo/testing purposes only!

-- Insert roles
INSERT INTO roles (id, name, created_at, updated_at) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'employee', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440002', 'customer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Insert users
-- EMPLOYEE users
INSERT INTO users (id, first_name, last_name, dob, gender, role_id, is_active, created_at, updated_at) VALUES
    ('650e8400-e29b-41d4-a716-446655440001', 'John', 'Anderson', '1985-03-15', 'male', '550e8400-e29b-41d4-a716-446655440001', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440002', 'Sarah', 'Williams', '1988-07-22', 'female', '550e8400-e29b-41d4-a716-446655440001', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440003', 'Michael', 'Chen', '1990-11-08', 'male', '550e8400-e29b-41d4-a716-446655440001', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440004', 'Emma', 'Johnson', '1992-05-14', 'female', '550e8400-e29b-41d4-a716-446655440001', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440005', 'David', 'Martinez', '1987-09-30', 'male', '550e8400-e29b-41d4-a716-446655440001', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Customer users (for orders)
INSERT INTO users (id, first_name, last_name, dob, gender, role_id, is_active, created_at, updated_at) VALUES
    ('650e8400-e29b-41d4-a716-446655440011', 'Alice', 'Cooper', '1989-06-15', 'female', '550e8400-e29b-41d4-a716-446655440002', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440012', 'Bob', 'Smith', '1991-03-22', 'male', '550e8400-e29b-41d4-a716-446655440002', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440013', 'Carol', 'Davis', '1993-11-08', 'female', '550e8400-e29b-41d4-a716-446655440002', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440014', 'Tom', 'Harris', '1995-04-20', 'male', '550e8400-e29b-41d4-a716-446655440002', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Insert credentials
-- Password: "Password123@" hashed with BCrypt (work factor 12)
-- Hash: $2a$12$jfTTUrmQKhMd1tfLowuiJehO557hbAJqIJmbq0B5LXxOPnY.dxYMe
INSERT INTO credentials (id, user_id, email, username, phone_number, password, is_verified, created_at, updated_at) VALUES
    -- Employees
    (gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440001', 'john.anderson@factory.com', 'john.employee', '+1234567001', '$2a$12$jfTTUrmQKhMd1tfLowuiJehO557hbAJqIJmbq0B5LXxOPnY.dxYMe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440002', 'sarah.williams@factory.com', 'sarah.employee', '+1234567002', '$2a$12$jfTTUrmQKhMd1tfLowuiJehO557hbAJqIJmbq0B5LXxOPnY.dxYMe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440003', 'michael.chen@factory.com', 'michael.employee', '+1234567003', '$2a$12$jfTTUrmQKhMd1tfLowuiJehO557hbAJqIJmbq0B5LXxOPnY.dxYMe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440004', 'emma.johnson@factory.com', 'emma.employee', '+1234567004', '$2a$12$jfTTUrmQKhMd1tfLowuiJehO557hbAJqIJmbq0B5LXxOPnY.dxYMe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440005', 'david.martinez@factory.com', 'david.employee', '+1234567005', '$2a$12$jfTTUrmQKhMd1tfLowuiJehO557hbAJqIJmbq0B5LXxOPnY.dxYMe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Customers
    (gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440011', 'alice.cooper@customer.com', 'alice.customer', '+1234567011', '$2a$12$jfTTUrmQKhMd1tfLowuiJehO557hbAJqIJmbq0B5LXxOPnY.dxYMe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440012', 'bob.smith@customer.com', 'bob.customer', '+1234567012', '$2a$12$jfTTUrmQKhMd1tfLowuiJehO557hbAJqIJmbq0B5LXxOPnY.dxYMe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440013', 'carol.davis@customer.com', 'carol.customer', '+1234567013', '$2a$12$jfTTUrmQKhMd1tfLowuiJehO557hbAJqIJmbq0B5LXxOPnY.dxYMe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440014', 'tom.harris@customer.com', 'tom.customer', '+1234567014', '$2a$12$jfTTUrmQKhMd1tfLowuiJehO557hbAJqIJmbq0B5LXxOPnY.dxYMe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;
