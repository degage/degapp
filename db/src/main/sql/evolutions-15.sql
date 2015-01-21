-- car insutance number is string
ALTER TABLE carinsurances
    MODIFY column `insurance_contract_id` VARCHAR(64);

ALTER TABLE carinsurances
    MODIFY column `insurance_bonus_malus` VARCHAR(64);
