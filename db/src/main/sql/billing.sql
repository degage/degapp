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
  DELETE FROM b_trip WHERE bt_billing_id = b_id;
  DELETE FROM b_fuel WHERE bf_billing_id = b_id;
  DELETE FROM b_user WHERE bu_billing_id = b_id;
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
    WHERE reservation_car_id = c_id AND reservation_from <  lim
        AND NOT reservation_archived
        AND (reservation_status = 'FINISHED' OR reservation_status = 'FROZEN');

    -- adjust privileged flag for privileged users
    UPDATE b_trip
       JOIN carprivileges ON
            car_privilege_user_id = bt_user_id AND car_privilege_car_id = bt_car_id
    SET bt_privileged = 1
    WHERE bt_billing_id = b_id AND bt_car_id = c_id;

    -- adjust privileged flag for owners (when owner has changed...)
    UPDATE b_trip
       JOIN cars ON car_owner_user_id = bt_user_id AND car_id = bt_car_id
    SET bt_privileged = 1
    WHERE bt_billing_id = b_id AND bt_car_id = c_id;

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
    WHERE reservation_car_id = c_id
        AND NOT refuel_archived
        AND (reservation_status = 'FINISHED' OR reservation_status = 'FROZEN')
        AND reservation_from <  lim AND (refuel_status = 'ACCEPTED' OR refuel_status = 'FROZEN');

    -- adjust privileged flag for privileged users
    UPDATE b_fuel
       JOIN carprivileges ON
            car_privilege_user_id = bf_user_id AND car_privilege_car_id = bf_car_id
    SET bf_privileged = 1
    WHERE bf_billing_id = b_id AND bf_car_id = c_id;

    -- adjust privileged flag for owners (when owner has changed...)
    UPDATE b_fuel
       JOIN cars ON car_owner_user_id = bf_user_id AND car_id = bf_car_id
    SET bf_privileged = 1
    WHERE bf_billing_id = b_id AND bf_car_id = c_id;

END $$

-- compute the total cost of a trip from the price info and total kms
-- b_id: id of billing
-- c_id: id of car
DROP PROCEDURE IF EXISTS billing_km_cost $$
CREATE PROCEDURE billing_km_cost (b_id INT, c_id INT)
BEGIN
    -- set up everything for loop
    DECLARE done BOOLEAN DEFAULT FALSE;
    DECLARE _from,_factor INT;
    DECLARE cur CURSOR FOR
        SELECT km_price_from, km_price_factor FROM km_price
        WHERE km_price_billing_id=b_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Reset cost to 0
    UPDATE b_trip SET bt_km_cost = 0
    WHERE bt_billing_id = b_id AND bt_car_id = c_id;

    -- Compute cost
    OPEN cur;

    main: LOOP
      FETCH cur INTO _from, _factor;
      IF done THEN
        LEAVE main;
      END IF;

      UPDATE b_trip SET bt_km_cost = bt_km_cost + _factor*(bt_km-_from+1)
      WHERE  bt_billing_id = b_id AND bt_car_id = c_id AND bt_km >= _from;

    END LOOP main;

    CLOSE cur;
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
      SELECT DISTINCT bt_user_id FROM b_trip WHERE bt_billing_id = b_id AND NOT bt_privileged AND bt_km_cost > 0
      UNION
      SELECT DISTINCT bf_user_id FROM b_fuel WHERE bt_billing_id = b_id AND NOT bf_privileged;
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
    CALL billing_km_cost (b_id, _id);
    CALL billing_fuel_aux(b_id, _id, lim, _name);
  END LOOP main;

  CLOSE cur;

  CALL billing_user_seq_nr(b_id);

  UPDATE billing SET billing_simulation_date = now() WHERE billing_id = b_id;

  -- TODO: freeze all simulated reservations and trips
  -- TODO: set status in billing
END $$

-- freeze all reservations and trips from the simulations (based on b_trip and b_fuel)
DROP PROCEDURE IF EXISTS billing_freeze_trips $$
CREATE PROCEDURE billing_freeze_trips (b_id INT)
BEGIN
    UPDATE reservations SET reservation_status = 'FROZEN'
    WHERE reservation_id IN
      (SELECT bt_reservation_id FROM b_trip WHERE bt_billing_id = b_id);

    UPDATE reservations SET reservation_status = 'FROZEN'
    WHERE reservation_id IN
      (SELECT bf_reservation_id FROM b_fuel WHERE bf_billing_id = b_id);

    UPDATE refuels SET refuel_status = 'FROZEN'
    WHERE refuel_id IN
      (SELECT bf_refuel_id FROM b_fuel WHERE bf_billing_id = b_id);
END $$

DROP PROCEDURE IF EXISTS billing_user_finalize $$
CREATE PROCEDURE billing_user_finalize (b_id INT)
BEGIN
    -- rerun simulation
    CALL billing_user_simulate (b_id);

    -- change status
    UPDATE billing SET billing_status = 'USERS_DONE', billing_drivers_date=NOW() WHERE billing_id = b_id;

    -- store totals
    UPDATE b_user SET bu_km_cost = (
           SELECT sum(bt_km_cost) FROM b_trip
           WHERE bt_billing_id = b_id AND NOT bt_privileged AND bt_user_id = bu_user_id AND bt_km > 0
    ) WHERE bu_billing_id = b_id;
    UPDATE b_user SET bu_fuel_cost = (
           SELECT sum(bf_fuel_cost) FROM b_fuel
           WHERE bf_billing_id = b_id AND NOT bf_privileged AND bf_user_id = bu_user_id
    ) WHERE bu_billing_id = b_id;
END $$

-- TODO: make sure refuels cannot be added to frozen reservations
-- TODO: check remaining refuels for frozen reservations
-- TODO: billing_trip_aux / billing_fuel_aux for finalizing invoices

-- finalize user billing
--
DELIMITER ;
