package com.sgp.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "processo_logs",
        indexes = {
                @Index(name = "idx_processo_logs_processo_datahora", columnList = "processo_id, data_hora")
        }
)
public class ProcessoLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento = "EDICAO";

    @Column(name = "usuario")
    private String usuario;

    @Column(nullable = false)
    private String campo;

    @Column(name = "valor_antigo", columnDefinition = "TEXT")
    private String valorAntigo;

    @Column(name = "valor_novo", columnDefinition = "TEXT")
    private String valorNovo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    

    public ProcessoLog() {}

    public ProcessoLog(Processo processo, String campo, String valorAntigo, String valorNovo) {
        this.processo = processo;
        this.tipoEvento = "EDICAO";
        this.campo = campo;
        this.valorAntigo = valorAntigo;
        this.valorNovo = valorNovo;
        this.dataHora = LocalDateTime.now();
    }

    public ProcessoLog(Processo processo,
                       String tipoEvento,
                       String usuario,
                       String campo,
                       String valorAntigo,
                       String valorNovo) {
        this.processo = processo;
        this.tipoEvento = tipoEvento;
        this.usuario = usuario;
        this.campo = campo;
        this.valorAntigo = valorAntigo;
        this.valorNovo = valorNovo;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Processo getProcesso() {
        return processo;
    }

    public void setProcesso(Processo processo) {
        this.processo = processo;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorAntigo() {
        return valorAntigo;
    }

    public void setValorAntigo(String valorAntigo) {
        this.valorAntigo = valorAntigo;
    }

    public String getValorNovo() {
        return valorNovo;
    }

    public void setValorNovo(String valorNovo) {
        this.valorNovo = valorNovo;
    }

    public LocalDateTime getDataAlteracao() {
        return dataHora;
    }

    public void setDataAlteracao(LocalDateTime dataAlteracao) {
        this.dataHora = dataAlteracao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

 
}
