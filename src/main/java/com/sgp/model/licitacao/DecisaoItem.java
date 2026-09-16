package com.sgp.model.licitacao;

public enum DecisaoItem {
    PENDENTE("Pendente"), APROVADO("Aprovado"), REPROVADO("Reprovado");
    private final String descricao;
    DecisaoItem(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
