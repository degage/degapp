ALTER TABLE addresses ADD `address_latitude` FLOAT( 10, 6 );
ALTER TABLE addresses ADD `address_longitude` FLOAT( 10, 6 );

DROP VIEW `infosessions_extended`;
CREATE VIEW `infosessions_extended` AS
    SELECT
        infosession_id, infosession_type,
        infosession_timestamp, infosession_max_enrollees, infosession_comments,
        address_id, address_country, address_city, address_zipcode, address_street, address_number, address_latitude, address_longitude,
        user_id, user_firstname, user_lastname, user_phone, user_email, user_status, user_cellphone, user_degage_id,
        count(infosession_enrollee_id) AS enrollee_count
    FROM infosessions AS ses
        JOIN users ON infosession_host_user_id = user_id
        JOIN addresses ON infosession_address_id = address_id
        LEFT JOIN infosessionenrollees USING (infosession_id)
    GROUP BY infosession_id;