-- Changed refuel structure
ALTER TABLE carrides DROP COLUMN car_ride_refueling;

DELETE FROM refuels WHERE refuel_status = 'CREATED';

ALTER TABLE refuels MODIFY COLUMN `refuel_status` ENUM('REQUEST','ACCEPTED', 'REFUSED') NOT NULL DEFAULT 'REQUEST';


