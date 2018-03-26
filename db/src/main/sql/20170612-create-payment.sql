create table `payments` (
  `payment_id` int(11) NOT NULL AUTO_INCREMENT,
  `payment_number` int(11) NOT NULL,
  `payment_date` timestamp NOT NULL,
  `payment_account_number` varchar(50) NOT NULL,
  `payment_user_id` int(11) DEFAULT NULL,
  `payment_name` varchar(100) NOT NULL,
  `payment_address` text NULL DEFAULT NULL,
  `payment_bank` varchar(20) NOT NULL,
  `payment_amount` decimal(10,2) NOT NULL,
  `payment_comment` varchar(100) DEFAULT NULL,
  `payment_structured_comm` varchar(20) DEFAULT NULL,
  `payment_currency` varchar(20) NOT NULL DEFAULT "EUR",
  `payment_status` enum('OK','CHANGE','UNASSIGNED') NOT NULL DEFAULT 'CHANGE',
  `payment_filename` varchar(64) NULL,
  PRIMARY KEY (`payment_id`),
  CONSTRAINT `UC_number_filename` UNIQUE(`payment_number`, `payment_filename`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
