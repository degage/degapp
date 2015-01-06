-- Added owner id to reservation
ALTER TABLE reservations ADD COLUMN `reservation_owner_id` INT NOT NULL REFERENCES users(user_id);

UPDATE reservations JOIN cars ON (car_id = reservation_car_id)
   SET reservation_owner_id = car_owner_user_id;

DROP TRIGGER reservations_ins;

DELIMITER $$

CREATE TRIGGER reservations_ins BEFORE INSERT ON reservations FOR EACH ROW
BEGIN
    DECLARE privileged int default 0;

    SELECT 1 INTO privileged FROM carprivileges
       WHERE car_privilege_car_id = NEW.reservation_car_id AND  car_privilege_user_id = NEW.reservation_user_id;
    SELECT 1 INTO privileged FROM cars
       WHERE car_id = NEW.reservation_car_id AND car_owner_user_id = NEW.reservation_user_id;

    SET NEW.reservation_owner_id =
       ( SELECT car_owner_user_id FROM cars WHERE car_id = NEW.reservation_car_id );

    IF privileged THEN
        SET NEW.reservation_status = 'ACCEPTED';
    ELSE
        SET NEW.reservation_status = 'REQUEST';
        -- TODO: add corresponding job

    END IF;
    IF new.reservation_created_at IS NULL THEN
        SET new.reservation_created_at = now();
    END IF;
END $$

DELIMITER ;

