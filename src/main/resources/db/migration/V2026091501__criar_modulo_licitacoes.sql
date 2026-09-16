CREATE TABLE lic_licitacoes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orgao VARCHAR(180) NOT NULL,
    numero_edital VARCHAR(100) NOT NULL,
    uasg VARCHAR(50),
    modalidade VARCHAR(100),
    objeto TEXT NOT NULL,
    data_disputa DATETIME(6) NOT NULL,
    prazo_entrega VARCHAR(150),
    valor_estimado_total DECIMAL(19,2),
    precisa_amostra BIT NOT NULL DEFAULT 0,
    link_edital VARCHAR(1000),
    observacoes TEXT,
    etapa_atual VARCHAR(30) NOT NULL,
    arquivada BIT NOT NULL DEFAULT 0,
    criado_por_id BIGINT,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    legado_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_lic_licitacao_legado UNIQUE (legado_id),
    CONSTRAINT fk_lic_licitacao_usuario FOREIGN KEY (criado_por_id) REFERENCES usuario(id),
    INDEX idx_lic_etapa_arquivada (etapa_atual, arquivada),
    INDEX idx_lic_data_disputa (data_disputa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lic_itens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    licitacao_id BIGINT NOT NULL,
    numero_item INT NOT NULL,
    descricao TEXT NOT NULL,
    quantidade DECIMAL(19,4) NOT NULL,
    unidade VARCHAR(30) NOT NULL,
    valor_referencia DECIMAL(19,4),
    decisao VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    preco_maximo DECIMAL(19,4),
    percentual_desconto DECIMAL(8,4),
    estrategia_lance VARCHAR(500),
    resultado VARCHAR(20),
    preco_final DECIMAL(19,4),
    motivo_perda VARCHAR(1000),
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    legado_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_lic_item_numero UNIQUE (licitacao_id, numero_item),
    CONSTRAINT uk_lic_item_legado UNIQUE (legado_id),
    CONSTRAINT fk_lic_item_licitacao FOREIGN KEY (licitacao_id) REFERENCES lic_licitacoes(id) ON DELETE CASCADE,
    INDEX idx_lic_item_licitacao (licitacao_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lic_fornecedores (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(180) NOT NULL,
    contato VARCHAR(255),
    resumo VARCHAR(1000),
    observacoes TEXT,
    criado_por_id BIGINT,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    legado_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_lic_forn_legado UNIQUE (legado_id),
    CONSTRAINT fk_lic_forn_usuario FOREIGN KEY (criado_por_id) REFERENCES usuario(id),
    INDEX idx_lic_forn_nome (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lic_fornecedor_itens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fornecedor_id BIGINT NOT NULL,
    nome VARCHAR(255) NOT NULL,
    marca VARCHAR(120),
    preco DECIMAL(19,4),
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    legado_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_lic_forn_item_legado UNIQUE (legado_id),
    CONSTRAINT fk_lic_forn_item_forn FOREIGN KEY (fornecedor_id) REFERENCES lic_fornecedores(id) ON DELETE CASCADE,
    INDEX idx_lic_forn_item_fornecedor (fornecedor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lic_cotacoes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    fornecedor_id BIGINT,
    fornecedor_nome VARCHAR(180) NOT NULL,
    valor_unitario DECIMAL(19,4) NOT NULL,
    data_cotacao DATE NOT NULL,
    observacoes TEXT,
    registrado_por_id BIGINT,
    criado_em DATETIME(6) NOT NULL,
    legado_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_lic_cot_legado UNIQUE (legado_id),
    CONSTRAINT fk_lic_cot_item FOREIGN KEY (item_id) REFERENCES lic_itens(id) ON DELETE CASCADE,
    CONSTRAINT fk_lic_cot_forn FOREIGN KEY (fornecedor_id) REFERENCES lic_fornecedores(id) ON DELETE SET NULL,
    CONSTRAINT fk_lic_cot_usuario FOREIGN KEY (registrado_por_id) REFERENCES usuario(id),
    INDEX idx_lic_cot_item (item_id),
    INDEX idx_lic_cot_fornecedor (fornecedor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lic_historicos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    licitacao_id BIGINT NOT NULL,
    etapa_origem VARCHAR(30),
    etapa_destino VARCHAR(30) NOT NULL,
    usuario_id BIGINT,
    data_hora DATETIME(6) NOT NULL,
    observacoes VARCHAR(1000),
    snapshot LONGTEXT,
    legado_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_lic_hist_legado UNIQUE (legado_id),
    CONSTRAINT fk_lic_hist_licitacao FOREIGN KEY (licitacao_id) REFERENCES lic_licitacoes(id) ON DELETE CASCADE,
    CONSTRAINT fk_lic_hist_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    INDEX idx_lic_hist_licitacao_data (licitacao_id, data_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lic_anexos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    licitacao_id BIGINT NOT NULL,
    item_id BIGINT,
    etapa VARCHAR(30),
    nome_original VARCHAR(255) NOT NULL,
    nome_armazenado VARCHAR(255) NOT NULL,
    caminho VARCHAR(1000) NOT NULL,
    mime_type VARCHAR(150) NOT NULL,
    tamanho BIGINT NOT NULL,
    enviado_por_id BIGINT,
    enviado_em DATETIME(6) NOT NULL,
    legado_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_lic_anexo_nome UNIQUE (nome_armazenado),
    CONSTRAINT uk_lic_anexo_legado UNIQUE (legado_id),
    CONSTRAINT fk_lic_anexo_licitacao FOREIGN KEY (licitacao_id) REFERENCES lic_licitacoes(id) ON DELETE CASCADE,
    CONSTRAINT fk_lic_anexo_item FOREIGN KEY (item_id) REFERENCES lic_itens(id) ON DELETE SET NULL,
    CONSTRAINT fk_lic_anexo_usuario FOREIGN KEY (enviado_por_id) REFERENCES usuario(id),
    INDEX idx_lic_anexo_licitacao (licitacao_id),
    INDEX idx_lic_anexo_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lic_portfolio_estoque (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    marca VARCHAR(120),
    fabricante VARCHAR(180),
    quantidade DECIMAL(19,4) NOT NULL DEFAULT 0,
    preco DECIMAL(19,4),
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    legado_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_lic_port_legado UNIQUE (legado_id),
    INDEX idx_lic_port_nome (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
