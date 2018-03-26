CREATE TABLE `carapprovals` (
  `carapproval_id` INT NOT NULL AUTO_INCREMENT,
  `carapproval_car_id` INT NULL DEFAULT NULL,
  `carapproval_admin_id` INT NULL DEFAULT NULL,
  `carapproval_submission_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `carapproval_date` TIMESTAMP NULL DEFAULT NULL,
  `carapproval_status` ENUM('REQUEST','ACCEPTED','REFUSED','FROZEN') NOT NULL DEFAULT 'REQUEST',
  `carapproval_user_message` TEXT NULL,
  `carapproval_admin_message` TEXT NULL,
  PRIMARY KEY (`carapproval_id`),
  INDEX `FK_carapproval_car_id` (`carapproval_car_id`),
  INDEX `FK_carapproval_admin_id` (`carapproval_admin_id`),
  CONSTRAINT `FK_carapproval_admin_id` FOREIGN KEY (`carapproval_admin_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FK_carapproval_car_id` FOREIGN KEY (`carapproval_car_id`) REFERENCES `cars` (`car_id`)
);