-- Initializes the tables with certain values

-- Tables must already have been created with create-tables.sql

-- administrator
-- initial password is 'opensesame'
INSERT INTO `users` (`user_email`,`user_password`,`user_firstname`,`user_lastname`, `user_status`)
    VALUES ("sysadmin@degage.be","$2a$12$yxnPiLMK9/AgAf9T.xpRlOLySyKXHZwYMREm3R8cDc8TekQTP5HBy","Sys","Admin","REGISTERED");
INSERT INTO `userroles` (`userrole_userid`, `userrole_role`)
    VALUES(LAST_INSERT_ID(), 'SUPER_USER');

-- settings
INSERT INTO `settings` (`setting_name`,`setting_value`) VALUES
        ("show_profile","true"),
        ("show_maps","false"),
        ("scheduler_interval","300"),
        ("infosession_reminder","1440"),
        ("reservation_auto_accept","4320"), -- Minutes before reservation is automatically accepted
        ("maps_tile_server","http://tile.openstreetmap.org/%d/%d/%d.png");

-- categories
INSERT INTO  `carcostcategories` (category_id, category_description) VALUES
        (1, "Onderhoud / herstellingen"),
        (2, "Verzekering"),
        (3, "Keuring"),
        (4, "Pechverhelping"),
        (5, "Schade"),
        (6, "Andere");
