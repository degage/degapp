CREATE TABLE `carassistances` (
	`assistance_id` INT NOT NULL,
	`assistance_name` VARCHAR(64),
	`assistance_expiration` DATE,
	`assistance_contract_id` VARCHAR(64),
	`assistance_type` ENUM('NONE','FULL','ACCIDENT','DEFECT'),
	`assistance_updated_at` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`assistance_id`)
);

INSERT INTO carassistances (assistance_id, assistance_name, assistance_expiration, assistance_contract_id, assistance_type, assistance_updated_at)
SELECT car_id, null, null, null, 'NONE', null
FROM cars;