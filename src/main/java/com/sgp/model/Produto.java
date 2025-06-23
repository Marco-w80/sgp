package com.sgp.model;

import java.time.LocalDate;
import jakarta.persistence.*;


@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(name = "nome_item", nullable = false)
    private String nomeItem;

    @Column(name = "unidade_medida", nullable = false)
    private String unidadeMedida;

    @Column(name = "sigla_unidade", nullable = false)
    private String siglaUnidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrupoProduto grupo;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    @Column(columnDefinition = "TEXT")
    private String especificacoes;

    private String fabricante;
    private String modelo;

    // Construtor padrão público
    public Produto() { }

    public Produto(String codigo, String nomeItem, String unidadeMedida, String siglaUnidade,
                GrupoProduto grupo, LocalDate dataCadastro) {
        this.codigo = codigo;
        this.nomeItem = nomeItem;
        this.unidadeMedida = unidadeMedida;
        this.siglaUnidade = siglaUnidade;
        this.grupo = grupo;
        this.dataCadastro = dataCadastro;
    }

    // Getters e setters
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNomeItem() { return nomeItem; }
    public void setNomeItem(String nomeItem) { this.nomeItem = nomeItem; }
    public String getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }
    public String getSiglaUnidade() { return siglaUnidade; }
    public void setSiglaUnidade(String siglaUnidade) { this.siglaUnidade = siglaUnidade; }
    public GrupoProduto getGrupo() { return grupo; }
    public void setGrupo(GrupoProduto grupo) { this.grupo = grupo; }
    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }
    public String getEspecificacoes() { return especificacoes; }
    public void setEspecificacoes(String especificacoes) { this.especificacoes = especificacoes; }
    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
}
