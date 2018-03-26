DELETE FROM codas;
DELETE FROM payment_invoice;
DELETE FROM payments;

ALTER TABLE `payments` MODIFY `payment_structured_comm` varchar(20) NOT NULL DEFAULT "";
ALTER TABLE `payments` MODIFY `payment_comment` varchar(100) NOT NULL DEFAULT "";

DROP INDEX `unique_payment` ON `payments`;

CREATE UNIQUE INDEX `unique_payment`
ON payments(`payment_filename`, `payment_number`, `payment_structured_comm`, `payment_comment`, `payment_date`, `payment_account_number`,
`payment_amount`);
