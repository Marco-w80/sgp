package com.sgp.controller;

import com.sgp.model.Local;
import com.sgp.repository.LocalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/locais")
public class LocalController {

    @Autowired
    private LocalRepository localRepository;

    @GetMapping("/cadastrar")
    public String showCreateForm(Model model) {
        model.addAttribute("local", new Local());
        return "locais/cadastrar-local";
    }

    @PostMapping("/cadastrar")
    public String create(@ModelAttribute Local local) {
        localRepository.save(local);
        return "redirect:/locais/listar";
    }

    @GetMapping("/listar")
    public String list(Model model) {
        List<Local> locais = localRepository.findAll();
        model.addAttribute("locais", locais);
        return "locais/listar-locais";
    }

    @GetMapping("/editar/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Local local = localRepository.findById(id).orElseThrow();
        model.addAttribute("local", local);
        return "locais/editar-local";
    }

    @PostMapping("/editar/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Local localForm) {
        Local local = localRepository.findById(id).orElseThrow();
        local.setComarca(localForm.getComarca());
        local.setEspecialidade(localForm.getEspecialidade());
        local.setNumeroVara(localForm.getNumeroVara());
        local.setCodigo(localForm.getCodigo());
        local.setLocalizacao(localForm.getLocalizacao());
        local.setObs(localForm.getObs());
        localRepository.save(local);
        return "redirect:/locais/listar";
    }
}