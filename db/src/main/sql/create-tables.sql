-- create-tables.sql
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- Copyright Ⓒ 2014-2015 Universiteit Gent
-- 
-- This file is part of the Degage Web Application
-- 
-- Corresponding author (see also AUTHORS.txt)
-- 
-- Kris Coolsaet
-- Department of Applied Mathematics, Computer Science and Statistics
-- Ghent University 
-- Krijgslaan 281-S9
-- B-9000 GENT Belgium
-- 
-- The Degage Web Application is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
-- 
-- The Degage Web Application is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Affero General Public License for more details.
-- 
-- You should have received a copy of the GNU Affero General Public License
-- along with the Degage Web Application (file LICENSE.txt in the
-- distribution).  If not, see <http://www.gnu.org/licenses/>.

-- Creates the tables

-- Note we use TIMESTAMP only for registering changes in the database
-- User visible dates and times are represented by DATETIME, and have no timezone. For example, if from England you
-- make a reservation of a car, you should do this in Belgian time. Reservation intervals across a daylight saving time border,
-- may be one hour off.
--
-- All times displayed by the web application should therefore be regarded as with respect to a fixed time zone,
-- the time zone of the server. The corresponding Java type is LocalDateTime.
--
-- This choice is not without difficulties, but we think this is the behaviour which the user expects,

SET names utf8;
SET default_storage_engine=INNODB;

-- TODO: split into separate files

-- TABLES
-- ~~~~~~

CREATE TABLE `settings` (
  `setting_name` CHAR(32) NOT NULL,
  `setting_value` VARCHAR(256) NULL DEFAULT NULL,
  `setting_after` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`setting_name`,`setting_after`)
);


CREATE TABLE `files` (
  `file_id` INT NOT NULL AUTO_INCREMENT,
  `file_path` VARCHAR(255) NOT NULL,
  `file_name` VARCHAR(128) NULL,
  `file_content_type` VARCHAR(64) NULL,
  `file_created_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `file_updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`file_id`)
);


CREATE TABLE `addresses` (
  `address_id` INT NOT NULL AUTO_INCREMENT,
  `address_country` VARCHAR(64) NOT NULL DEFAULT 'België',
  `address_city` VARCHAR(64) NOT NULL DEFAULT '',
  `address_zipcode` VARCHAR(12) NOT NULL DEFAULT '',
  `address_street` VARCHAR(64) NOT NULL DEFAULT '',
  `address_number` VARCHAR(12) NOT NULL DEFAULT '',
  `address_created_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `address_updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`address_id`)
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
	`user_driver_license_date` DATE,
	`user_identity_card_id` VARCHAR(32), -- Identiteitskaartnr
	`user_identity_card_registration_nr` VARCHAR(32), -- Rijksregisternummer
	`user_status` ENUM('REGISTERED','FULL_VALIDATING','FULL','BLOCKED','DROPPED','INACTIVE') NOT NULL DEFAULT 'REGISTERED', -- Stadia die de gebruiker moet doorlopen
	`user_damage_history` TEXT,
	`user_agree_terms` BIT(1),
	`user_image_id` INT,
	`user_degage_id` INT,
	`user_date_joined` DATE,
	`user_deposit` INT,
	`user_last_notified` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`user_created_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`user_updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`user_id`),
	FOREIGN KEY (`user_address_domicile_id`) REFERENCES addresses(`address_id`),
	FOREIGN KEY (`user_address_residence_id`) REFERENCES addresses(`address_id`),
	FOREIGN KEY (`user_image_id`) REFERENCES files(`file_id`),
	UNIQUE INDEX `user_email` (`user_email`)
);

CREATE TABLE `verifications` (
	`verification_ident` CHAR(37) NOT NULL,
	`verification_email` VARCHAR(64) NOT NULL,
	`verification_type` ENUM('REGISTRATION','PWRESET') NOT NULL DEFAULT 'REGISTRATION',
	`verification_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`verification_ident`)
);

CREATE TABLE idfiles (
    user_id INT NOT NULL,
    file_id INT NOT NULL,
    PRIMARY KEY (user_id, file_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (file_id) REFERENCES files(file_id) ON DELETE CASCADE
);

CREATE TABLE licensefiles (
    user_id INT NOT NULL,
    file_id INT NOT NULL,
    PRIMARY KEY (user_id, file_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (file_id) REFERENCES files(file_id)  ON DELETE CASCADE
);

CREATE TABLE `userroles` (
	`userrole_userid` INT NOT NULL,
	`userrole_role` ENUM('SUPER_USER', 'CAR_OWNER', 'CAR_USER', 'INFOSESSION_ADMIN', 'MAIL_ADMIN', 'PROFILE_ADMIN', 'RESERVATION_ADMIN', 'CAR_ADMIN') NOT NULL,
	PRIMARY KEY (`userrole_userid`, `userrole_role`),
	FOREIGN KEY (`userrole_userid`) REFERENCES users(`user_id`)
);

CREATE TABLE `carinsurances` (
	`insurance_id` INT NOT NULL,
	`insurance_name` VARCHAR(64),
	`insurance_expiration` DATE,
	`insurance_contract_id` VARCHAR(64), -- Polisnr
	`insurance_bonus_malus` VARCHAR(64),
	`insurance_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`insurance_id`)
);

CREATE TABLE `technicalcardetails` (
	`details_id` INT NOT NULL,
	`details_car_license_plate` VARCHAR(64),
	`details_car_registration` INT,
	`details_car_chassis_number` VARCHAR(17),
	`details_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`details_id`),
	UNIQUE INDEX `ix_details` (`details_car_license_plate`, `details_car_chassis_number`),
	FOREIGN KEY (`details_car_registration`) REFERENCES files(`file_id`)
);

CREATE TABLE `cars` (
	`car_id` INT NOT NULL AUTO_INCREMENT,
	`car_name` VARCHAR(64) NOT NULL,
	`car_email` VARCHAR(64) NOT NULL,
	`car_type` VARCHAR(64) NOT NULL DEFAULT '0',
	`car_brand` VARCHAR(64) NOT NULL DEFAULT '0',
	`car_location` INT,
	`car_seats` TINYINT UNSIGNED,
	`car_doors` TINYINT UNSIGNED,
	`car_year` INT,
	`car_manual` BIT(1) NOT NULL DEFAULT 0,
	`car_gps` BIT(1) NOT NULL DEFAULT 0,
	`car_hook` BIT(1) NOT NULL DEFAULT 0,
	`car_fuel` ENUM('PETROL','DIESEL', 'BIODIESEL', 'LPG', 'CNG', 'HYBRID', 'ELECTRIC'),
	`car_fuel_economy` INT,
	`car_estimated_value` INT,
	`car_owner_annual_km` INT,
	`car_owner_user_id` INT NOT NULL,
	`car_comments` VARCHAR(4096),
	`car_active` BIT(1) NOT NULL DEFAULT 0,
	`car_images_id` INT,
	`car_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`car_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`car_id`),
	FOREIGN KEY (`car_owner_user_id`) REFERENCES users(`user_id`) ON DELETE CASCADE,
	FOREIGN KEY (`car_location`) REFERENCES addresses(`address_id`) ON DELETE CASCADE,
	FOREIGN KEY (`car_images_id`) REFERENCES files(`file_id`)
);

CREATE TABLE `reservations` (
	`reservation_id` INT NOT NULL AUTO_INCREMENT,
	`reservation_status` ENUM('REQUEST','ACCEPTED', 'REFUSED', 'CANCELLED', 'REQUEST_DETAILS', 'DETAILS_PROVIDED', 'FINISHED') NOT NULL DEFAULT 'REQUEST',
	`reservation_car_id` INT NOT NULL,
	`reservation_user_id` INT NOT NULL,
	`reservation_owner_id` INT NOT NULL,
	`reservation_privileged` BIT(1) NOT NULL DEFAULT 0,
	`reservation_from` DATETIME NOT NULL,
	`reservation_to` DATETIME NOT NULL,
	`reservation_message` VARCHAR(4096),
	`reservation_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`reservation_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`reservation_id`),
	FOREIGN KEY (`reservation_car_id`) REFERENCES cars(`car_id`),
	FOREIGN KEY (`reservation_user_id`) REFERENCES users(`user_id`),
	FOREIGN KEY (`reservation_owner_id`) REFERENCES users(`user_id`)
);

CREATE TABLE `infosessions` (
	`infosession_id` INT NOT NULL AUTO_INCREMENT,
	`infosession_type` ENUM('NORMAL', 'OWNER', 'OTHER') NOT NULL DEFAULT 'NORMAL',
	`infosession_timestamp` TIMESTAMP NULL,
	`infosession_address_id` INT NOT NULL,
	`infosession_host_user_id` INT,
	`infosession_max_enrollees` INT,
	`infosession_comments` VARCHAR(4096),
	`infosession_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`infosession_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`infosession_id`),
	FOREIGN KEY (`infosession_host_user_id`) REFERENCES users(`user_id`),
	FOREIGN KEY (`infosession_address_id`) REFERENCES addresses(`address_id`)
);

CREATE TABLE `infosessionenrollees` ( -- Wie is ingeschreven?
	`infosession_id` INT NOT NULL,
	`infosession_enrollee_id` INT NOT NULL,
	`infosession_enrollment_status` ENUM('ENROLLED', 'PRESENT', 'ABSENT') NOT NULL DEFAULT 'ENROLLED',
	PRIMARY KEY (`infosession_id`, `infosession_enrollee_id`),
	FOREIGN KEY (`infosession_enrollee_id`) REFERENCES users(`user_id`),
	FOREIGN KEY (`infosession_id`) REFERENCES infosessions(`infosession_id`) ON DELETE CASCADE
);

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

-- Keeps track of intervals where cars are NOT available
CREATE TABLE `caravailabilities` (
	`car_availability_id` INT NOT NULL AUTO_INCREMENT,
	`car_availability_car_id` INT NOT NULL,
	`car_availability_start` INT NOT NULL, -- seconds in range 0-604800
	`car_availability_end` INT NOT NULL,  -- seconds in range ...start-1209600
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
	`car_cost_status` ENUM('REQUEST','ACCEPTED', 'REFUSED') NOT NULL DEFAULT 'REQUEST', -- approved by car_admin
	`car_cost_time` DATE,
	`car_cost_mileage` DECIMAL(10,1),
	`car_cost_billed` DATE DEFAULT NULL,
	`car_cost_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`car_cost_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`car_cost_id`),
	FOREIGN KEY (`car_cost_car_id`) REFERENCES cars(`car_id`),
	FOREIGN KEY (`car_cost_proof`) REFERENCES files(`file_id`)
);

CREATE TABLE `carrides` (
  `car_ride_car_reservation_id` INT NOT NULL, -- also primary key
  `car_ride_status` BIT(1) NOT NULL DEFAULT 0, -- approved by owner?
  `car_ride_start_km` INTEGER NOT NULL DEFAULT 0,
  `car_ride_end_km` INTEGER NOT NULL DEFAULT 0,
  `car_ride_damage` BIT(1) NOT NULL DEFAULT 0,
  `car_ride_cost` DECIMAL(19,4) DEFAULT NULL,
  `car_ride_billed` DATE DEFAULT NULL,
  `car_ride_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `car_ride_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`car_ride_car_reservation_id`),
  FOREIGN KEY (`car_ride_car_reservation_id`) REFERENCES reservations(`reservation_id`)
);

CREATE TABLE `refuels` (
	`refuel_id` INT NOT NULL AUTO_INCREMENT,
	`refuel_car_ride_id` INT NOT NULL,
	`refuel_file_id` INT,
	`refuel_eurocents` INT,
	`refuel_status` ENUM('REQUEST','ACCEPTED', 'REFUSED') NOT NULL DEFAULT 'REQUEST',
	`refuel_billed` DATE DEFAULT NULL,
	`refuel_km` INTEGER,
	`refuel_amount` VARCHAR(16), -- amount of fuel, free format
	`refuel_message` VARCHAR(4096),
   	`refuel_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   	`refuel_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`refuel_id`),
	FOREIGN KEY (`refuel_car_ride_id`) REFERENCES reservations(`reservation_id`),
	FOREIGN KEY (`refuel_file_id`) REFERENCES files(`file_id`)
);

CREATE TABLE `damages` (
	`damage_id` INT NOT NULL AUTO_INCREMENT,
	`damage_car_ride_id` INT NOT NULL,
	`damage_description` TEXT,
	`damage_finished` BIT(1) NOT NULL DEFAULT 0,
	`damage_time` DATE,
   	`damage_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   	`damage_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`damage_id`),
	FOREIGN KEY (`damage_car_ride_id`) REFERENCES carrides(`car_ride_car_reservation_id`)
);

CREATE TABLE damagefiles (
    damage_id INT NOT NULL,
    file_id INT NOT NULL,
    PRIMARY KEY (damage_id, file_id),
    FOREIGN KEY (damage_id) REFERENCES damages(damage_id),
    FOREIGN KEY (file_id) REFERENCES files(file_id)
);

CREATE TABLE `damagelogs` (
	`damage_log_id` INT NOT NULL AUTO_INCREMENT,
	`damage_log_damage_id` INT NOT NULL,
	`damage_log_description` TEXT,
   	`damage_log_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   	`damage_log_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`damage_log_id`),
	FOREIGN KEY (`damage_log_damage_id`) REFERENCES damages(`damage_id`)
);

CREATE TABLE `messages` ( -- from user to user != notifications
	`message_id` INT NOT NULL AUTO_INCREMENT,
	`message_from_user_id` INT NOT NULL,
	`message_to_user_id` INT NOT NULL,
	`message_read` BIT(1) NOT NULL DEFAULT 0,
	`message_subject` VARCHAR(255) NOT NULL DEFAULT 'Bericht van een Dégage-gebruiker',
	`message_body` TEXT NOT NULL,
   `message_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   `message_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`message_id`),
	FOREIGN KEY (`message_from_user_id`) REFERENCES users(`user_id`),
	FOREIGN KEY (`message_to_user_id`) REFERENCES users(`user_id`)
);

CREATE TABLE `notifications` ( -- from system to user
	`notification_id` INT NOT NULL AUTO_INCREMENT,
	`notification_user_id` INT NOT NULL,
	`notification_read` BIT(1) NOT NULL DEFAULT 0,
	`notification_subject` VARCHAR(255),
	`notification_body` TEXT,
   `notification_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   `notification_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`notification_id`),
	FOREIGN KEY (`notification_user_id`) REFERENCES users(`user_id`)
);

CREATE TABLE `approvals` (
  `approval_id` INT NOT NULL AUTO_INCREMENT,
  `approval_user` INT NULL DEFAULT NULL,
  `approval_admin` INT NULL DEFAULT NULL,
  `approval_submission` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `approval_date` TIMESTAMP NULL DEFAULT NULL,
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
  `job_type` ENUM('IS_REMINDER','RES_REMINDER','REPORT') NOT NULL DEFAULT 'REPORT',
  `job_ref_id` INT NULL DEFAULT '0',
  `job_time` TIMESTAMP,
  `job_finished` BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`job_id`)
);


CREATE TABLE `receipts` (
  `receipt_id` INT NOT NULL AUTO_INCREMENT,
  `receipt_name` CHAR(32) NOT NULL,
  `receipt_date` DATE NULL DEFAULT NULL,
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

CREATE TRIGGER users_create BEFORE INSERT ON users FOR EACH ROW
BEGIN
   INSERT INTO addresses VALUES ();
   SET NEW.user_address_domicile_id = last_insert_id();
   INSERT INTO addresses VALUES ();
   SET NEW.user_address_residence_id = last_insert_id();
END $$

CREATE TRIGGER cars_make AFTER INSERT ON cars FOR EACH ROW
BEGIN
  INSERT INTO technicalcardetails(details_id) VALUES (new.car_id);
  INSERT INTO carinsurances(insurance_id) VALUES (new.car_id);
END $$

CREATE TRIGGER cars_create BEFORE INSERT ON cars FOR EACH ROW
BEGIN
  INSERT INTO addresses VALUES ();
  SET NEW.car_location = last_insert_id();
END $$

CREATE TRIGGER infosession_create BEFORE INSERT ON infosessions FOR EACH ROW
BEGIN
  INSERT INTO addresses VALUES ();
  SET NEW.infosession_address_id = last_insert_id();
END $$

CREATE TRIGGER reservations_ins BEFORE INSERT ON reservations FOR EACH ROW
BEGIN
    DECLARE privileged int default 0;

    SELECT 1 INTO privileged FROM carprivileges
       WHERE car_privilege_car_id = NEW.reservation_car_id AND  car_privilege_user_id = NEW.reservation_user_id;
    SELECT 1 INTO privileged FROM cars
       WHERE car_id = NEW.reservation_car_id AND car_owner_user_id = NEW.reservation_user_id;

    SET NEW.reservation_owner_id =
       ( SELECT car_owner_user_id FROM cars WHERE car_id = NEW.reservation_car_id );

    IF privileged THEN
        IF NEW.reservation_from < NOW() THEN
            SET NEW.reservation_status = 'REQUEST_DETAILS';
        ELSE
            SET NEW.reservation_status = 'ACCEPTED';
        END IF;
    ELSE
        SET NEW.reservation_status = 'REQUEST';
        -- TODO: add corresponding job

    END IF;
    IF new.reservation_created_at IS NULL THEN
        SET new.reservation_created_at = now();
    END IF;
END $$

DELIMITER ;

