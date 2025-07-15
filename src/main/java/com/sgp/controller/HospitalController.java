package com.sgp.controller;

import com.sgp.model.Hospital;
import com.sgp.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hospitais")
public class HospitalController {

    @Autowired
    private HospitalRepository hospitalRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("hospitais", hospitalRepository.findAll());
        return "hospitais/listar";
    }

    @GetMapping("/cadastrar")
    public String mostrarFormulario(Model model) {
        model.addAttribute("hospital", new Hospital());
        return "hospitais/cadastrar";
    }

    @PostMapping("/cadastrar")
    public String salvar(@ModelAttribute Hospital hospital) {
        hospitalRepository.save(hospital);
        return "redirect:/hospitais";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Hospital hospital = hospitalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        model.addAttribute("hospital", hospital);
        return "hospitais/editar";
    }

    @PostMapping("/editar/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute Hospital hospital) {
        hospital.setId(id);
        hospitalRepository.save(hospital);
        return "redirect:/hospitais";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        hospitalRepository.deleteById(id);
        return "redirect:/hospitais";
    }
}