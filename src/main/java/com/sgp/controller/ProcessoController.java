package com.sgp.controller;

import com.sgp.model.*;
import com.sgp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/processos")
public class ProcessoController {

    @Autowired
    private ProcessoRepository processoRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private LocalRepository localRepository;

    @GetMapping("/cadastrar")
    public String showCreateForm(Model model) {
        List<Paciente> pacientes = pessoaRepository.findAll().stream()
            .filter(p -> p instanceof Paciente)
            .map(p -> (Paciente)p)
            .collect(Collectors.toList());
        List<Advogado> advogados = pessoaRepository.findAll().stream()
            .filter(p -> p instanceof Advogado)
            .map(p -> (Advogado)p)
            .collect(Collectors.toList());
        List<Medico> medicos = pessoaRepository.findAll().stream()
            .filter(p -> p instanceof Medico)
            .map(p -> (Medico)p)
            .collect(Collectors.toList());

        model.addAttribute("pacientes", pacientes);
        model.addAttribute("advogados", advogados);
        model.addAttribute("medicos", medicos);
        model.addAttribute("locais", localRepository.findAll());
        model.addAttribute("produtos", produtoRepository.findAll());
        model.addAttribute("statusValues", StatusProcesso.values());
        return "processos/cadastrar-processo";
    }

    @PostMapping("/cadastrar")
    public String create(
        @RequestParam String numeroInterno,
        @RequestParam String numeroProcesso,
        @RequestParam Long pacienteId,
        @RequestParam Long advogadoId,
        @RequestParam Long medicoId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @RequestParam StatusProcesso status,
        @RequestParam Long localId,
        @RequestParam(required = false) List<Long> produtoIds,
        @RequestParam(required = false) String obs
    ) {
        Processo proc = new Processo();
        proc.setNumeroInterno(numeroInterno);
        proc.setNumeroProcesso(numeroProcesso);
        proc.setPaciente((Paciente)pessoaRepository.findById(pacienteId).orElseThrow());
        proc.setAdvogado((Advogado)pessoaRepository.findById(advogadoId).orElseThrow());
        proc.setMedico((Medico)pessoaRepository.findById(medicoId).orElseThrow());
        proc.setDataInicio(dataInicio);
        proc.setStatus(status);
        proc.setLocal(localRepository.findById(localId).orElseThrow());
        if (produtoIds != null) {
            Set<Produto> produtos = produtoRepository.findAllById(produtoIds)
                                                   .stream().collect(Collectors.toSet());
            proc.setProdutos(produtos);
        }
        proc.setObs(obs);

        processoRepository.save(proc);
        return "redirect:/processos/listar";
    }

    @GetMapping("/listar")
    public String list(Model model) {
        model.addAttribute("processos", processoRepository.findAll());
        return "processos/listar-processos";
    }

    @GetMapping("/editar/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Processo proc = processoRepository.findById(id).orElseThrow();
        model.addAttribute("processo", proc);
        // mesmo carregamento de listas
        model.addAttribute("pacientes", pessoaRepository.findAll().stream()
            .filter(p -> p instanceof Paciente).map(p->(Paciente)p).collect(Collectors.toList()));
        model.addAttribute("advogados", pessoaRepository.findAll().stream()
            .filter(p -> p instanceof Advogado).map(p->(Advogado)p).collect(Collectors.toList()));
        model.addAttribute("medicos", pessoaRepository.findAll().stream()
            .filter(p -> p instanceof Medico).map(p->(Medico)p).collect(Collectors.toList()));
        model.addAttribute("locais", localRepository.findAll());
        model.addAttribute("produtos", produtoRepository.findAll());
        model.addAttribute("statusValues", StatusProcesso.values());
        return "processos/editar-processo";
    }

    @PostMapping("/editar/{id}")
    public String update(
        @PathVariable Long id,
        @RequestParam String numeroInterno,
        @RequestParam String numeroProcesso,
        @RequestParam Long pacienteId,
        @RequestParam Long advogadoId,
        @RequestParam Long medicoId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @RequestParam StatusProcesso status,
        @RequestParam Long localId,
        @RequestParam(required = false) List<Long> produtoIds,
        @RequestParam(required = false) String obs
    ) {
        Processo proc = processoRepository.findById(id).orElseThrow();
        proc.setNumeroInterno(numeroInterno);
        proc.setNumeroProcesso(numeroProcesso);
        proc.setPaciente((Paciente)pessoaRepository.findById(pacienteId).orElseThrow());
        proc.setAdvogado((Advogado)pessoaRepository.findById(advogadoId).orElseThrow());
        proc.setMedico((Medico)pessoaRepository.findById(medicoId).orElseThrow());
        proc.setDataInicio(dataInicio);
        proc.setStatus(status);
        proc.setLocal(localRepository.findById(localId).orElseThrow());
        if (produtoIds != null) {
            proc.setProdutos(produtoRepository.findAllById(produtoIds)
                                               .stream().collect(Collectors.toSet()));
        } else {
            proc.getProdutos().clear();
        }
        proc.setObs(obs);

        processoRepository.save(proc);
        return "redirect:/processos/listar";
    }
}
