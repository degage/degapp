-- changed refuel price from

ALTER TABLE refuels ADD COLUMN refuel_eurocents INT;
UPDATE refuels SET refuel_eurocents = floor(100* refuel_amount);
ALTER TABLE refuels DROP COLUMN refuel_amount;