package com.sgp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("MEDICO")
public class Medico extends Pessoa {

    @Column(name = "crm", unique = true, nullable = true)
    private String crm;

    protected Medico() { super(); }

    public Medico(String nome, Sexo sexo, LocalDate dataNascimento, String cpf, String identidade, String crm) {
        super(nome, sexo, dataNascimento, cpf, identidade);
        this.crm = crm;
    }

    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }
}
