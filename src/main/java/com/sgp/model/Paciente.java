package com.sgp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("PACIENTE")
public class Paciente extends Pessoa {

    protected Paciente() { super(); }

    public Paciente(String nome, Sexo sexo, LocalDate dataNascimento, String cpf, String identidade) {
        super(nome, sexo, dataNascimento, cpf, identidade);
    }
}