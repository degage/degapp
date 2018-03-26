ALTER TABLE users
ADD column user_license_expiration timestamp NULL DEFAULT NULL,
ADD column user_id_expiration timestamp NULL DEFAULT NULL;
