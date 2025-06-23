package com.sgp.model;

import jakarta.persistence.*;

/**
 * Entidade Local com detalhes do fórum
 */
@Entity
@Table(name = "locais")
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String comarca;

    @Column(nullable = false)
    private String especialidade;

    @Column(name = "numero_vara", nullable = false)
    private String numeroVara;

    @Column(nullable = false)
    private Integer codigo;

    @Column(nullable = false)
    private String localizacao;

    @Column(columnDefinition = "TEXT")
    private String obs;

    // Construtor padrão público para JPA
    public Local() { }

    /**
     * Construtor com todos os atributos
     */
    public Local(String comarca, String especialidade, String numeroVara,
                 Integer codigo, String localizacao, String obs) {
        this.comarca = comarca;
        this.especialidade = especialidade;
        this.numeroVara = numeroVara;
        this.codigo = codigo;
        this.localizacao = localizacao;
        this.obs = obs;
    }

    // Getters e setters
    public Long getId() { return id; }

    public String getComarca() { return comarca; }
    public void setComarca(String comarca) { this.comarca = comarca; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public String getNumeroVara() { return numeroVara; }
    public void setNumeroVara(String numeroVara) { this.numeroVara = numeroVara; }

    public Integer getCodigo() { return codigo; }
    public void setCodigo(Integer codigo) { this.codigo = codigo; }

    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }

    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }
}
