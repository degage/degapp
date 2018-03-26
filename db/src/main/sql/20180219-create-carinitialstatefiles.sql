CREATE TABLE `carinitialstatefiles` (
	`car_id` INT NOT NULL,
	`file_id` INT NOT NULL,
	PRIMARY KEY (`car_id`, `file_id`),
	CONSTRAINT `carinitialstatefiles_ibfk_1` FOREIGN KEY (`car_id`) REFERENCES `cars` (`car_id`),
  CONSTRAINT `carinitialstatefiles_ibfk_2` FOREIGN KEY (`file_id`) REFERENCES `files` (`file_id`) ON DELETE CASCADE
);
