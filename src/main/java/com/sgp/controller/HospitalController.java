package com.sgp.controller;

import com.sgp.model.Hospital;
import com.sgp.repository.HospitalRepository;
import com.sgp.repository.ProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/hospitais")
public class HospitalController {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private ProcessoRepository processoRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("hospitais", hospitalRepository.findAll());
        model.addAttribute("vinculosProcessos", processoRepository.contarVinculosPorHospital().stream()
                .collect(Collectors.toMap(item -> item.getId(), item -> item.getTotal())));
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

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Hospital hospital = hospitalRepository.findById(id).orElse(null);
        if (hospital == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Hospital não encontrado.");
            return "redirect:/hospitais";
        }

        long vinculos = processoRepository.countByHospitalId(id);
        if (vinculos > 0) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "O hospital " + hospital.getNome() + " está vinculado a " + vinculos + " processo"
                            + (vinculos == 1 ? "" : "s") + " e não pode ser excluído.");
            return "redirect:/hospitais";
        }

        try {
            hospitalRepository.delete(hospital);
            hospitalRepository.flush();
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Hospital excluído com sucesso.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Não foi possível excluir o hospital porque ele possui vínculos no sistema.");
        }
        return "redirect:/hospitais";
    }
}
