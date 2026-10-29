package com.sgp.controller;

import com.sgp.model.Local;
import com.sgp.repository.LocalRepository;
import com.sgp.repository.ProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/locais")
public class LocalController {

    @Autowired
    private LocalRepository localRepository;

    @Autowired
    private ProcessoRepository processoRepository;

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
        model.addAttribute("vinculosProcessos", processoRepository.contarVinculosPorLocal().stream()
                .collect(Collectors.toMap(item -> item.getId(), item -> item.getTotal())));
        return "locais/listar-locais";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Local local = localRepository.findById(id).orElse(null);
        if (local == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Local não encontrado.");
            return "redirect:/locais/listar";
        }

        long vinculos = processoRepository.countByLocalId(id);
        if (vinculos > 0) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "O local " + local.getComarca() + " está vinculado a " + vinculos + " processo"
                            + (vinculos == 1 ? "" : "s") + " e não pode ser excluído.");
            return "redirect:/locais/listar";
        }

        try {
            localRepository.delete(local);
            localRepository.flush();
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Local excluído com sucesso.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Não foi possível excluir o local porque ele possui vínculos no sistema.");
        }
        return "redirect:/locais/listar";
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
