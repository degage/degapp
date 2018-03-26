ALTER TABLE payments
ADD column `payment_include_in_balance` bit(1) NOT NULL DEFAULT b'1';
