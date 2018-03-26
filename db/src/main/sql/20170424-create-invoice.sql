create table `invoices` (
  `invoice_id` int(11) NOT NULL AUTO_INCREMENT,
  `invoice_number` varchar(20) NOT NULL,
  `invoice_date` timestamp NULL DEFAULT NULL,
  `invoice_payment_date` timestamp NULL DEFAULT NULL,
  `invoice_due_date` timestamp NULL DEFAULT NULL,
  `invoice_user_id` int(11) NOT NULL,
  `invoice_billing_id` int(11) NOT NULL,
  `invoice_amount` decimal(10,2) DEFAULT 0,
  `invoice_comment` text DEFAULT NULL,
  `invoice_status` enum('OPEN','PAID','OVERDUE') NOT NULL DEFAULT 'OPEN',
  `invoice_structured_comm` varchar(20) DEFAULT NULL,
  `invoice_created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `invoice_updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`invoice_id`),
  UNIQUE KEY `invoice_number` (`invoice_number`),
  CONSTRAINT `FK_invoice_user` FOREIGN KEY (`invoice_user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FK_invoice_billing` FOREIGN KEY (`invoice_billing_id`) REFERENCES `billing` (`billing_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
