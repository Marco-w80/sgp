package com.sgp.controller;

import com.sgp.dto.ProdutoDto;
import com.sgp.model.*;
import com.sgp.repository.*;
import com.sgp.service.ProcessoLogService;
import com.sgp.service.ProcessoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    @Autowired
    private GrupoDoencaRepository grupoDoencaRepository;
    @Autowired
    private DoencaRepository doencaRepository;
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private ProcessoLogService processoLogService;
    @Autowired
    private ProcessoService processoService;

    @GetMapping("/cadastrar")
    public String showCreateForm(Model model) {
        List<Paciente> pacientes = pessoaRepository.findAll().stream()
                .filter(p -> p instanceof Paciente)
                .map(p -> (Paciente) p)
                .collect(Collectors.toList());
        List<Advogado> advogados = pessoaRepository.findAll().stream()
                .filter(p -> p instanceof Advogado)
                .map(p -> (Advogado) p)
                .collect(Collectors.toList());
        List<Medico> medicos = pessoaRepository.findAll().stream()
                .filter(p -> p instanceof Medico)
                .map(p -> (Medico) p)
                .collect(Collectors.toList());

        List<ProdutoDto> produtos = produtoRepository.findAll().stream()
                .map(p -> new ProdutoDto(p.getId(), p.getNomeItem()))
                .collect(Collectors.toList());

        model.addAttribute("pacientes", pacientes);
        model.addAttribute("advogados", advogados);
        model.addAttribute("medicos", medicos);
        model.addAttribute("locais", localRepository.findAll());
        model.addAttribute("produtos", produtos);

        model.addAttribute("statusValues", StatusProcesso.values());

        model.addAttribute("gruposDoenca", grupoDoencaRepository.findAll());
        model.addAttribute("doencas", doencaRepository.findAll());
        model.addAttribute("gruposDoenca", grupoDoencaRepository.findAll());

        return "processos/cadastrar-processo";
    }

    @PostMapping("/cadastrar")
    public String create(
            @RequestParam String numeroInterno,
            @RequestParam(required = false) String numeroProcesso, // agora opcional
            @RequestParam Long pacienteId,
            @RequestParam(required = false) Long advogadoId, // agora opcional
            @RequestParam(required = false) Long medicoId, // agora opcional
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam StatusProcesso status,
            @RequestParam(required = false) Long localId, // agora opcional
            @RequestParam(required = false) TipoHospital tipoHospital, // agora opcional
            @RequestParam Long doencaId,
            @RequestParam Long grupoDoencaId,
            @RequestParam(required = false) Long hospitalId, // agora opcional

            @RequestParam(required = false) List<Long> produtoIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> produtoDatas,
            @RequestParam(required = false) List<Integer> produtoQuantidades,

            @RequestParam(defaultValue = "false") boolean cpfAnexado,
            @RequestParam(defaultValue = "false") boolean compResidenciaAnexado,
            @RequestParam(defaultValue = "false") boolean compRendaAnexado,
            @RequestParam(defaultValue = "false") boolean procuracaoAnexado,
            @RequestParam(defaultValue = "false") boolean declaracaoInsuficienciaAnexado,

            @RequestParam(required = false) String obs) {
        Processo proc = new Processo();
        proc.setNumeroInterno(numeroInterno);

        // somente se vier não-nulo e não-blank
        if (numeroProcesso != null && !numeroProcesso.isBlank()) {
            proc.setNumeroProcesso(numeroProcesso);
        }

        proc.setPaciente(
                pessoaRepository.findById(pacienteId)
                        .map(Paciente.class::cast)
                        .orElseThrow(() -> new RuntimeException("Paciente não encontrado")));

        if (advogadoId != null) {
            proc.setAdvogado(pessoaRepository.findById(advogadoId)
                    .map(Advogado.class::cast)
                    .orElseThrow());
        }

        if (medicoId != null) {
            proc.setMedico(pessoaRepository.findById(medicoId)
                    .map(Medico.class::cast)
                    .orElseThrow());
        }

        if (localId != null) {
            proc.setLocal(localRepository.findById(localId).orElseThrow());
        }

        if (hospitalId != null) {
            proc.setHospital(hospitalRepository.findById(hospitalId).orElse(null));
        }

        if (tipoHospital != null) {
            proc.setTipoHospital(tipoHospital);
        }

        proc.setDataInicio(dataInicio);
        proc.setStatus(status);

        proc.setDoenca(doencaRepository.findById(doencaId).orElseThrow());

        // produtos (mesma lógica de antes)
        if (produtoIds != null && produtoDatas != null && produtoQuantidades != null
                && produtoIds.size() == produtoDatas.size()
                && produtoIds.size() == produtoQuantidades.size()) {
            for (int i = 0; i < produtoIds.size(); i++) {
                Produto p = produtoRepository.findById(produtoIds.get(i)).orElseThrow();
                proc.addItem(p, produtoDatas.get(i), produtoQuantidades.get(i));
            }
        }

        // flags e observação
        proc.setCpfAnexado(cpfAnexado);
        proc.setCompResidenciaAnexado(compResidenciaAnexado);
        proc.setCompRendaAnexado(compRendaAnexado);
        proc.setProcuracaoAnexado(procuracaoAnexado);
        proc.setDeclaracaoInsuficienciaAnexado(declaracaoInsuficienciaAnexado);
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

        model.addAttribute("pacientes", pessoaRepository.findAll().stream()
                .filter(p -> p instanceof Paciente).map(p -> (Paciente) p).collect(Collectors.toList()));
        model.addAttribute("advogados", pessoaRepository.findAll().stream()
                .filter(p -> p instanceof Advogado).map(p -> (Advogado) p).collect(Collectors.toList()));
        model.addAttribute("medicos", pessoaRepository.findAll().stream()
                .filter(p -> p instanceof Medico).map(p -> (Medico) p).collect(Collectors.toList()));

        model.addAttribute("locais", localRepository.findAll());
        model.addAttribute("produtos", produtoRepository.findAll());
        model.addAttribute("statusValues", StatusProcesso.values());

        model.addAttribute("hospitais", hospitalRepository.findAll());
        model.addAttribute("gruposDoenca", grupoDoencaRepository.findAll());
        model.addAttribute("doencas", doencaRepository.findAll());

        model.addAttribute("tiposHospital", TipoHospital.values());

        System.out.println("Hospital do processo: " + proc.getHospital());

        return "processos/editar-processo";
    }

    @PostMapping("/editar/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam String numeroInterno,
            @RequestParam(required = false) String numeroProcesso,
            @RequestParam Long pacienteId,
            @RequestParam(required = false) Long advogadoId,
            @RequestParam(required = false) Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam StatusProcesso status,
            @RequestParam(required = false) Long localId,
            @RequestParam(required = false) TipoHospital tipoHospital,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam Long doencaId,

            @RequestParam(required = false) List<Long> produtoIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> produtoDatas,
            @RequestParam(required = false) List<Integer> produtoQuantidades,

            @RequestParam(defaultValue = "false") boolean cpfAnexado,
            @RequestParam(defaultValue = "false") boolean compResidenciaAnexado,
            @RequestParam(defaultValue = "false") boolean compRendaAnexado,
            @RequestParam(defaultValue = "false") boolean procuracaoAnexado,
            @RequestParam(defaultValue = "false") boolean declaracaoInsuficienciaAnexado,

            @RequestParam(required = false) String obs) {
        Processo proc = processoRepository.findById(id).orElseThrow();

        // --- Log de alterações (exemplo para advogado) ---
        String nomeAdvAntigo = proc.getAdvogado() != null ? proc.getAdvogado().getNome() : "";
        String nomeAdvNovo = advogadoId != null
                ? pessoaRepository.findById(advogadoId).map(p -> ((Advogado) p).getNome()).orElse("")
                : "";
        processoLogService.logIfChanged(proc, "Advogado", nomeAdvAntigo, nomeAdvNovo);

        // (faça o mesmo para médico, local, hospital, tipoHospital se quiser logar)

        // --- Atualização dos campos ---
        proc.setNumeroInterno(numeroInterno);
        proc.setNumeroProcesso(numeroProcesso); // aceita null ou blank

        proc.setPaciente(pessoaRepository.findById(pacienteId)
                .map(Paciente.class::cast).orElseThrow());

        if (advogadoId != null) {
            proc.setAdvogado(
                    pessoaRepository.findById(advogadoId)
                            .map(Advogado.class::cast).orElse(null));
        } else {
            proc.setAdvogado(null);
        }

        if (medicoId != null) {
            proc.setMedico(
                    pessoaRepository.findById(medicoId)
                            .map(Medico.class::cast).orElse(null));
        } else {
            proc.setMedico(null);
        }

        proc.setDataInicio(dataInicio);
        proc.setStatus(status);

        if (localId != null) {
            proc.setLocal(localRepository.findById(localId).orElse(null));
        } else {
            proc.setLocal(null);
        }

        proc.setTipoHospital(tipoHospital); // enum aceita null

        if (hospitalId != null) {
            proc.setHospital(hospitalRepository.findById(hospitalId).orElse(null));
        } else {
            proc.setHospital(null);
        }

        proc.setDoenca(doencaRepository.findById(doencaId).orElseThrow());

        proc.setObs(obs); // aceita null ou blank

        proc.setCpfAnexado(cpfAnexado);
        proc.setCompResidenciaAnexado(compResidenciaAnexado);
        proc.setCompRendaAnexado(compRendaAnexado);
        proc.setProcuracaoAnexado(procuracaoAnexado);
        proc.setDeclaracaoInsuficienciaAnexado(declaracaoInsuficienciaAnexado);

        // atualiza itens
        proc.clearItems();
        if (produtoIds != null && produtoDatas != null && produtoQuantidades != null
                && produtoIds.size() == produtoDatas.size()
                && produtoIds.size() == produtoQuantidades.size()) {

            for (int i = 0; i < produtoIds.size(); i++) {
                Produto p = produtoRepository.findById(produtoIds.get(i)).orElseThrow();
                proc.addItem(p, produtoDatas.get(i), produtoQuantidades.get(i));
            }
        }

        processoRepository.save(proc);
        return "redirect:/processos/listar";
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluirProcesso(@PathVariable("id") Long id) {
        try {
            processoService.excluir(id);
            // Retorna uma resposta de sucesso sem conteúdo (HTTP 204 No Content)
            // ou pode retornar um JSON com uma mensagem de sucesso.
            return ResponseEntity.ok().body("Processo excluído com sucesso.");
        } catch (Exception e) {
            // Em caso de erro, retorna uma resposta de erro com uma mensagem
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Erro ao excluir o processo: " + e.getMessage());
        }
    }


}
