DROP table payment_invoice;
DROP table payments;

create table `payments` (
  `payment_id` int(11) NOT NULL AUTO_INCREMENT,
  `payment_number` int(11) NOT NULL,
  `payment_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
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
  `payment_debit_type` enum('DEBIT','CREDIT') NOT NULL DEFAULT 'CREDIT',
  `payment_filename` varchar(64) NULL,
  `payment_current_hash` int(11),
  `payment_previous_hash` int(11),
  `payment_next_hash` int(11),
  `payment_created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `payment_updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`payment_id`),
  CONSTRAINT `UC_number_filename` UNIQUE(`payment_previous_hash`, `payment_current_hash`, `payment_next_hash`, `payment_filename`, `payment_date`, `payment_account_number`,
`payment_amount`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

ALTER TABLE `payments` ALTER COLUMN `payment_date` DROP DEFAULT;

create table `payment_invoice` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `payment_id` int(11) NOT NULL,
  `invoice_id` int(11) NOT NULL,
  `payment_created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `payment_updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_payment` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`payment_id`),
  CONSTRAINT `FK_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`invoice_id`),
  CONSTRAINT `UC_payment_invoice` UNIQUE(`payment_id`, `invoice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
