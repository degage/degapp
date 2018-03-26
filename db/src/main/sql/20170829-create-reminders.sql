ALTER TABLE `users`
ADD COLUMN `user_send_reminder` BIT DEFAULT 1;

CREATE TABLE `reminders` (
  `reminder_id` int(11) NOT NULL AUTO_INCREMENT,
  `reminder_date` timestamp NOT NULL,
  `reminder_description` TEXT DEFAULT NULL,
  `reminder_invoice_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`reminder_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
