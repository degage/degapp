-- changes needed to enable upload of car data
ALTER TABLE cars
    MODIFY column    `car_comments` VARCHAR(4096);

ALTER TABLE cars
    MODIFY COLUMN `car_fuel` 
      ENUM('PETROL','DIESEL', 'BIODIESEL', 'LPG', 'CNG', 'HYBRID', 'ELECTRIC');
