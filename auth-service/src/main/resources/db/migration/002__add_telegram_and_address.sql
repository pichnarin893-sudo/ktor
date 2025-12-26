-- Add telegram_id to credentials table for Telegram bot integration
ALTER TABLE credentials
ADD COLUMN telegram_id BIGINT UNIQUE;

-- Create index for telegram_id lookup
CREATE INDEX IF NOT EXISTS idx_credentials_telegram_id ON credentials(telegram_id);

-- Add delivery address fields to users table (for customer orders)
ALTER TABLE users
ADD COLUMN address_line1 VARCHAR(255),
ADD COLUMN address_line2 VARCHAR(255),
ADD COLUMN city VARCHAR(100),
ADD COLUMN state VARCHAR(100),
ADD COLUMN postal_code VARCHAR(20),
ADD COLUMN country VARCHAR(100);

-- Create index for postal code searches
CREATE INDEX IF NOT EXISTS idx_users_postal_code ON users(postal_code);
