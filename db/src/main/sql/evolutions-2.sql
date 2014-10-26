-- New way to represent car availabilities

ALTER TABLE caravailabilities ADD COLUMN car_availability_start INT NOT NULL;
ALTER TABLE caravailabilities ADD COLUMN car_availability_end   INT NOT NULL;

UPDATE caravailabilities
   SET car_availability_start =
       TIME_TO_SEC(car_availability_begin_time) + (car_availability_begin_day_of_week-1)*86400;

UPDATE caravailabilities
   SET car_availability_end =
       TIME_TO_SEC(car_availability_end_time) + (car_availability_end_day_of_week-1)*86400;

ALTER TABLE caravailabilities DROP COLUMN car_availability_begin_time;
ALTER TABLE caravailabilities DROP COLUMN car_availability_end_time;
ALTER TABLE caravailabilities DROP COLUMN car_availability_begin_day_of_week;
ALTER TABLE caravailabilities DROP COLUMN car_availability_end_day_of_week;
