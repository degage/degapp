ALTER TABLE technicalcardetails
ADD COLUMN eco_score integer,
ADD COLUMN euro_norm VARCHAR(255),
ADD COLUMN start_date timestamp NULL DEFAULT NULL,
ADD COLUMN kilo_watt integer;