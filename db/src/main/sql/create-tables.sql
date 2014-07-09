-- Creates the database tables

SET names utf8;
SET default_storage_engine=INNODB;

-- TODO: split into separate files

-- TABLES
-- ~~~~~~

CREATE TABLE `filegroups` (
	`file_group_id` INT NOT NULL AUTO_INCREMENT,
	PRIMARY KEY (`file_group_id`)
);

CREATE TABLE `files` (
  `file_id` INT NOT NULL AUTO_INCREMENT,
  `file_path` VARCHAR(255) NOT NULL,
  `file_name` VARCHAR(128) NULL,
  `file_content_type` VARCHAR(64) NULL,
  `file_file_group_id` INT(11) NULL DEFAULT NULL,
  `file_created_at` DATETIME,
  `file_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`file_id`),
	FOREIGN KEY (`file_file_group_id`) REFERENCES filegroups(`file_group_id`)
);


CREATE TABLE `addresses` (
  `address_id` INT NOT NULL AUTO_INCREMENT,
  `address_country` VARCHAR(64) NOT NULL DEFAULT 'Belgium',
  `address_city` VARCHAR(64) NOT NULL,
  `address_zipcode` VARCHAR(12) NOT NULL,
  `address_street` VARCHAR(64) NOT NULL DEFAULT '',
  `address_street_number` VARCHAR(8) NOT NULL DEFAULT '',
  `address_street_bus` VARCHAR(4),
  `address_created_at` DATETIME,
  `address_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`address_id`),
  INDEX `address_place_zip` (`address_city`)
);

CREATE TABLE `users` (
	`user_id` INT NOT NULL AUTO_INCREMENT,
	`user_email` VARCHAR(64) NOT NULL,
	`user_password` CHAR(64) NOT NULL,
	`user_firstname` VARCHAR(64) NOT NULL,
	`user_lastname` VARCHAR(64) NOT NULL,
	`user_gender` ENUM('MALE', 'FEMALE', 'UNKNOWN') NOT NULL DEFAULT 'UNKNOWN',
	`user_cellphone` VARCHAR(16),
	`user_phone` VARCHAR(16),
	`user_address_domicile_id` INT,
	`user_address_residence_id` INT,
	`user_driver_license_id` VARCHAR(32),
	`user_driver_license_file_group_id` INT,
	`user_identity_card_id` VARCHAR(32), # Identiteitskaartnr
	`user_identity_card_registration_nr` VARCHAR(32), # Rijksregisternummer
	`user_identity_card_file_group_id` INT,
	`user_status` ENUM('EMAIL_VALIDATING','REGISTERED','FULL_VALIDATING','FULL','BLOCKED','DROPPED','INACTIVE') NOT NULL DEFAULT 'EMAIL_VALIDATING', # Stadia die de gebruiker moet doorlopen
	`user_damage_history` TEXT,
	`user_payed_deposit` BIT(1),
	`user_agree_terms` BIT(1),
	`user_image_id` INT,
	`user_created_at` DATETIME,
	`user_last_notified` DATETIME,
	`user_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`user_id`),
	FOREIGN KEY (`user_address_domicile_id`) REFERENCES addresses(`address_id`),
	FOREIGN KEY (`user_address_residence_id`) REFERENCES addresses(`address_id`),
	FOREIGN KEY (`user_driver_license_file_group_id`) REFERENCES filegroups(`file_group_id`),
	FOREIGN KEY (`user_identity_card_file_group_id`) REFERENCES filegroups(`file_group_id`),
	FOREIGN KEY (`user_image_id`) REFERENCES files(`file_id`),
	UNIQUE INDEX `user_email` (`user_email`)
);

CREATE TABLE `userroles` (
	`userrole_userid` INT NOT NULL,
	`userrole_role` ENUM('SUPER_USER', 'CAR_OWNER', 'CAR_USER', 'INFOSESSION_ADMIN', 'MAIL_ADMIN', 'PROFILE_ADMIN', 'RESERVATION_ADMIN', 'CAR_ADMIN') NOT NULL,
	PRIMARY KEY (`userrole_userid`, `userrole_role`),
	FOREIGN KEY (`userrole_userid`) REFERENCES users(`user_id`)
);

CREATE TABLE `carinsurances` (
	`insurance_id` INT NOT NULL AUTO_INCREMENT,
	`insurance_name` VARCHAR(64),
	`insurance_expiration` DATE,
	`insurance_contract_id` INT, # Polisnr
	`insurance_bonus_malus` INT,
	`insurance_created_at` DATETIME,
	`insurance_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`insurance_id`)
);

CREATE TABLE `technicalcardetails` (
	`details_id` INT NOT NULL AUTO_INCREMENT,
	`details_car_license_plate` VARCHAR(64),
	`details_car_registration` INT,
	`details_car_chassis_number` VARCHAR(17),
	`details_created_at` DATETIME,
	`details_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`details_id`),
	UNIQUE INDEX `ix_details` (`details_car_license_plate`, `details_car_chassis_number`),
	FOREIGN KEY (`details_car_registration`) REFERENCES files(`file_id`)
);


CREATE TABLE `cars` (
	`car_id` INT NOT NULL AUTO_INCREMENT,
	`car_name` VARCHAR(64) NOT NULL,
	`car_type` VARCHAR(64) NOT NULL DEFAULT '0',
	`car_brand` VARCHAR(64) NOT NULL DEFAULT '0',
	`car_location` INT,
	`car_seats` TINYINT UNSIGNED,
	`car_doors` TINYINT UNSIGNED,
	`car_year` INT,
	`car_manual` BIT(1) NOT NULL DEFAULT 0,
	`car_gps` BIT(1) NOT NULL DEFAULT 0,
	`car_hook` BIT(1) NOT NULL DEFAULT 0,
	`car_fuel` ENUM('PETROL','DIESEL', 'BIODIESEL', 'GAS', 'HYBRID', 'ELECTRIC'),
	`car_fuel_economy` INT,
	`car_estimated_value` INT,
	`car_owner_annual_km` INT,
	`car_technical_details` INT,
	`car_insurance` INT,
	`car_owner_user_id` INT NOT NULL,
	`car_comments` VARCHAR(256),
	`car_active` BIT(1) NOT NULL DEFAULT 0,
	`car_images_id` INT,
	`car_created_at` DATETIME,
	`car_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`car_id`),
	FOREIGN KEY (`car_owner_user_id`) REFERENCES users(`user_id`) ON DELETE CASCADE,
	FOREIGN KEY (`car_location`) REFERENCES addresses(`address_id`) ON DELETE CASCADE,
	FOREIGN KEY (`car_images_id`) REFERENCES files(`file_id`),
	FOREIGN KEY (`car_technical_details`) REFERENCES technicalcardetails(`details_id`),
	FOREIGN KEY (`car_insurance`) REFERENCES carinsurances(`insurance_id`)
);

CREATE TABLE `carreservations` (
	`reservation_id` INT NOT NULL AUTO_INCREMENT,
	`reservation_status` ENUM('REQUEST','ACCEPTED', 'REFUSED', 'CANCELLED', 'REQUEST_DETAILS', 'DETAILS_PROVIDED', 'FINISHED') NOT NULL DEFAULT 'REQUEST', # Reeds goedgekeurd?
	`reservation_car_id` INT NOT NULL,
	`reservation_user_id` INT NOT NULL,
	`reservation_from` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
	`reservation_to` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
	`reservation_message` VARCHAR(128),
	`reservation_created_at` DATETIME,
	`reservation_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`reservation_id`),
	FOREIGN KEY (`reservation_car_id`) REFERENCES cars(`car_id`), # Wat moet er gebeuren als de auto verwijderd wordt?
	FOREIGN KEY (`reservation_user_id`) REFERENCES users(`user_id`) ON DELETE CASCADE
);

CREATE TABLE `infosessions` (
	`infosession_id` INT NOT NULL AUTO_INCREMENT,
	`infosession_type` ENUM('NORMAL', 'OWNER', 'OTHER') NOT NULL DEFAULT 'NORMAL',
	`infosession_type_alternative` VARCHAR(64),
	`infosession_timestamp` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
	`infosession_address_id` INT NOT NULL,
	`infosession_host_user_id` INT,
	`infosession_max_enrollees` INT,
	`infosession_comments` VARCHAR(256),
	`infosession_created_at` DATETIME,
	`infosession_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`infosession_id`),
	FOREIGN KEY (`infosession_host_user_id`) REFERENCES users(`user_id`),
	FOREIGN KEY (`infosession_address_id`) REFERENCES addresses(`address_id`)
);

CREATE TABLE `infosessionenrollees` ( # Wie is ingeschreven?
	`infosession_id` INT NOT NULL,
	`infosession_enrollee_id` INT NOT NULL,
	`infosession_enrollment_status` ENUM('ENROLLED', 'PRESENT', 'ABSENT') NOT NULL DEFAULT 'ENROLLED',
	PRIMARY KEY (`infosession_id`, `infosession_enrollee_id`),
	FOREIGN KEY (`infosession_enrollee_id`) REFERENCES users(`user_id`),
	FOREIGN KEY (`infosession_id`) REFERENCES infosessions(`infosession_id`) ON DELETE CASCADE
);

CREATE TABLE `caravailabilities` (
	`car_availability_id` INT NOT NULL AUTO_INCREMENT,
	`car_availability_car_id` INT NOT NULL,
	`car_availability_begin_day_of_week` INT NOT NULL,
	`car_availability_begin_time` TIME NOT NULL,
	`car_availability_end_day_of_week` INT NOT NULL,
	`car_availability_end_time` TIME NOT NULL,
	PRIMARY KEY (`car_availability_id`),
	FOREIGN KEY (`car_availability_car_id`) REFERENCES cars(`car_id`)
);

CREATE TABLE `carprivileges` (
	`car_privilege_user_id` INT NOT NULL,
	`car_privilege_car_id` INT NOT NULL,
	PRIMARY KEY (`car_privilege_user_id`,`car_privilege_car_id`),
	FOREIGN KEY (`car_privilege_user_id`) REFERENCES users(`user_id`),
	FOREIGN KEY (`car_privilege_car_id`) REFERENCES cars(`car_id`)
);

CREATE TABLE `carcosts` (
	`car_cost_id` INT NOT NULL AUTO_INCREMENT,
	`car_cost_car_id` INT NOT NULL,
	`car_cost_proof` INT,
	`car_cost_amount` DECIMAL(19,4) NOT NULL,
	`car_cost_description` TEXT,
	`car_cost_status` ENUM('REQUEST','ACCEPTED', 'REFUSED') NOT NULL DEFAULT 'REQUEST', #approved by car_admin
	`car_cost_time` DATETIME,
	`car_cost_mileage` DECIMAL(10,1),
	`car_cost_billed` DATE DEFAULT NULL,
	`car_cost_created_at` DATETIME,
	`car_cost_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`car_cost_id`),
	FOREIGN KEY (`car_cost_car_id`) REFERENCES cars(`car_id`),
	FOREIGN KEY (`car_cost_proof`) REFERENCES files(`file_id`)
);

CREATE TABLE `carrides` (
  `car_ride_car_reservation_id` INT NOT NULL, # also primary key
  `car_ride_status` BIT(1) NOT NULL DEFAULT 0, # approved by owner?
  `car_ride_start_mileage` DECIMAL(10,1),
  `car_ride_end_mileage` DECIMAL(10,1),
  `car_ride_damage` BIT(1) NOT NULL DEFAULT 0,
  `car_ride_refueling` INT NOT NULL,
  `car_ride_cost` DECIMAL(19,4) DEFAULT NULL,
  `car_ride_billed` DATE DEFAULT NULL,
  `car_ride_created_at` DATETIME,
  `car_ride_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`car_ride_car_reservation_id`),
  FOREIGN KEY (`car_ride_car_reservation_id`) REFERENCES carreservations(`reservation_id`)
);

CREATE TABLE `refuels` (
	`refuel_id` INT NOT NULL AUTO_INCREMENT,
	`refuel_car_ride_id` INT NOT NULL,
	`refuel_file_id` INT,
	`refuel_amount` DECIMAL(19,4),
	`refuel_status` ENUM('CREATED', 'REQUEST','ACCEPTED', 'REFUSED') NOT NULL DEFAULT 'CREATED', #approved by owner
	`refuel_billed` DATE DEFAULT NULL,
   	`refuel_created_at` DATETIME,
   	`refuel_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`refuel_id`),
	FOREIGN KEY (`refuel_car_ride_id`) REFERENCES carrides(`car_ride_car_reservation_id`),
	FOREIGN KEY (`refuel_file_id`) REFERENCES files(`file_id`)
);

CREATE TABLE `damages` (
	`damage_id` INT NOT NULL AUTO_INCREMENT,
	`damage_car_ride_id` INT NOT NULL,
	`damage_filegroup_id` INT,
	`damage_description` TEXT,
	`damage_finished` BIT(1) NOT NULL DEFAULT 0,
	`damage_time` DATETIME NOT NULL,
   	`damage_created_at` DATETIME,
   	`damage_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`damage_id`),
	FOREIGN KEY (`damage_car_ride_id`) REFERENCES carrides(`car_ride_car_reservation_id`),
	FOREIGN KEY (`damage_filegroup_id`) REFERENCES filegroups(`file_group_id`)
);

CREATE TABLE `damagelogs` (
	`damage_log_id` INT NOT NULL AUTO_INCREMENT,
	`damage_log_damage_id` INT NOT NULL,
	`damage_log_description` TEXT,
   	`damage_log_created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`damage_log_id`),
	FOREIGN KEY (`damage_log_damage_id`) REFERENCES damages(`damage_id`)
);

CREATE TABLE `messages` ( # from user to user != notifications
	`message_id` INT NOT NULL AUTO_INCREMENT,
	`message_from_user_id` INT NOT NULL,
	`message_to_user_id` INT NOT NULL,
	`message_read` BIT(1) NOT NULL DEFAULT 0,
	`message_subject` VARCHAR(255) NOT NULL DEFAULT 'Bericht van een Dégage-gebruiker',
	`message_body` TEXT NOT NULL,
   `message_created_at` DATETIME,
   `message_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`message_id`),
	FOREIGN KEY (`message_from_user_id`) REFERENCES users(`user_id`),
	FOREIGN KEY (`message_to_user_id`) REFERENCES users(`user_id`)
);

CREATE TABLE `templates` (
	`template_id` INT NOT NULL,
	`template_title` VARCHAR(255) NOT NULL,
	`template_subject` VARCHAR(255) NOT NULL DEFAULT 'Bericht van Dégage!',
	`template_body` TEXT NOT NULL,
	`template_send_mail` BIT(1) NOT NULL DEFAULT 1, # Mail of notificatie verzenden? Instelbaar via dashboard mailtemplates
	`template_send_mail_changeable` BIT(1) NOT NULL DEFAULT 1, # Mag aangepast worden? Bv wachtwoord reset/verificatie niet!
	`template_created_at` DATETIME,
	`template_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`template_id`),
	UNIQUE INDEX `template_title` (`template_title`)
);

CREATE TABLE `templatetags` ( # Welke tags kunnen we gebruiken in de templates
	`template_tag_id` INT NOT NULL AUTO_INCREMENT,
	`template_tag_body` VARCHAR(255) NOT NULL,
	PRIMARY KEY (`template_tag_id`)
);

CREATE TABLE `templatetagassociations` ( # Welke tags horen bij welke templates
	`template_tag_association_id` INT NOT NULL AUTO_INCREMENT,
	`template_tag_id` INT NOT NULL,
	`template_id` INT NOT NULL,
	PRIMARY KEY (`template_tag_association_id`),
	FOREIGN KEY (`template_id`) REFERENCES templates(`template_id`),
	FOREIGN KEY (`template_tag_id`) REFERENCES templatetags(`template_tag_id`)
);

CREATE TABLE `verifications` (
	`verification_ident` CHAR(37) NOT NULL,
	`verification_user_id` INT NOT NULL,
	`verification_type` ENUM('REGISTRATION','PWRESET') NOT NULL DEFAULT 'REGISTRATION',
	`verification_created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`verification_user_id`, `verification_type`),
	CONSTRAINT `FK_VERIFICATION_USER` FOREIGN KEY (`verification_user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `notifications` ( # from system to user
	`notification_id` INT NOT NULL AUTO_INCREMENT,
	`notification_user_id` INT NOT NULL,
	`notification_read` BIT(1) NOT NULL DEFAULT 0,
	`notification_subject` VARCHAR(255),
	`notification_body` TEXT,
   `notification_created_at` DATETIME,
   `notification_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`notification_id`),
	FOREIGN KEY (`notification_user_id`) REFERENCES users(`user_id`)
);

CREATE TABLE `approvals` (
  `approval_id` INT NOT NULL AUTO_INCREMENT,
  `approval_user` INT NULL DEFAULT NULL,
  `approval_admin` INT NULL DEFAULT NULL,
  `approval_submission` DATETIME NOT NULL,
  `approval_date` DATETIME NULL DEFAULT NULL,
  `approval_status` ENUM('PENDING','ACCEPTED','DENIED') NOT NULL DEFAULT 'PENDING',
  `approval_infosession` INT NULL DEFAULT NULL,
  `approval_user_message` TEXT NULL,
  `approval_admin_message` TEXT NULL,
  PRIMARY KEY (`approval_id`),
  INDEX `FK_approval_user` (`approval_user`),
  INDEX `FK_approval_admin` (`approval_admin`),
  INDEX `FK_approval_session` (`approval_infosession`),
  CONSTRAINT `FK_approval_admin` FOREIGN KEY (`approval_admin`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FK_approval_session` FOREIGN KEY (`approval_infosession`) REFERENCES `infosessions` (`infosession_id`),
  CONSTRAINT `FK_approval_user` FOREIGN KEY (`approval_user`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `jobs` (
  `job_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `job_type` ENUM('IS_REMINDER','RES_REMINDER','REPORT', 'RESERVE_ACCEPT', 'DRIVE_FINISH') NOT NULL DEFAULT 'REPORT',
  `job_ref_id` INT NULL DEFAULT '0',
  `job_time` DATETIME NOT NULL,
  `job_finished` BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`job_id`)
);


CREATE TABLE `settings` (
  `setting_id` INT NOT NULL AUTO_INCREMENT,
  `setting_name` CHAR(32) NOT NULL,
  `setting_value` VARCHAR(256) NULL DEFAULT NULL,
  `setting_after` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`setting_id`)
);

CREATE TABLE `receipts` (
  `receipt_id` INT NOT NULL AUTO_INCREMENT,
  `receipt_name` CHAR(32) NOT NULL,
  `receipt_date` DATETIME NULL DEFAULT NULL,
  `receipt_fileID` INT NULL DEFAULT NULL,
  `receipt_userID` INT NOT NULL,
  `receipt_price` DECIMAL(19,4),
  PRIMARY KEY (`receipt_id`),
  FOREIGN KEY (`receipt_fileID`) REFERENCES files(`file_id`),
  FOREIGN KEY (`receipt_userID`) REFERENCES users(`user_id`)
);

-- TRIGGERS
-- ~~~~~~~~

-- TODO: can this not be done with a default value?

DELIMITER $$

CREATE TRIGGER files_ins BEFORE INSERT ON files FOR EACH ROW
BEGIN
  IF new.file_created_at IS NULL THEN
    SET new.file_created_at = now();
  END IF;
END $$

CREATE TRIGGER addresses_ins BEFORE INSERT ON addresses FOR EACH ROW
BEGIN
  IF new.address_created_at IS NULL THEN
    SET new.address_created_at = now();
  END IF;
END $$

CREATE TRIGGER users_ins BEFORE INSERT ON users FOR EACH ROW
BEGIN
  IF new.user_created_at IS NULL THEN
    SET new.user_created_at = now();
  END IF;
  IF new.user_last_notified IS NULL THEN
    SET new.user_last_notified = now();
  END IF;
END $$

CREATE TRIGGER cars_ins BEFORE INSERT ON cars FOR EACH ROW
BEGIN
  IF new.car_created_at IS NULL THEN
    SET new.car_created_at = now();
  END IF;
END $$

CREATE TRIGGER carinsurances_ins BEFORE INSERT ON carinsurances FOR EACH ROW
BEGIN
  IF new.insurance_created_at IS NULL THEN
    SET new.insurance_created_at = now();
  END IF;
END $$

CREATE TRIGGER TechnicalcarsDetails_ins BEFORE INSERT ON technicalcardetails FOR EACH ROW
BEGIN
  IF new.details_created_at IS NULL THEN
    SET new.details_created_at = now();
  END IF;
END $$

CREATE TRIGGER carreservations_ins BEFORE INSERT ON carreservations FOR EACH ROW
BEGIN
  IF new.reservation_created_at IS NULL THEN
    SET new.reservation_created_at = now();
  END IF;
END $$

CREATE TRIGGER infosessions_ins BEFORE INSERT ON infosessions FOR EACH ROW
BEGIN
  IF new.infosession_created_at IS NULL THEN
    SET new.infosession_created_at = now();
  END IF;
END $$

CREATE TRIGGER carcosts_ins BEFORE INSERT ON carcosts FOR EACH ROW
BEGIN
  IF new.car_cost_created_at IS NULL THEN
    SET new.car_cost_created_at = now();
  END IF;
END $$

CREATE TRIGGER refuels_ins BEFORE INSERT ON refuels FOR EACH ROW
BEGIN
  IF new.refuel_created_at IS NULL THEN
    SET new.refuel_created_at = now();
  END IF;
END $$

CREATE TRIGGER damages_ins BEFORE INSERT ON damages FOR EACH ROW
BEGIN
  IF new.damage_created_at IS NULL THEN
    SET new.damage_created_at = now();
  END IF;
END $$

CREATE TRIGGER carrides_ins BEFORE INSERT ON carrides FOR EACH ROW
BEGIN
  IF new.car_ride_created_at IS NULL THEN
    SET new.car_ride_created_at = now();
  END IF;
END $$

CREATE TRIGGER templates_ins BEFORE INSERT ON templates FOR EACH ROW
BEGIN
  IF new.template_created_at IS NULL THEN
    SET new.template_created_at = now();
  END IF;
END $$

CREATE TRIGGER messages_ins BEFORE INSERT ON messages FOR EACH ROW
BEGIN
  IF new.message_created_at IS NULL THEN
    SET new.message_created_at = now();
  END IF;
END $$

CREATE TRIGGER notifications_ins BEFORE INSERT ON notifications FOR EACH ROW
BEGIN
  IF new.notification_created_at IS NULL THEN
    SET new.notification_created_at = now();
  END IF;
END $$
DELIMITER ;

