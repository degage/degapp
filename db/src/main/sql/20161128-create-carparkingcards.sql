CREATE TABLE `carparkingcards` (
	`parkingcard_id` INT NOT NULL,
	`parkingcard_city` VARCHAR(64),
	`parkingcard_expiration` DATE,
	`parkingcard_contract_id` VARCHAR(64),
	`parkingcard_zones` VARCHAR(64),
	`parkingcard_updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`parkingcard_id`)
);

INSERT INTO carparkingcards (parkingcard_id, parkingcard_city, parkingcard_expiration, parkingcard_contract_id, parkingcard_zones, parkingcard_updated_at)
SELECT car_id, null, null, null, null, null
FROM cars;

DROP TRIGGER cars_make;

DELIMITER $$
CREATE TRIGGER cars_make AFTER INSERT ON cars FOR EACH ROW
BEGIN
  INSERT INTO technicalcardetails(details_id) VALUES (new.car_id);
  INSERT INTO carinsurances(insurance_id) VALUES (new.car_id);
  INSERT INTO carassistances(assistance_id) VALUES (new.car_id);
  INSERT INTO carparkingcards(parkingcard_id) VALUES (new.car_id);
END $$
DELIMITER ;