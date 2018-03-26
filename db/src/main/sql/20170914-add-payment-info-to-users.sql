ALTER TABLE users
ADD column user_payment_info text NULL DEFAULT NULL;
ALTER TABLE users
ADD column `user_credit_status` enum('REGULAR','PAYMENT_PLAN') NOT NULL DEFAULT 'REGULAR';
