package com.sgp.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "deferimentos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_deferimento_processo_numero", columnNames = {"processo_id", "numero_deferimento"})
        }
)
public class Deferimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;

    @Column(name = "numero_deferimento", nullable = false)
    private Integer numeroDeferimento;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDeferimento tipo;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    @Column(name = "data_deferimento")
    private LocalDate dataDeferimento;

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

    public Integer getNumeroDeferimento() {
        return numeroDeferimento;
    }

    public void setNumeroDeferimento(Integer numeroDeferimento) {
        this.numeroDeferimento = numeroDeferimento;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public TipoDeferimento getTipo() {
        return tipo;
    }

    public void setTipo(TipoDeferimento tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public LocalDate getDataDeferimento() {
        return dataDeferimento;
    }

    public void setDataDeferimento(LocalDate dataDeferimento) {
        this.dataDeferimento = dataDeferimento;
    }
}
