ALTER TABLE lic_itens
    ADD COLUMN selecionado_cotacao BIT NOT NULL DEFAULT 1 AFTER valor_referencia;
