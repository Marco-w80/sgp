package com.sgp.model.licitacao;

import java.util.List;

public enum EtapaLicitacao {
    CADASTRO("Cadastro"),
    COTACAO("Cotação"),
    APROVACAO("Aprovação"),
    DEFINICAO("Definição"),
    PARTICIPACAO("Participação");

    private static final List<EtapaLicitacao> FLUXO = List.of(values());
    private final String descricao;

    EtapaLicitacao(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
    public EtapaLicitacao proxima() { return ordinal() == FLUXO.size() - 1 ? null : FLUXO.get(ordinal() + 1); }
    public EtapaLicitacao anterior() { return ordinal() == 0 ? null : FLUXO.get(ordinal() - 1); }
    public boolean adjacenteA(EtapaLicitacao outra) { return outra != null && Math.abs(ordinal() - outra.ordinal()) == 1; }
}
