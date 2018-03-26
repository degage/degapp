create table `properties` (
  `property_id` int(11) NOT NULL AUTO_INCREMENT,
  `property_key` varchar(255) NOT NULL,
  `property_value` varchar(1024) NOT NULL,
  `invoice_created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `invoice_updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`property_id`),
  CONSTRAINT `UC_property_key` UNIQUE(`property_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT INTO `properties` (`property_key`, `property_value`) VALUES ('mail_signature_link', '');
INSERT INTO `properties` (`property_key`, `property_value`) VALUES ('mail_signature_image', '');
