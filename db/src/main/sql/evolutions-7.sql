-- New treatment of date time related fields
ALTER TABLE files MODIFY COLUMN file_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE files MODIFY COLUMN file_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
DROP TRIGGER files_ins;

ALTER TABLE addresses MODIFY COLUMN address_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE addresses MODIFY COLUMN address_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
DROP TRIGGER addresses_ins;

ALTER TABLE users MODIFY COLUMN user_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE users MODIFY COLUMN user_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users MODIFY COLUMN user_last_notified TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
DROP TRIGGER users_ins;

ALTER TABLE technicalcardetails MODIFY COLUMN details_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE carinsurances MODIFY COLUMN insurance_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE cars MODIFY COLUMN car_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE cars MODIFY COLUMN car_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
DROP TRIGGER cars_ins;

ALTER TABLE carcosts MODIFY COLUMN car_cost_time DATE;
ALTER TABLE carcosts MODIFY COLUMN car_cost_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE carcosts MODIFY COLUMN car_cost_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
DROP TRIGGER carcosts_ins;

CREATE TABLE `reservations` (
	`reservation_id` INT NOT NULL AUTO_INCREMENT,
	`reservation_status` ENUM('REQUEST','ACCEPTED', 'REFUSED', 'CANCELLED', 'REQUEST_DETAILS', 'DETAILS_PROVIDED', 'FINISHED') NOT NULL DEFAULT 'REQUEST',
	`reservation_car_id` INT NOT NULL,
	`reservation_user_id` INT NOT NULL,
	`reservation_privileged` BIT(1) NOT NULL DEFAULT 0,
	`reservation_from` DATETIME NOT NULL,
	`reservation_to` DATETIME NOT NULL,
	`reservation_message` VARCHAR(128),
	`reservation_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`reservation_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`reservation_id`),
	FOREIGN KEY (`reservation_car_id`) REFERENCES cars(`car_id`),
	FOREIGN KEY (`reservation_user_id`) REFERENCES users(`user_id`)
);

INSERT reservations
  SELECT reservation_id, reservation_status, reservation_car_id, reservation_user_id, reservation_privileged,
         reservation_from, reservation_to, reservation_message, reservation_created_at, reservation_updated_at
  FROM carreservations;

ALTER TABLE carrides DROP FOREIGN KEY carrides_ibfk_1;
DROP TABLE carreservations;
ALTER TABLE carrides ADD FOREIGN KEY (`car_ride_car_reservation_id`) REFERENCES reservations(`reservation_id`);
ALTER TABLE carrides MODIFY COLUMN car_ride_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE carrides MODIFY COLUMN car_ride_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
DROP TRIGGER carrides_ins;

ALTER TABLE refuels MODIFY COLUMN refuel_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE refuels MODIFY COLUMN refuel_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
DROP TRIGGER refuels_ins;

ALTER TABLE infosessions MODIFY COLUMN infosession_timestamp TIMESTAMP NULL;
ALTER TABLE infosessions MODIFY COLUMN infosession_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE infosessions MODIFY COLUMN infosession_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
DROP TRIGGER infosessions_ins;

ALTER TABLE damages MODIFY COLUMN damage_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE damages MODIFY COLUMN damage_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE damages MODIFY COLUMN damage_time DATE;
DROP TRIGGER damages_ins;

ALTER TABLE damagelogs ADD COLUMN damage_log_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE damagelogs MODIFY COLUMN damage_log_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE templates MODIFY COLUMN template_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE templates MODIFY COLUMN template_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
DROP TRIGGER templates_ins;

ALTER TABLE notifications MODIFY COLUMN notification_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE notifications MODIFY COLUMN notification_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
DROP TRIGGER notifications_ins;

ALTER TABLE messages MODIFY COLUMN message_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE messages MODIFY COLUMN message_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
DROP TRIGGER messages_ins;

ALTER TABLE verifications ADD COLUMN verification_updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE verifications MODIFY COLUMN verification_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE approvals MODIFY COLUMN approval_submission TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE approvals MODIFY COLUMN approval_date TIMESTAMP NULL DEFAULT NULL;

ALTER TABLE jobs MODIFY COLUMN job_time TIMESTAMP;

ALTER TABLE receipts MODIFY COLUMN receipt_date DATE NULL DEFAULT NULL;

DELIMITER $$

CREATE TRIGGER reservations_ins BEFORE INSERT ON reservations FOR EACH ROW
BEGIN
    DECLARE privileged int default 0;

    SELECT 1 INTO privileged FROM carprivileges
       WHERE car_privilege_car_id = NEW.reservation_car_id AND  car_privilege_user_id = NEW.reservation_user_id;
    SELECT 1 INTO privileged FROM cars
       WHERE car_id = NEW.reservation_car_id AND car_owner_user_id = NEW.reservation_user_id;

    IF privileged THEN
        SET NEW.reservation_status = 'ACCEPTED';
    ELSE
        SET NEW.reservation_status = 'REQUEST';
        -- TODO: add corresponding job

    END IF;
END $$

DELIMITER ;
