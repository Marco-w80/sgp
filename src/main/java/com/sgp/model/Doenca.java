package com.sgp.model;

import jakarta.persistence.*;

@Entity
public class Doenca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private GrupoDoenca grupo;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public GrupoDoenca getGrupo() { return grupo; }
    public void setGrupo(GrupoDoenca grupo) { this.grupo = grupo; }
}
