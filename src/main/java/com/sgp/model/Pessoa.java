package com.sgp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pessoas")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_pessoa", discriminatorType = DiscriminatorType.STRING)
public abstract class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sexo sexo;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(length = 14, unique = true, nullable = false)
    private String cpf;

    @Column(nullable = false)
    private String identidade;

    // Construtor padrão para JPA
    protected Pessoa() {}

    // Construtor para uso em código
    protected Pessoa(String nome, Sexo sexo, LocalDate dataNascimento, String cpf, String identidade) {
        this.nome = nome;
        this.sexo = sexo;
        this.dataNascimento = dataNascimento;
        this.cpf = cpf;
        this.identidade = identidade;
    }

    // Getters e setters padrão
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getIdentidade() { return identidade; }
    public void setIdentidade(String identidade) { this.identidade = identidade; }

    /**
     * Retorna o tipo da pessoa (MEDICO, ADVOGADO ou PACIENTE)
     */
    @Transient
    public String getTipo() {
        return this.getClass().getSimpleName().toUpperCase();
    }

    /**
     * Retorna o documento específico (CRM, OAB ou "-")
     */
    @Transient
    public String getDocumentoEspecial() {
        if (this instanceof Medico) {
            return ((Medico) this).getCrm();
        } else if (this instanceof Advogado) {
            return ((Advogado) this).getOab();
        }
        return "-";
    }
}