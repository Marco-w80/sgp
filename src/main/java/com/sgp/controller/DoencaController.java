package com.sgp.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sgp.dto.DoencaForm;
import com.sgp.model.Doenca;
import com.sgp.model.GrupoDoenca;
import com.sgp.repository.DoencaRepository;
import com.sgp.repository.GrupoDoencaRepository;

@Controller
@RequestMapping("/doencas")
public class DoencaController {

    static final String GRUPO_OUTRAS_DOENCAS = "Outras doenças";

    private final DoencaRepository doencaRepository;
    private final GrupoDoencaRepository grupoRepository;

    public DoencaController(DoencaRepository doencaRepository, GrupoDoencaRepository grupoRepository) {
        this.doencaRepository = doencaRepository;
        this.grupoRepository = grupoRepository;
    }

    @GetMapping("/cadastrar")
    public String mostrarCadastro(@RequestParam(name = "grupoId", required = false) Long grupoId, Model model) {
        List<GrupoDoenca> gruposPorLocalizacao = grupoRepository.findAll().stream()
                .filter(grupo -> !ehGrupoOutrasDoencas(grupo))
                .sorted(Comparator.comparing(GrupoDoenca::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
        GrupoDoenca grupoOutrasDoencas = grupoRepository.findByNomeIgnoreCase(GRUPO_OUTRAS_DOENCAS).orElse(null);

        model.addAttribute("grupos", gruposPorLocalizacao);
        model.addAttribute("doencaForm", new DoencaForm());
        model.addAttribute("outrasDoencas", grupoOutrasDoencas == null
                ? List.of()
                : doencaRepository.findByGrupoId(grupoOutrasDoencas.getId()).stream()
                        .sorted(Comparator.comparing(Doenca::getNome, String.CASE_INSENSITIVE_ORDER))
                        .toList());

        if (grupoId != null) {
            grupoRepository.findById(grupoId)
                    .filter(grupo -> !ehGrupoOutrasDoencas(grupo))
                    .ifPresent(grupo -> {
                        model.addAttribute("grupoSelecionado", grupo);
                        model.addAttribute("doencas", doencaRepository.findByGrupoId(grupo.getId()).stream()
                                .sorted(Comparator.comparing(Doenca::getNome, String.CASE_INSENSITIVE_ORDER))
                                .toList());
                    });
        }

        return "doencas/cadastrar";
    }

    @PostMapping("/cadastrar")
    public String cadastrar(@ModelAttribute DoencaForm form, RedirectAttributes redirect) {
        String nome = normalizar(form.getNome());
        GrupoDoenca grupo = form.getGrupoId() == null ? null : grupoRepository.findById(form.getGrupoId()).orElse(null);

        if (nome == null || grupo == null || ehGrupoOutrasDoencas(grupo)) {
            redirect.addFlashAttribute("errorMessage", "Informe a localização e o nome da doença.");
            return "redirect:/doencas/cadastrar";
        }
        if (doencaRepository.existsByNomeIgnoreCaseAndGrupoId(nome, grupo.getId())) {
            redirect.addFlashAttribute("errorMessage", "Esta doença já está cadastrada nesta localização.");
            return "redirect:/doencas/cadastrar?grupoId=" + grupo.getId();
        }

        salvarDoenca(nome, grupo);
        redirect.addFlashAttribute("successMessage", "Doença cadastrada com sucesso.");
        return "redirect:/doencas/cadastrar?grupoId=" + grupo.getId();
    }

    @PostMapping("/cadastrar-geral")
    public String cadastrarGeral(@RequestParam("nome") String nomeInformado, RedirectAttributes redirect) {
        String nome = normalizar(nomeInformado);
        if (nome == null) {
            redirect.addFlashAttribute("errorMessage", "Informe o nome da doença.");
            return "redirect:/doencas/cadastrar";
        }

        GrupoDoenca grupo = obterOuCriarGrupoOutrasDoencas();
        if (doencaRepository.existsByNomeIgnoreCaseAndGrupoId(nome, grupo.getId())) {
            redirect.addFlashAttribute("errorMessage", "Esta doença já está cadastrada.");
            return "redirect:/doencas/cadastrar";
        }

        salvarDoenca(nome, grupo);
        redirect.addFlashAttribute("successMessage", "Doença cadastrada com sucesso.");
        return "redirect:/doencas/cadastrar";
    }

    @GetMapping("/listar")
    public String listarTodos(Model model) {
        List<GrupoDoenca> grupos = grupoRepository.findAll().stream()
                .sorted(Comparator.comparing(GrupoDoenca::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
        model.addAttribute("grupos", grupos);
        return "doencas/listar";
    }

    @PostMapping("/editar")
    public String editar(@RequestParam("id") Long id,
                         @RequestParam("nome") String nomeInformado,
                         @RequestParam("grupoId") Long grupoId,
                         RedirectAttributes redirect) {
        String nome = normalizar(nomeInformado);
        Doenca doenca = doencaRepository.findById(id).orElse(null);

        if (doenca == null || nome == null || doenca.getGrupo() == null
                || !doenca.getGrupo().getId().equals(grupoId) || ehGrupoOutrasDoencas(doenca.getGrupo())) {
            redirect.addFlashAttribute("errorMessage", "Não foi possível atualizar a doença.");
            return "redirect:/doencas/cadastrar?grupoId=" + grupoId;
        }
        if (doencaRepository.existsByNomeIgnoreCaseAndGrupoIdAndIdNot(nome, grupoId, id)) {
            redirect.addFlashAttribute("errorMessage", "Esta doença já está cadastrada nesta localização.");
            return "redirect:/doencas/cadastrar?grupoId=" + grupoId;
        }

        doenca.setNome(nome);
        doencaRepository.save(doenca);
        redirect.addFlashAttribute("successMessage", "Doença atualizada com sucesso.");
        return "redirect:/doencas/cadastrar?grupoId=" + grupoId;
    }

    @PostMapping("/editar-geral")
    public String editarGeral(@RequestParam("id") Long id,
                              @RequestParam("nome") String nomeInformado,
                              RedirectAttributes redirect) {
        String nome = normalizar(nomeInformado);
        Doenca doenca = doencaRepository.findById(id).orElse(null);

        if (doenca == null || nome == null || !ehGrupoOutrasDoencas(doenca.getGrupo())) {
            redirect.addFlashAttribute("errorMessage", "Não foi possível atualizar a doença.");
            return "redirect:/doencas/cadastrar";
        }
        if (doencaRepository.existsByNomeIgnoreCaseAndGrupoIdAndIdNot(nome, doenca.getGrupo().getId(), id)) {
            redirect.addFlashAttribute("errorMessage", "Esta doença já está cadastrada.");
            return "redirect:/doencas/cadastrar";
        }

        doenca.setNome(nome);
        doencaRepository.save(doenca);
        redirect.addFlashAttribute("successMessage", "Doença atualizada com sucesso.");
        return "redirect:/doencas/cadastrar";
    }

    private GrupoDoenca obterOuCriarGrupoOutrasDoencas() {
        return grupoRepository.findByNomeIgnoreCase(GRUPO_OUTRAS_DOENCAS).orElseGet(() -> {
            GrupoDoenca grupo = new GrupoDoenca();
            grupo.setNome(GRUPO_OUTRAS_DOENCAS);
            return grupoRepository.save(grupo);
        });
    }

    private void salvarDoenca(String nome, GrupoDoenca grupo) {
        Doenca doenca = new Doenca();
        doenca.setNome(nome);
        doenca.setGrupo(grupo);
        doencaRepository.save(doenca);
    }

    private boolean ehGrupoOutrasDoencas(GrupoDoenca grupo) {
        return grupo != null && grupo.getNome() != null
                && GRUPO_OUTRAS_DOENCAS.equalsIgnoreCase(grupo.getNome().trim());
    }

    private String normalizar(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        return nome.trim();
    }
}
