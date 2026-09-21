ALTER TABLE lic_fornecedores
    ADD COLUMN cnpj VARCHAR(14);

ALTER TABLE lic_fornecedores
    ADD CONSTRAINT uk_lic_forn_cnpj UNIQUE (cnpj);
