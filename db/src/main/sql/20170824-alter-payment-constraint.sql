DROP INDEX `UC_number_filename` ON payments;

CREATE UNIQUE INDEX `unique_payment`
ON payments(`payment_number`, `payment_filename`, `payment_date`, `payment_account_number`,
`payment_amount`);
