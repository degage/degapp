ALTER TABLE invoices
ADD column `invoice_type` varchar(60) DEFAULT 'CAR_USER';

UPDATE invoices set `invoice_type` = 'CAR_USER' where invoice_number like 'A%';
UPDATE invoices set `invoice_type` = 'CAR_OWNER' where invoice_number like 'E%';