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
      SELECT car_id,car_name FROM cars_billed JOIN cars USING(car_id)
      WHERE billing_id = b_id;
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

-- compiles the billing information for the given car
-- b_id: id of billing
-- lim: limit date, only records before this date will be considered
-- c_id: id of car
-- cd_factor: car deprecation factor
-- cd_limit: car_deprec_limit for this car
-- cd_last: car_deprec_last for this car
-- NOTE: uses information from the tables b_trip and b_fuel and b_costs
DROP PROCEDURE IF EXISTS billing_car_aux $$
CREATE PROCEDURE billing_car_aux (b_id INT, c_id INT, lim DATETIME, cd_factor INT, cd_limit INT, cd_last INT)
BEGIN
    DECLARE first_km INT;
    DECLARE last_km INT;

    DECLARE owner_km INT;
    DECLARE fuel_cost INT;
    DECLARE fuel_owner INT;

    DECLARE deprec_km INT;
    DECLARE total_km INT;
    DECLARE deprec_recup INT;
    DECLARE fuel_due INT;

    DECLARE car_cost INT;
    DECLARE car_cost_recup INT;

    SELECT MIN(car_ride_start_km), MAX(car_ride_end_km)
        FROM reservations
        JOIN carrides ON reservation_id = car_ride_car_reservation_id
        WHERE reservation_car_id = c_id AND reservation_from <  lim
            AND NOT reservation_archived
            AND (reservation_status = 'FINISHED' OR reservation_status = 'FROZEN')
    INTO first_km, last_km;

    SELECT IFNULL(SUM(bt_km),0) FROM b_trip
       WHERE bt_billing_id = b_id AND bt_car_id = c_id
    INTO total_km;

    SELECT IFNULL(SUM(bt_km),0) FROM b_trip
       WHERE bt_billing_id = b_id AND bt_car_id = c_id AND bt_privileged
    INTO owner_km;

    SELECT IFNULL(SUM(bf_fuel_cost),0) FROM b_fuel
       WHERE bf_billing_id = b_id AND bf_car_id = c_id
    INTO fuel_cost;

    SELECT IFNULL(SUM(bf_fuel_cost),0) FROM b_fuel
       WHERE bf_billing_id = b_id AND bf_car_id = c_id AND bf_privileged
    INTO fuel_owner;

    SELECT IFNULL(SUM(bcc_refunded),0) FROM b_costs
       JOIN carcosts ON car_cost_id = bcc_cost_id
       WHERE bcc_billing_id = b_id AND car_cost_car_id = c_id
    INTO car_cost;

    SET deprec_km = GREATEST(LEAST(cd_limit, last_km) - cd_last, 0);

    IF deprec_km = 0 THEN
        SET deprec_recup = 0;
    ELSEIF total_km = deprec_km THEN
        SET deprec_recup = cd_factor*(total_km-owner_km)/10;
    ELSE
        SET deprec_recup = cd_factor*deprec_km*(total_km-owner_km)/total_km/10;
    END IF;

    IF fuel_cost = 0 THEN
       SET fuel_due = 0;
    ELSEIF total_km = 0 THEN
       SET fuel_due = 0;
    ELSE
       SET fuel_due = fuel_cost*owner_km / total_km;
    END IF;

    IF car_cost = 0 THEN
       SET car_cost_recup = 0;
    ELSEIF total_km = 0 THEN
       SET car_cost_recup = 0;
    ELSE
       SET car_cost_recup = car_cost*(total_km-owner_km) / total_km;
    END IF;

    INSERT INTO b_cars(bc_billing_id, bc_car_id,
                       bc_first_km, bc_last_km, bc_total_km, bc_owner_km, bc_deprec_km,
                       bc_fuel_total, bc_fuel_owner, bc_fuel_due, bc_deprec_recup,
                       bc_costs, bc_costs_recup
                       )
    VALUES( b_id, c_id, first_km, last_km, total_km, owner_km, deprec_km,
            fuel_cost, fuel_owner, fuel_due, deprec_recup, car_cost, car_cost_recup
    );
END $$

-- add sequence number to car bills
DROP PROCEDURE IF EXISTS billing_car_seq_nr $$
CREATE PROCEDURE billing_car_seq_nr (b_id INT)
BEGIN
  DECLARE seq INT DEFAULT 1;

  -- set up everything for loop
  DECLARE done BOOLEAN DEFAULT FALSE;
  DECLARE _id INT;
  DECLARE cur CURSOR FOR
      SELECT car_id FROM cars_billed WHERE billing_id = b_id;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  -- Set sequence numbers
  OPEN cur;

  main: LOOP
    FETCH cur INTO _id;
    IF done THEN
      LEAVE main;
    END IF;
    UPDATE b_cars SET bc_seq_nr = seq WHERE bc_car_id = _id;
    SET seq = seq + 1;
  END LOOP main;

  CLOSE cur;
END  $$

-- Compute portion of car cost that should be refunded in this period
--
-- b_i billing
-- c_id cost id
-- months number of months since start of cost
-- spread number of months over which to spread the cost
-- amount total cost
DROP PROCEDURE IF EXISTS billing_cost_aux $$
CREATE PROCEDURE billing_cost_aux (b_id INT, c_id INT, months INT, spread INT, amount INT)
BEGIN
  DECLARE refunded INT; -- amount already taken into account
  DECLARE total INT;    -- total amount that must be taken into account

  IF months >= spread THEN
     SET total = amount;
  ELSE
     SET total = amount*months/spread;
  END IF;

  SELECT IFNULL(SUM(bcc_refunded),0) FROM b_costs WHERE bcc_cost_id=c_id AND bcc_billing_id < b_id
  INTO refunded;

  INSERT INTO b_costs(bcc_billing_id,bcc_cost_id,bcc_refunded)
  VALUES (b_id, c_id, total-refunded);
END $$

DROP PROCEDURE IF EXISTS billing_cost $$
CREATE PROCEDURE billing_cost (b_id INT)
BEGIN
  DECLARE lim DATETIME;

  -- set up everything for loop
  DECLARE done BOOLEAN DEFAULT FALSE;
  DECLARE _id INT;
  DECLARE _start DATE;
  DECLARE _spread INT;
  DECLARE _amount INT;

  DECLARE cur CURSOR FOR
      SELECT car_cost_id, car_cost_start, car_cost_spread, car_cost_amount
      FROM cars_billed JOIN carcosts ON car_cost_car_id = car_id
      WHERE billing_id = b_id
        AND NOT car_cost_archived
        AND (car_cost_status='ACCEPTED' OR car_cost_status='FROZEN')
        AND car_cost_start < lim;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  SELECT billing_limit FROM billing WHERE billing_id = b_id INTO lim;

  -- reset
  DELETE FROM b_costs WHERE bcc_billing_id = b_id;

  -- loop
  OPEN cur;

  main: LOOP
    FETCH cur INTO _id, _start, _spread, _amount;
    IF done THEN
      LEAVE main;
    END IF;
    CALL billing_cost_aux(b_id, _id, 12*(YEAR(lim)-YEAR(_start)) + MONTH(lim) - MONTH(_start), _spread, _amount);
  END LOOP main;

END $$

DROP PROCEDURE IF EXISTS billing_car_finalize $$
CREATE PROCEDURE billing_car_finalize (b_id INT)
BEGIN
  DECLARE lim DATETIME;

  -- set up everything for loop
  DECLARE done BOOLEAN DEFAULT FALSE;
  DECLARE _id INT;
  DECLARE _cd_factor INT;
  DECLARE _cd_limit INT;
  DECLARE _cd_last INT;
  DECLARE cur CURSOR FOR
      SELECT car_id,car_deprec,car_deprec_limit,car_deprec_last
        FROM cars_billed JOIN cars USING(car_id)
        WHERE billing_id = b_id;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;


  SELECT billing_limit FROM billing WHERE billing_id = b_id INTO lim;

  -- clear results of earlier runs
  DELETE FROM b_cars WHERE bc_billing_id = b_id;

  -- compute costs
  CALL billing_cost(b_id);

  -- fill in b_cars for every car
  OPEN cur;

  main: LOOP
    FETCH cur INTO _id, _cd_factor, _cd_limit, _cd_last;
    IF done THEN
      LEAVE main;
    END IF;
    CALL billing_car_aux(b_id, _id, lim, _cd_factor, _cd_limit, _cd_last);
  END LOOP main;

  CALL billing_car_seq_nr (b_id);

  UPDATE billing SET billing_status = 'ALL_DONE', billing_owners_date=NOW()
  WHERE billing_id = b_id;

  UPDATE carcosts SET car_cost_status = 'FROZEN'
  WHERE car_cost_id IN
     (SELECT bcc_cost_id FROM b_costs WHERE bcc_billing_id = b_id);

END $$

DROP PROCEDURE IF EXISTS billing_archive $$
CREATE PROCEDURE billing_archive (b_id INT)
BEGIN

  DECLARE lim INT;
  SELECT billing_limit FROM billing WHERE billing_id = b_id INTO lim;

  -- trips: two options TODO
  -- 1. Archive everything that should have been billed, for every car that was billed
  -- UPDATE reservations
  --   JOIN cars_billed ON car_id = reservation_car_id
  --      SET reservation_archived = TRUE
  --   WHERE billing_id = b_id AND reservation_from < lim;
  -- 2. Archive everything that was billed, plus old cancellations
  UPDATE reservations JOIN b_trip ON reservation_id = bt_reservation_id
       SET reservation_archived = TRUE
    WHERE bt_billing_id = b_id;
  UPDATE reservations
       SET reservation_archived = TRUE
       WHERE reservation_from < lim AND reservation_status < 4; -- [ENUM INDEX]

  -- refuels
  UPDATE refuels JOIN b_fuel ON refuel_id = bf_refuel_id
       SET refuel_archived = TRUE
    WHERE bf_billing_id = b_id;
  UPDATE refuels JOIN reservations ON refuel_car_ride_id = reservation_id
       SET refuel_archived = TRUE
       WHERE reservation_from < lim AND refuel_status = 'REFUSED';

  -- save what has already been paid
  UPDATE carcosts
     JOIN ( SELECT SUM(bcc_refunded) AS r, bcc_cost_id FROM b_costs GROUP BY bcc_cost_id
     ) AS tab ON bcc_cost_id = car_cost_id
     SET car_cost_already_paid = r
     WHERE NOT car_cost_archived;

  -- costs that are finished can be archived
  UPDATE carcosts
    SET car_cost_archived = true
    WHERE car_cost_amount <= car_cost_already_paid;
  UPDATE carcosts
    SET car_cost_archived = true
    WHERE car_cost_start < lim AND car_cost_status='REFUSED';

  -- adjust car_deprec_last
  UPDATE cars JOIN b_cars ON car_id = bc_car_id
    SET car_deprec_last = bc_total_km
    WHERE bc_billing_id = b_id;

  -- set status
  UPDATE billing SET billing_status='ARCHIVED' WHERE billing_id = b_id;

END $$

-- TODO: make sure refuels cannot be added to frozen reservations
-- TODO: check remaining refuels for frozen reservations
-- TODO: archiving evrything - freezing costs

-- finalize user billing
--
DELIMITER ;
