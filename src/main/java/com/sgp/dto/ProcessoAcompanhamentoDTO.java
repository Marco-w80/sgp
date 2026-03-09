package com.sgp.dto;

import java.time.LocalDateTime;

public class ProcessoAcompanhamentoDTO {

    private Long processoId;
    private String nomePessoa;
    private LocalDateTime ultimoAcesso;
    private String ultimoUsuarioAcesso;
    private LocalDateTime ultimaEdicao;
    private String ultimoUsuarioEdicao;
    private Long diasSemAcesso;
    private Long diasSemEdicao;

    public ProcessoAcompanhamentoDTO(Long processoId,
                                     String nomePessoa,
                                     LocalDateTime ultimoAcesso,
                                     String ultimoUsuarioAcesso,
                                     LocalDateTime ultimaEdicao,
                                     String ultimoUsuarioEdicao,
                                     Number diasSemAcesso,
                                     Number diasSemEdicao) {
        this.processoId = processoId;
        this.nomePessoa = nomePessoa;
        this.ultimoAcesso = ultimoAcesso;
        this.ultimoUsuarioAcesso = ultimoUsuarioAcesso;
        this.ultimaEdicao = ultimaEdicao;
        this.ultimoUsuarioEdicao = ultimoUsuarioEdicao;
        this.diasSemAcesso = diasSemAcesso != null ? diasSemAcesso.longValue() : null;
        this.diasSemEdicao = diasSemEdicao != null ? diasSemEdicao.longValue() : null;
    }

    public ProcessoAcompanhamentoDTO(Long processoId,
                                     String nomePessoa,
                                     LocalDateTime ultimoAcesso,
                                     String ultimoUsuarioAcesso,
                                     LocalDateTime ultimaEdicao,
                                     String ultimoUsuarioEdicao,
                                     Integer diasSemAcesso,
                                     Integer diasSemEdicao) {
        this(processoId,
                nomePessoa,
                ultimoAcesso,
                ultimoUsuarioAcesso,
                ultimaEdicao,
                ultimoUsuarioEdicao,
                (Number) diasSemAcesso,
                (Number) diasSemEdicao);
    }

    public ProcessoAcompanhamentoDTO(Long processoId,
                                     String nomePessoa,
                                     LocalDateTime ultimoAcesso,
                                     String ultimoUsuarioAcesso,
                                     LocalDateTime ultimaEdicao,
                                     String ultimoUsuarioEdicao,
                                     Long diasSemAcesso,
                                     Long diasSemEdicao) {
        this(processoId,
                nomePessoa,
                ultimoAcesso,
                ultimoUsuarioAcesso,
                ultimaEdicao,
                ultimoUsuarioEdicao,
                (Number) diasSemAcesso,
                (Number) diasSemEdicao);
    }

    public Long getProcessoId() {
        return processoId;
    }

    public String getNomePessoa() {
        return nomePessoa;
    }

    public LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    public String getUltimoUsuarioAcesso() {
        return ultimoUsuarioAcesso;
    }

    public LocalDateTime getUltimaEdicao() {
        return ultimaEdicao;
    }

    public String getUltimoUsuarioEdicao() {
        return ultimoUsuarioEdicao;
    }

    public Long getDiasSemAcesso() {
        return diasSemAcesso;
    }

    public Long getDiasSemEdicao() {
        return diasSemEdicao;
    }
}
