-- changes to address table

ALTER TABLE addresses DROP INDEX address_place_zip;

ALTER TABLE addresses ADD COLUMN address_number VARCHAR(12) NOT NULL DEFAULT '';
UPDATE addresses SET address_number = address_street_number + address_street_bus;
ALTER TABLE addresses DROP COLUMN address_street_number;
ALTER TABLE addresses DROP COLUMN address_street_bus;

DROP VIEW `infosessions_extended`;

CREATE VIEW `infosessions_extended` AS
               SELECT
                   infosession_id, infosession_type, infosession_type_alternative,
                   infosession_timestamp, infosession_max_enrollees, infosession_comments,
                   address_id, address_country, address_city, address_zipcode, address_street, address_number,
                   user_id, user_firstname, user_lastname, user_phone, user_email, user_status, user_cellphone,
                   count(infosession_enrollee_id) AS enrollee_count
               FROM infosessions AS ses
                   JOIN users ON infosession_host_user_id = user_id
                   JOIN addresses ON infosession_address_id = address_id
                   LEFT JOIN infosessionenrollees USING (infosession_id)
               GROUP BY infosession_id;
