ALTER TABLE carinsurances
ADD COLUMN start_insurance_policy timestamp NULL DEFAULT NULL,
ADD COLUMN start_bonus_malus integer,
ADD COLUMN civil_liability BIT,
ADD COLUMN legal_counsel BIT,
ADD COLUMN driver_insurance BIT,
ADD COLUMN material_damage BIT,
ADD COLUMN value_exclusive_VAT integer,
ADD COLUMN exemption integer,
ADD COLUMN glass_breakage BIT,
ADD COLUMN theft BIT;
