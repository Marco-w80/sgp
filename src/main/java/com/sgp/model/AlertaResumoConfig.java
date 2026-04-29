package com.sgp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerta_resumo_config")
public class AlertaResumoConfig {

    @Id
    private Long id = 1L;

    @Column(name = "emails_destino", nullable = false, columnDefinition = "TEXT")
    private String emailsDestino = "";

    @Column(name = "dias_sem_acesso", nullable = false)
    private Integer diasSemAcesso = 10;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "enviar_sem_resultados", nullable = false)
    private boolean enviarSemResultados = true;

    @Column(name = "horario_envio", nullable = false, length = 5)
    private String horarioEnvio = "09:00";

    @Column(name = "ultima_execucao_em")
    private LocalDateTime ultimaExecucaoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailsDestino() {
        return emailsDestino;
    }

    public void setEmailsDestino(String emailsDestino) {
        this.emailsDestino = emailsDestino;
    }

    public Integer getDiasSemAcesso() {
        return diasSemAcesso;
    }

    public void setDiasSemAcesso(Integer diasSemAcesso) {
        this.diasSemAcesso = diasSemAcesso;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isEnviarSemResultados() {
        return enviarSemResultados;
    }

    public void setEnviarSemResultados(boolean enviarSemResultados) {
        this.enviarSemResultados = enviarSemResultados;
    }

    public String getHorarioEnvio() {
        return horarioEnvio;
    }

    public void setHorarioEnvio(String horarioEnvio) {
        this.horarioEnvio = horarioEnvio;
    }

    public LocalDateTime getUltimaExecucaoEm() {
        return ultimaExecucaoEm;
    }

    public void setUltimaExecucaoEm(LocalDateTime ultimaExecucaoEm) {
        this.ultimaExecucaoEm = ultimaExecucaoEm;
    }
}
