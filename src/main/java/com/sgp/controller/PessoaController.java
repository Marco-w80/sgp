package com.sgp.controller;

import com.sgp.model.*;
import com.sgp.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/pessoas")
public class PessoaController {

    @Autowired
    private PessoaRepository pessoaRepository;

    @GetMapping("/cadastrar")
    public String showForm(Model model) {
        model.addAttribute("tipos", List.of("MEDICO", "ADVOGADO", "PACIENTE"));
        return "pessoas/cadastrar-pessoa";
    }

    @PostMapping("/cadastrar")
    public String create(
            @RequestParam String tipo,
            @RequestParam String nome,
            @RequestParam Sexo sexo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNascimento,
            @RequestParam String cpf,
            @RequestParam String identidade,
            @RequestParam(required = false) String crm,
            @RequestParam(required = false) String oab
    ) {
        Pessoa pessoa;
        switch (tipo) {
            case "MEDICO":
                pessoa = new Medico(nome, sexo, dataNascimento, cpf, identidade, crm);
                break;
            case "ADVOGADO":
                pessoa = new Advogado(nome, sexo, dataNascimento, cpf, identidade, oab);
                break;
            default:
                pessoa = new Paciente(nome, sexo, dataNascimento, cpf, identidade);
        }
        pessoaRepository.save(pessoa);
        return "redirect:/pessoas/listar";
    }

    @GetMapping("/listar")
    public String list(Model model) {
        List<Pessoa> pessoas = pessoaRepository.findAll();
        model.addAttribute("pessoas", pessoas);
        return "pessoas/listar-pessoas";
    }
}