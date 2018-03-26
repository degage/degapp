-- See Issue 263
USE Degage;

-- Default was '0' previously instead of NULL.
ALTER TABLE users ALTER COLUMN user_fee SET DEFAULT NULL;

-- Update all old '0's to NULL
UPDATE users SET user_fee = NULL WHERE user_fee = 0;
