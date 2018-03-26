ALTER TABLE `cars`
ADD column `car_agreed_value` int(11) NULL,
ADD column `car_contract_file_id` int(11) NULL,
ADD column `car_contract` timestamp NULL;
ALTER TABLE `cars` MODIFY COLUMN `car_status` enum('REGISTERED','FULL','REFUSED') NOT NULL DEFAULT 'REGISTERED';

ALTER TABLE `technicalcardetails`
ADD column `details_car_first_inscription` timestamp NULL;

ALTER TABLE `carparkingcards`
ADD column `parkingcard_file_id` int(11) NULL;

ALTER TABLE `carassistances`
ADD column `assistance_file_id` int(11) NULL;

ALTER TABLE `carinsurances`
ADD column `insurance_file_id` int(11) NULL;

DROP TRIGGER cars_make;

DELIMITER $$
CREATE TRIGGER cars_make AFTER INSERT ON cars FOR EACH ROW
BEGIN
  INSERT INTO technicalcardetails(details_id) VALUES (new.car_id);
  INSERT INTO carinsurances(insurance_id) VALUES (new.car_id);
  INSERT INTO carassistances(assistance_id) VALUES (new.car_id);
  INSERT INTO carparkingcards(parkingcard_id) VALUES (new.car_id);
  INSERT INTO carapprovals(carapproval_car_id) VALUES (new.car_id);
END $$
DELIMITER ;
