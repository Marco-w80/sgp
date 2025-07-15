package com.sgp.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processo_logs")
public class ProcessoLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    @Column(nullable = false)
    private String campo;

    @Column(name = "valor_antigo", columnDefinition = "TEXT")
    private String valorAntigo;

    @Column(name = "valor_novo", columnDefinition = "TEXT")
    private String valorNovo;

    @Column(nullable = false)
    private LocalDateTime dataAlteracao;

    

    public ProcessoLog() {}

    public ProcessoLog(Processo processo, String campo, String valorAntigo, String valorNovo) {
        this.processo = processo;
        this.campo = campo;
        this.valorAntigo = valorAntigo;
        this.valorNovo = valorNovo;
        this.dataAlteracao = LocalDateTime.now();
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
        return dataAlteracao;
    }

    public void setDataAlteracao(LocalDateTime dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

 
}
