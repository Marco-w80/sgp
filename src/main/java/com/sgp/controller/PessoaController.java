package com.sgp.controller;

import com.sgp.dto.PessoaForm;
import com.sgp.model.*;
import com.sgp.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
        // 1. Cria o objeto do formulário
        PessoaForm form = new PessoaForm();

        // 2. AQUI ESTÁ A CORREÇÃO!
        // Adicionamos um objeto Endereco novo e vazio à lista de endereços.
        // (Certifique-se de que a classe Endereco está importada na sua controller)
        form.getEnderecos().add(new Endereco());

        // 3. Adiciona o formulário (agora com um endereço) ao modelo
        model.addAttribute("pessoaForm", form);
        model.addAttribute("tipos", List.of("MEDICO", "ADVOGADO", "PACIENTE"));

        return "pessoas/cadastrar-pessoa";
    }

    @PostMapping("/cadastrar")
    public String create(@ModelAttribute PessoaForm form) {
        Pessoa pessoa;
        switch (form.getTipo()) {
            case "MEDICO":
                pessoa = new Medico(form.getNome(), form.getSexo(), form.getDataNascimento(),
                        form.getCpf(), form.getIdentidade(), form.getCrm());
                break;
            case "ADVOGADO":
                pessoa = new Advogado(form.getNome(), form.getSexo(), form.getDataNascimento(),
                        form.getCpf(), form.getIdentidade(), form.getOab());
                break;
            default:
                pessoa = new Paciente(form.getNome(), form.getSexo(), form.getDataNascimento(),
                        form.getCpf(), form.getIdentidade());
        }

        if (form.getEnderecos() != null) {
            for (Endereco e : form.getEnderecos()) {
                e.setPessoa(pessoa);
                pessoa.getEnderecos().add(e);
            }
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

    @GetMapping("/editar/{id}")
    public String showEditForm(@PathVariable Long id, @RequestParam(required = false) String error, Model model) {
        Pessoa pessoa = pessoaRepository.findById(id).orElseThrow();

        PessoaForm form = new PessoaForm();
        form.setTipo(pessoa.getTipo());
        form.setNome(pessoa.getNome());
        form.setSexo(pessoa.getSexo());
        form.setDataNascimento(pessoa.getDataNascimento());
        form.setCpf(pessoa.getCpf());
        form.setIdentidade(pessoa.getIdentidade());
        form.setEnderecos(pessoa.getEnderecos());

        if (pessoa instanceof Medico) {
            form.setCrm(((Medico) pessoa).getCrm());
        } else if (pessoa instanceof Advogado) {
            form.setOab(((Advogado) pessoa).getOab());
        }

        form.setId(pessoa.getId()); // você deve adicionar o campo `id` ao DTO PessoaForm

        model.addAttribute("pessoaForm", form);
        model.addAttribute("tipos", List.of("MEDICO", "ADVOGADO", "PACIENTE"));
        model.addAttribute("error", error);

        return "pessoas/editar-pessoa";
    }

    @PostMapping("/editar/{id}")
    public String update(@PathVariable Long id, @ModelAttribute PessoaForm form) {
        Pessoa pessoa = pessoaRepository.findById(id).orElseThrow();

        // Atualiza campos comuns
        pessoa.setNome(form.getNome());
        pessoa.setSexo(form.getSexo());
        pessoa.setDataNascimento(form.getDataNascimento());
        pessoa.setCpf(form.getCpf());
        pessoa.setIdentidade(form.getIdentidade());

        // Atualiza campos específicos
        if (pessoa instanceof Medico) {
            ((Medico) pessoa).setCrm(form.getCrm());
        } else if (pessoa instanceof Advogado) {
            ((Advogado) pessoa).setOab(form.getOab());
        }

        // Atualiza endereços
        pessoa.getEnderecos().clear(); // remove os antigos
        if (form.getEnderecos() != null) {
            for (Endereco e : form.getEnderecos()) {
                e.setPessoa(pessoa);
                pessoa.getEnderecos().add(e);
            }
        }

        try {
            pessoaRepository.save(pessoa);
        } catch (DataIntegrityViolationException ex) {
            return "redirect:/pessoas/editar/" + id + "?error=CPF já cadastrado";
        }

        return "redirect:/pessoas/listar";
    }

}