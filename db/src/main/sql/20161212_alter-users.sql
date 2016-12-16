ALTER TABLE users
ADD COLUMN user_date_blocked timestamp NULL DEFAULT NULL,
ADD COLUMN user_date_dropped timestamp NULL DEFAULT NULL,
ADD COLUMN user_reason_blocked VARCHAR(255),
ADD COLUMN user_reason_dropped VARCHAR(255);
