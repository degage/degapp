-- Added information to the users table
-- Changed verification process

ALTER TABLE users ADD COLUMN `user_degage_id` INT;
ALTER TABLE users ADD COLUMN `user_date_joined` DATE;
ALTER TABLE users ADD COLUMN `user_deposit` INT;
ALTER TABLE users ADD COLUMN `user_driver_license_date` DATE;

ALTER TABLE users DROP COLUMN user_payed_deposit;

UPDATE users SET user_date_joined = '2001-01-01', user_degage_id = 14000+user_id WHERE user_status = 'FULL';

UPDATE users SET user_address_residence_id = 15, user_address_domicile_id = 16 where user_id = 4;

DELETE FROM verifications;

DROP TABLE verifications;

CREATE TABLE `verifications` (
	`verification_ident` CHAR(37) NOT NULL,
	`verification_email` VARCHAR(64) NOT NULL,
	`verification_type` ENUM('REGISTRATION','PWRESET') NOT NULL DEFAULT 'REGISTRATION',
	`verification_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`verification_ident`)
);

delete from users where user_status='EMAIL_VALIDATING';

ALTER TABLE users
  MODIFY COLUMN user_status ENUM('REGISTERED','FULL_VALIDATING','FULL','BLOCKED','DROPPED','INACTIVE')
  NOT NULL DEFAULT 'REGISTERED';

UPDATE templates
  SET template_body =
  "Beste toekomstige gebruiker,<br>
  <br>
  Klik op onderstaande link om verder te gaan met de registratie:<br>
  %verification_url% <br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage"
WHERE template_id = 1;

DELETE from templatetagassociations where template_id = 1 and template_tag_id < 3;

DROP VIEW infosessions_extended;

CREATE VIEW `infosessions_extended` AS
    SELECT
        infosession_id, infosession_type,
        infosession_timestamp, infosession_max_enrollees, infosession_comments,
        address_id, address_country, address_city, address_zipcode, address_street, address_number,
        user_id, user_firstname, user_lastname, user_phone, user_email, user_status, user_cellphone, user_degage_id,
        count(infosession_enrollee_id) AS enrollee_count
    FROM infosessions AS ses
        JOIN users ON infosession_host_user_id = user_id
        JOIN addresses ON infosession_address_id = address_id
        LEFT JOIN infosessionenrollees USING (infosession_id)
    GROUP BY infosession_id;
