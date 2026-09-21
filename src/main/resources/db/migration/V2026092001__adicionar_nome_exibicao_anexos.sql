ALTER TABLE lic_anexos
    ADD COLUMN nome_exibicao VARCHAR(255);

UPDATE lic_anexos
SET nome_exibicao = nome_original
WHERE nome_exibicao IS NULL OR TRIM(nome_exibicao) = '';

ALTER TABLE lic_anexos
    MODIFY COLUMN nome_exibicao VARCHAR(255) NOT NULL;
