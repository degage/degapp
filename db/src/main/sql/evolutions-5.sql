-- added column reservation_privileged

ALTER TABLE carreservations ADD COLUMN reservation_privileged BIT(1) NOT NULL DEFAULT 0;

UPDATE carreservations SET reservation_privileged = false;

UPDATE carreservations
  JOIN carprivileges
         ON  car_privilege_car_id = reservation_car_id AND  car_privilege_user_id = reservation_user_id
  SET reservation_privileged = true;

UPDATE carreservations
  JOIN cars
         ON  car_id = reservation_car_id AND  car_owner_user_id = reservation_user_id
  SET reservation_privileged = true;

DROP TRIGGER carreservations_ins;

DROP VIEW `infosessions_extended`;

CREATE VIEW `infosessions_extended` AS
               SELECT
                   infosession_id, infosession_type, infosession_type_alternative,
                   infosession_timestamp, infosession_max_enrollees, infosession_comments,
                   address_id, address_country, address_city, address_zipcode, address_street, address_street_number, address_street_bus,
                   user_id, user_firstname, user_lastname, user_phone, user_email, user_status, user_cellphone,
                   count(infosession_enrollee_id) AS enrollee_count
               FROM infosessions AS ses
                   JOIN users ON infosession_host_user_id = user_id
                   JOIN addresses ON infosession_address_id = address_id
                   LEFT JOIN infosessionenrollees USING (infosession_id)
               GROUP BY infosession_id;


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

    SET NEW.reservation_privileged = privileged;

    END IF;
    IF new.reservation_created_at IS NULL THEN
        SET new.reservation_created_at = now();
    END IF;
END $$
