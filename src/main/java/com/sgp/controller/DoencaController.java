package com.sgp.controller;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.sgp.dto.DoencaForm;
import com.sgp.model.Doenca;
import com.sgp.model.GrupoDoenca;
import com.sgp.repository.DoencaRepository;
import com.sgp.repository.GrupoDoencaRepository;

import java.util.List;

@Controller
@RequestMapping("/doencas")
public class DoencaController {

    private final DoencaRepository doencaRepository;
    private final GrupoDoencaRepository grupoRepository;

    public DoencaController(DoencaRepository doencaRepository, GrupoDoencaRepository grupoRepository) {
        this.doencaRepository = doencaRepository;
        this.grupoRepository = grupoRepository;
    }

    @GetMapping("/cadastrar")
public String mostrarCadastro(@RequestParam(name = "grupoId", required = false) Long grupoId, Model model) {
    
    model.addAttribute("grupos", grupoRepository.findAll());
    model.addAttribute("doencaForm", new DoencaForm());
    

    if (grupoId != null) {
        GrupoDoenca grupoSelecionado = grupoRepository.findById(grupoId).orElse(null);
        model.addAttribute("grupoSelecionado", grupoSelecionado);
        model.addAttribute("doencas", doencaRepository.findAll()
            .stream()
            .filter(d -> d.getGrupo().getId().equals(grupoId))
            .toList());
    }

    return "doencas/cadastrar";
}

@PostMapping("/cadastrar")
public String cadastrar(@ModelAttribute DoencaForm form) {
    Long grupoId = form.getGrupoId();
    System.out.println("➡️ [DEBUG] Recebido form: nome=" + form.getNome() + ", grupoId=" + form.getGrupoId());


    if (grupoId != null && form.getNome() != null && !form.getNome().isBlank()) {
        Doenca doenca = new Doenca();
        doenca.setNome(form.getNome());
        doenca.setGrupo(grupoRepository.findById(grupoId).orElse(null));
        doencaRepository.save(doenca);
        return "redirect:/doencas/cadastrar?grupoId=" + grupoId + "&sucesso";
    }

    return "redirect:/doencas/cadastrar";
}



    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("doencas", doencaRepository.findAll());
        return "doencas/listar";
    }

    @PostMapping("/editar")
public String editar(@RequestParam("id") Long id,
                     @RequestParam("nome") String nome,
                     @RequestParam("grupoId") Long grupoId) {

    Doenca doenca = doencaRepository.findById(id).orElse(null);
    if (doenca != null && nome != null && !nome.isBlank()) {
        doenca.setNome(nome.trim());
        doencaRepository.save(doenca);
    }
    return "redirect:/doencas/cadastrar?grupoId=" + grupoId + "&sucesso";
}


}