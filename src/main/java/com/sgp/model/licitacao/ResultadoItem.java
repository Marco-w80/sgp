package com.sgp.model.licitacao;

public enum ResultadoItem {
    GANHO("Ganho"), PERDIDO("Perdido"), CANCELADO("Cancelado");
    private final String descricao;
    ResultadoItem(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
