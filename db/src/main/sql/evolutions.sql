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
