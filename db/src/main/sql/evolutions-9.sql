-- Simplified address handling
ALTER TABLE addresses MODIFY COLUMN `address_city` VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE addresses MODIFY COLUMN `address_zipcode` VARCHAR(12) NOT NULL DEFAULT '';

ALTER TABLE addresses DROP COLUMN address_created_at; -- always created together with 'parent' record

DELIMITER $$

CREATE TRIGGER users_make BEFORE INSERT ON users FOR EACH ROW
BEGIN
   INSERT INTO addresses VALUES ();
   SET NEW.user_address_domicile_id = last_insert_id();
   INSERT INTO addresses VALUES ();
   SET NEW.user_address_residence_id = last_insert_id();
END $$

DELIMITER ;
