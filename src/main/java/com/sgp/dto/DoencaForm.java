package com.sgp.dto;

public class DoencaForm {
    private String nome; // nome da doença
    private Long grupoId; // grupo já existente
    private String novoGrupo; // opcional: novo grupo de doença

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Long getGrupoId() { return grupoId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }

    public String getNovoGrupo() { return novoGrupo; }
    public void setNovoGrupo(String novoGrupo) { this.novoGrupo = novoGrupo; }
}