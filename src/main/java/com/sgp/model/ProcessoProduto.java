package com.sgp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "processo_produtos")
public class ProcessoProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relação com Processo
    @ManyToOne(optional = false)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    // Relação com Produto
    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    // Data de envio do medicamento
    @Column(name = "data_envio", nullable = false)
    private LocalDate dataEnvio;

    // Quantidade enviada
    @Column(nullable = false)
    private Integer quantidade;

    public ProcessoProduto() {
    }

    /**
     * Construtor principal, recebe processo, produto, data de envio e quantidade
     */
    public ProcessoProduto(Processo processo,
                           Produto produto,
                           LocalDate dataEnvio,
                           Integer quantidade) {
        this.processo   = processo;
        this.produto    = produto;
        this.dataEnvio  = dataEnvio;
        this.quantidade = quantidade;
    }

    /**
     * Construtor de conveniência: quantidade padrão = 1
     */
    public ProcessoProduto(Processo processo,
                           Produto produto,
                           LocalDate dataEnvio) {
        this(processo, produto, dataEnvio, 1);
    }

    // ===== Getters & Setters =====

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

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
