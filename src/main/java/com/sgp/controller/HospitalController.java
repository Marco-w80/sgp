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
}
