-- dropped car_technical_details from car
--
ALTER TABLE cars DROP FOREIGN KEY cars_ibfk_4;
ALTER TABLE cars DROP COLUMN car_technical_details;

ALTER TABLE technicalcardetails CHANGE details_id details_id INT NOT NULL;
ALTER TABLE technicalcardetails DROP COLUMN details_created_at;

DROP TRIGGER TechnicalcarsDetails_ins;

DELIMITER $$

CREATE TRIGGER cars_make AFTER INSERT ON cars FOR EACH ROW
BEGIN
  INSERT INTO technicalcardetails(details_id) VALUES (new.car_id);
END $$

DELIMITER ;

-- dropped car_insurance

ALTER TABLE cars DROP FOREIGN KEY cars_ibfk_5;
ALTER TABLE cars DROP COLUMN car_insurance;

ALTER TABLE carinsurances CHANGE insurance_id insurance_id INT NOT NULL;
ALTER TABLE carinsurances DROP COLUMN insurance_created_at;

DROP TRIGGER carinsurances_ins;

DROP TRIGGER cars_make;

DELIMITER $$

CREATE TRIGGER cars_make AFTER INSERT ON cars FOR EACH ROW
BEGIN
  INSERT INTO technicalcardetails(details_id) VALUES (new.car_id);
  INSERT INTO carinsurances(insurance_id) VALUES (new.car_id);
END $$

DELIMITER ;
