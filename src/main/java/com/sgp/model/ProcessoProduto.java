package com.sgp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "processo_produtos")
public class ProcessoProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "data_envio", nullable = false)
    private LocalDate dataEnvio;

    public ProcessoProduto() {}
    public ProcessoProduto(Processo processo, Produto produto, LocalDate dataEnvio) {
        this.processo = processo;
        this.produto = produto;
        this.dataEnvio = dataEnvio;
    }

    // getters e setters...
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
    public Produto getProduto() {
        return produto;
    }
    public void setProduto(Produto produto) {
        this.produto = produto;
    }
    public LocalDate getDataEnvio() {
        return dataEnvio;
    }
    public void setDataEnvio(LocalDate dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    
}
