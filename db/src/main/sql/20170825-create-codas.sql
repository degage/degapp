create table `codas` (
  `coda_id` int(11) NOT NULL AUTO_INCREMENT,
  `coda_date` timestamp NOT NULL,
  `coda_filename` varchar(64) NULL,
  `coda_user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`coda_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
