-- introduced some on delete cascades
ALTER TABLE idfiles DROP FOREIGN KEY `idfiles_ibfk_2`;
ALTER TABLE idfiles ADD
  FOREIGN KEY (file_id)  REFERENCES files(file_id) ON DELETE CASCADE;

ALTER TABLE licensefiles DROP FOREIGN KEY `licensefiles_ibfk_2`;
ALTER TABLE licensefiles ADD
  FOREIGN KEY (file_id)  REFERENCES files(file_id) ON DELETE CASCADE;
