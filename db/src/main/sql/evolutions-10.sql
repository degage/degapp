-- Added information to the users table
-- Changed verification process

ALTER TABLE users ADD COLUMN `user_degage_id` INT;
ALTER TABLE users ADD COLUMN `user_date_joined` DATE;
ALTER TABLE users ADD COLUMN `user_deposit` INT;
ALTER TABLE users ADD COLUMN `user_driver_license_date` DATE;

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
