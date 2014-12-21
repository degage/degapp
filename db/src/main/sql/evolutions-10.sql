-- Added information to the users table
ALTER TABLE users ADD COLUMN `user_degage_id` INT;
ALTER TABLE users ADD COLUMN `user_date_joined` DATE;
ALTER TABLE users ADD COLUMN `user_deposit` INT;
ALTER TABLE users ADD COLUMN `user_driver_license_date` DATE;

