-- Initializes the tables with certain values

-- Tables must already have been created with create-tables.sql

load data local infile 'settings.txt'
   into table settings
   fields terminated by ':';

source init-users.sql;
source init-userroles.sql;
