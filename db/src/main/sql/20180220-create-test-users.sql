-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!
-- DO NOT USE IN PRODUCTION !!!!!!!!!!


update users set user_email = concat('user_',user_id), user_firstname = concat('first_',user_id), user_lastname = concat('last_',user_id),
 user_cellphone = '123456789', user_phone = '123456789', user_vat = 'BE123456789', user_driver_license_id = null, user_identity_card_id = null, 
 user_credit_status = 'REGULAR', user_accountnumber = 'BE1234567890'
 where user_id != 1389 and user_id != 73;
update addresses set address_street = 'Coupure Links', address_number = concat(address_id), address_zipcode = '9000';
update users set user_email = 'hogent_admin@ugent.be', user_firstname='admin', user_lastname='hogent', 
  user_password='$2a$12$Bc9Gz9gl8nxepOV2ytMEL.Duk1P7838fYGdAKsKoY7ibCevI0wqbi' where user_id = 226;
update users set user_email = 'hogent_car_user@ugent.be', user_firstname='car_user', user_lastname='hogent', 
  user_password='$2a$12$0UsXCwPt6/KURF1Xy/irWe7Gg/IIVKC5u2QQoDGaq8D8FibR8DVgG' where user_id = 227;
update users set user_email = 'hogent_car_owner@ugent.be', user_firstname='car_owner', user_lastname='hogent', 
  user_password='$2a$12$A6tN6h4yNKp8VszRrkD3..siY28pge/evNfZbfGYpYtssdHt1PS.u' where user_id = 228;
insert into userroles values(228, 'CAR_OWNER');
insert into userroles values(228, 'CAR_USER');
insert into userroles values(227, 'CAR_USER');
insert into userroles values(226, 'CAR_ADMIN');
insert into userroles values(226, 'RESERVATION_ADMIN');
insert into userroles values(226, 'INVOICE_ADMIN');