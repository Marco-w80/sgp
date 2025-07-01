package com.sgp.dto;

import com.sgp.model.Endereco;
import com.sgp.model.Sexo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PessoaForm {

    private String tipo;
    private String nome;
    private Sexo sexo;
    private LocalDate dataNascimento;
    private String cpf;
    private String identidade;

    // Campos específicos
    private String crm;
    private String oab;

    // Endereços opcionais
    private List<Endereco> enderecos = new ArrayList<>();

    // Getters e Setters

    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public Sexo getSexo() {
        return sexo;
    }
    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCpf() {
        return cpf;
    }
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getIdentidade() {
        return identidade;
    }
    public void setIdentidade(String identidade) {
        this.identidade = identidade;
    }

    public String getCrm() {
        return crm;
    }
    public void setCrm(String crm) {
        this.crm = crm;
    }

    public String getOab() {
        return oab;
    }
    public void setOab(String oab) {
        this.oab = oab;
    }

    public List<Endereco> getEnderecos() {
        return enderecos;
    }
    public void setEnderecos(List<Endereco> enderecos) {
        this.enderecos = enderecos;
    }
}
