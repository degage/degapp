-- automatic status change for reservation by privileged driver

DROP TRIGGER carreservations_ins;

DELIMITER $$

CREATE TRIGGER carreservations_ins BEFORE INSERT ON carreservations FOR EACH ROW
BEGIN
    DECLARE privileged int default 0;

    SELECT 1 INTO privileged FROM carprivileges
       WHERE car_privilege_car_id = NEW.reservation_car_id AND  car_privilege_user_id = NEW.reservation_user_id;
    SELECT 1 INTO privileged FROM cars
       WHERE car_id = NEW.reservation_car_id AND car_owner_user_id = NEW.reservation_user_id;

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


