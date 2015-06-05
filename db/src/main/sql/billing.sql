-- STORED PROCEDURES USED IN BILLING

DELIMITER $$

-- reset billing for car, must be called before computing bills
-- b_id: id of billing
-- c_id: id of car
DROP PROCEDURE IF EXISTS billing_reset $$
CREATE PROCEDURE billing_reset (b_id INT, c_id INT)
BEGIN
  DELETE FROM b_trip
     WHERE bt_billing_id = b_id AND bt_car_id = c_id;
  DELETE FROM b_fuel
     WHERE bf_billing_id = b_id AND bf_car_id = c_id;
END $$

-- reset billing for all cars
-- b_id: id of billing
DROP PROCEDURE IF EXISTS billing_reset_all $$
CREATE PROCEDURE billing_reset_all (b_id INT)
BEGIN
  DELETE FROM b_trip
     WHERE bt_billing_id = b_id;
  DELETE FROM b_fuel
     WHERE bf_billing_id = b_id;
END $$


-- create bt_trip records for billing a certain car
-- b_id: id of billing
-- limit: limit date, only records before this date will be considered
-- c_id: id of car
-- c_name: name of car
DROP PROCEDURE IF EXISTS billing_trip_aux $$
CREATE PROCEDURE billing_trip_aux (b_id INT, c_id INT, lim DATETIME, c_name VARCHAR(64))
BEGIN
    -- basic records
    INSERT INTO b_trip(bt_billing_id,bt_reservation_id,bt_user_id,bt_car_id,bt_car_name,bt_datetime,bt_km,bt_privileged)
    SELECT
       b_id, reservation_id, reservation_user_id, reservation_car_id, c_name, reservation_from,
       car_ride_end_km - car_ride_start_km,
       reservation_owner_id = reservation_user_id
    FROM reservations
        JOIN carrides ON reservation_id = car_ride_car_reservation_id
    WHERE reservation_car_id = c_id AND reservation_status = 'FINISHED' AND reservation_from <  lim;

    -- delete zero km trips
    DELETE FROM b_trip
        WHERE bt_billing_id = b_id AND bt_car_id = c_id AND bt_km <= 0;

    -- adjust privileged flag for privileged users
    UPDATE b_trip
       JOIN carprivileges ON
            car_privilege_user_id = bt_user_id AND car_privilege_car_id = bt_car_id
    SET
       bt_privileged = 1
    WHERE bt_billing_id = b_id AND bt_car_id = c_id;

    -- compute km costs
    UPDATE b_trip
       JOIN km_price ON km_price_billing_id=bt_billing_id
    SET
       bt_km_cost = bt_km_cost + km_price_factor*bt_km
    WHERE
        bt_billing_id = b_id AND bt_car_id = c_id AND bt_km >= km_price_from;
END $$

-- create
DROP PROCEDURE IF EXISTS billing_fuel_aux $$
CREATE PROCEDURE billing_fuel_aux (b_id INT, c_id INT, lim DATETIME, c_name VARCHAR(64))
BEGIN
    -- basic records
    INSERT INTO b_fuel(bf_billing_id,bf_refuel_id,bf_reservation_id,bf_user_id,bf_car_id,bf_car_name,bf_datetime,bf_privileged,bf_fuel_cost)
    SELECT
       b_id, refuel_id, reservation_id, reservation_user_id, reservation_car_id, c_name, reservation_from,
       reservation_owner_id = reservation_user_id, refuel_eurocents
    FROM refuels
        JOIN reservations ON refuel_car_ride_id = reservation_id
        JOIN carrides ON reservation_id = car_ride_car_reservation_id
    WHERE reservation_car_id = c_id AND reservation_status = 'FINISHED'
        AND reservation_from <  lim AND refuel_status = 'ACCEPTED';

    -- adjust privileged flag for privileged users
    UPDATE b_fuel
       JOIN carprivileges ON
            car_privilege_user_id = bf_user_id AND car_privilege_car_id = bf_car_id
    SET
       bf_privileged = 1
    WHERE bf_billing_id = b_id AND bf_car_id = c_id;
END $$

-- add sequence number to user bills
DROP PROCEDURE IF EXISTS billing_user_seq_nr $$
CREATE PROCEDURE billing_user_seq_nr (b_id INT)
BEGIN
  DECLARE seq INT DEFAULT 1;

  -- set up everything for loop
  DECLARE done BOOLEAN DEFAULT FALSE;
  DECLARE _id INT;
  DECLARE cur CURSOR FOR
      SELECT DISTINCT bt_user_id FROM b_trip WHERE NOT bt_privileged AND bt_km_cost > 0
      UNION
      SELECT DISTINCT bf_user_id FROM b_fuel WHERE NOT bf_privileged;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  -- Reset
  DELETE FROM b_user WHERE bu_billing_id = b_id;

  -- Add sequence numbers
  OPEN cur;

  main: LOOP
    FETCH cur INTO _id;
    IF done THEN
      LEAVE main;
    END IF;
    INSERT INTO  b_user(bu_billing_id,bu_user_id,bu_seq_nr)
        VALUES (b_id, _id, seq);
    SET seq = seq + 1;
  END LOOP main;

  CLOSE cur;

END  $$

-- perform a simulation run
DROP PROCEDURE IF EXISTS billing_user_simulate $$
CREATE PROCEDURE billing_user_simulate (b_id INT)
BEGIN
  DECLARE lim DATETIME;

  -- set up everything for loop
  DECLARE done BOOLEAN DEFAULT FALSE;
  DECLARE _id INT;
  DECLARE _name VARCHAR(64);
  DECLARE cur CURSOR FOR
      SELECT car_id,car_name FROM cars_billed JOIN cars USING(car_id);
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;


  SELECT billing_limit FROM billing WHERE billing_id = b_id INTO lim;

  -- clear results of earlier simulations
  CALL billing_reset_all(b_id);

  -- bill trips and fuels for every car
  OPEN cur;

  main: LOOP
    FETCH cur INTO _id, _name;
    IF done THEN
      LEAVE main;
    END IF;
    CALL billing_trip_aux(b_id, _id, lim, _name);
    CALL billing_fuel_aux(b_id, _id, lim, _name);
  END LOOP main;

  CLOSE cur;

  CALL billing_user_seq_nr(b_id);

  -- TODO: freeze all simulated reservations and trips
  -- TODO: set status in billing
END $$

-- TODO: make sure refuels cannot be added to frozen reservations

--
DELIMITER ;
