# Installatie database #

Gebruikt mysql server versie 5.7. **Opgelet**, dit is niet de standaardversie die op Debian geïnstalleerd is.

## Aanmaken database ##

Via mysql-client: `mysql -u root -p`
```
CREATE DATABASE IF NOT EXISTS degage
   DEFAULT CHARACTER SET utf8
   DEFAULT COLLATE utf8_general_ci;

CREATE USER 'degage'@'localhost'
   IDENTIFIED BY 'DeGaGe';

GRANT ALL ON degage.* TO 'degage'@'localhost';
GRANT FILE ON *.* TO 'degage'@'localhost';
```

## Create the database tables ##

Via mysql-client, in `sql`-directory
```
mysql degage -u degage -p < create-tables.sql
```

## Fill in with initial data ##
```
mysql degage -u degage -p --local-infile=1 < init.sql
```
## Test database

Via mysql-client: `mysql -u root -p`
```
CREATE DATABASE IF NOT EXISTS degagetest
   DEFAULT CHARACTER SET utf8
   DEFAULT COLLATE utf8_general_ci;

GRANT ALL ON degagetest.* TO 'degage'@'localhost';
```
Via mysql-client, in `sql`-directory
```
mysql degagetest -u degage -p < create-tables.sql
mysql degagetest -u degage -p --local-infile=1 < init.sql
```


