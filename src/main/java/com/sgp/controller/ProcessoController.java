package com.sgp.controller;

import com.sgp.dto.ProdutoDto;
import com.sgp.model.*;
import com.sgp.repository.*;
import com.sgp.service.ProcessoLogService;

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
    @Autowired
    private GrupoDoencaRepository grupoDoencaRepository;
    @Autowired
    private DoencaRepository doencaRepository;
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private ProcessoLogService processoLogService;


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
        @RequestParam String numeroProcesso,
        @RequestParam Long pacienteId,
        @RequestParam Long advogadoId,
        @RequestParam Long medicoId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataInicio,
        @RequestParam StatusProcesso status,
        @RequestParam Long localId,
        @RequestParam TipoHospital tipoHospital,
        @RequestParam Long doencaId,
        @RequestParam Long grupoDoencaId,

        @RequestParam(required = false) List<Long> produtoIds,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        List<LocalDate> produtoDatas,
        @RequestParam(required = false) List<Integer> produtoQuantidades,

        @RequestParam(defaultValue = "false") boolean cpfAnexado,
        @RequestParam(defaultValue = "false") boolean compResidenciaAnexado,
        @RequestParam(defaultValue = "false") boolean compRendaAnexado,
        @RequestParam(defaultValue = "false") boolean procuracaoAnexado,
        @RequestParam(defaultValue = "false") boolean declaracaoInsuficienciaAnexado,

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
        proc.setObs(obs);
        

        // seta flags dos checkboxes
        proc.setCpfAnexado(cpfAnexado);
        proc.setCompResidenciaAnexado(compResidenciaAnexado);
        proc.setCompRendaAnexado(compRendaAnexado);
        proc.setProcuracaoAnexado(procuracaoAnexado);
        proc.setDeclaracaoInsuficienciaAnexado(declaracaoInsuficienciaAnexado);

        proc.setTipoHospital(tipoHospital);

        proc.setDoenca(doencaRepository.findById(doencaId).orElse(null));



        


        // adiciona produtos
        if (produtoIds != null
            && produtoDatas != null
            && produtoQuantidades != null
            && produtoIds.size() == produtoDatas.size()
            && produtoIds.size() == produtoQuantidades.size()) {

            for (int i = 0; i < produtoIds.size(); i++) {
                Produto p = produtoRepository.findById(produtoIds.get(i)).orElseThrow();
                LocalDate envio = produtoDatas.get(i);
                Integer qtde = produtoQuantidades.get(i);
                proc.addItem(p, envio, qtde);
            }
        }

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
    @RequestParam String numeroProcesso,
    @RequestParam Long pacienteId,
    @RequestParam Long advogadoId,
    @RequestParam Long medicoId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
    @RequestParam StatusProcesso status,
    @RequestParam Long localId,

    @RequestParam(required = false) List<Long> produtoIds,
    @RequestParam(required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        List<LocalDate> produtoDatas,
    @RequestParam(required = false) List<Integer> produtoQuantidades,

    @RequestParam(defaultValue = "false") boolean cpfAnexado,
    @RequestParam(defaultValue = "false") boolean compResidenciaAnexado,
    @RequestParam(defaultValue = "false") boolean compRendaAnexado,
    @RequestParam(defaultValue = "false") boolean procuracaoAnexado,
    @RequestParam(defaultValue = "false") boolean declaracaoInsuficienciaAnexado,

    @RequestParam(required = false) String obs
) {
    Processo proc = processoRepository.findById(id).orElseThrow();

    // Log de alterações
    processoLogService.logIfChanged(proc, "Número Interno", proc.getNumeroInterno(), numeroInterno);
    processoLogService.logIfChanged(proc, "Número Processo", proc.getNumeroProcesso(), numeroProcesso);
    processoLogService.logIfChanged(proc, "Paciente", proc.getPaciente().getNome(), pessoaRepository.findById(pacienteId).map(p -> ((Paciente)p).getNome()).orElse(""));
    processoLogService.logIfChanged(proc, "Advogado", proc.getAdvogado().getNome(), pessoaRepository.findById(advogadoId).map(p -> ((Advogado)p).getNome()).orElse(""));
    processoLogService.logIfChanged(proc, "Médico", proc.getMedico().getNome(), pessoaRepository.findById(medicoId).map(p -> ((Medico)p).getNome()).orElse(""));
    processoLogService.logIfChanged(proc, "Data Início", proc.getDataInicio(), dataInicio);
    processoLogService.logIfChanged(proc, "Status", proc.getStatus(), status);
    processoLogService.logIfChanged(proc, "Local", proc.getLocal().getComarca(), localRepository.findById(localId).map(l -> l.getComarca()).orElse(""));
    processoLogService.logIfChanged(proc, "Observações", proc.getObs(), obs);

    processoLogService.logIfChanged(proc, "CPF Anexado", proc.isCpfAnexado(), cpfAnexado);
    processoLogService.logIfChanged(proc, "Comp. Residência", proc.isCompResidenciaAnexado(), compResidenciaAnexado);
    processoLogService.logIfChanged(proc, "Comp. Renda", proc.isCompRendaAnexado(), compRendaAnexado);
    processoLogService.logIfChanged(proc, "Procuração", proc.isProcuracaoAnexado(), procuracaoAnexado);
    processoLogService.logIfChanged(proc, "Decl. Insuficiência", proc.isDeclaracaoInsuficienciaAnexado(), declaracaoInsuficienciaAnexado);

    // Atualiza dados
    proc.setNumeroInterno(numeroInterno);
    proc.setNumeroProcesso(numeroProcesso);
    proc.setPaciente((Paciente) pessoaRepository.findById(pacienteId).orElseThrow());
    proc.setAdvogado((Advogado) pessoaRepository.findById(advogadoId).orElseThrow());
    proc.setMedico((Medico) pessoaRepository.findById(medicoId).orElseThrow());
    proc.setDataInicio(dataInicio);
    proc.setStatus(status);
    proc.setLocal(localRepository.findById(localId).orElseThrow());
    proc.setObs(obs);

    proc.setCpfAnexado(cpfAnexado);
    proc.setCompResidenciaAnexado(compResidenciaAnexado);
    proc.setCompRendaAnexado(compRendaAnexado);
    proc.setProcuracaoAnexado(procuracaoAnexado);
    proc.setDeclaracaoInsuficienciaAnexado(declaracaoInsuficienciaAnexado);

    // Itens
    proc.clearItems();
    if (produtoIds != null && produtoDatas != null && produtoQuantidades != null
            && produtoIds.size() == produtoDatas.size()
            && produtoIds.size() == produtoQuantidades.size()) {

        for (int i = 0; i < produtoIds.size(); i++) {
            Produto p = produtoRepository.findById(produtoIds.get(i)).orElseThrow();
            LocalDate envio = produtoDatas.get(i);
            Integer qtde = produtoQuantidades.get(i);
            proc.addItem(p, envio, qtde);
        }
    }

    processoRepository.save(proc);
    return "redirect:/processos/listar";
}


}
