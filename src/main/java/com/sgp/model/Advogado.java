package com.sgp.model;

import jakarta.persistence.*;
import java.time.LocalDate;


@Entity
@DiscriminatorValue("ADVOGADO")
public class Advogado extends Pessoa {

    @Column(name = "oab", unique = true, nullable = true)
    private String oab;

    protected Advogado() { super(); }

    public Advogado(String nome, Sexo sexo, LocalDate dataNascimento, String cpf, String identidade, String oab) {
        super(nome, sexo, dataNascimento, cpf, identidade);
        this.oab = oab;
    }

    public String getOab() { return oab; }
    public void setOab(String oab) { this.oab = oab; }
}