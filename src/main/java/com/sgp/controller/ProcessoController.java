package com.sgp.controller;

import com.sgp.dto.ProdutoDto;
import com.sgp.dto.ProcessoPendenciaListagemDTO;
import com.sgp.model.*;
import com.sgp.repository.*;
import com.sgp.service.ProcessoExcelService;
import com.sgp.service.ProcessoLogService;
import com.sgp.service.ProcessoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/processos")
public class ProcessoController {

    private static final List<StatusProcesso> STATUS_ATIVOS = List.of(
            StatusProcesso.ABERTO, StatusProcesso.EM_ANDAMENTO);
    private static final List<StatusProcesso> STATUS_SUSPENSOS = List.of(StatusProcesso.SUSPENSO);
    private static final List<StatusProcesso> STATUS_OBITOS = List.of(
            StatusProcesso.OBITO, StatusProcesso.CONCLUIDO);

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
    @Autowired
    private ProcessoExcelService processoExcelService;
    @Autowired
    private DeferimentoRepository deferimentoRepository;

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

        model.addAttribute("statusValues", statusValuesForForm());

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
        return redirecionarParaListagem(proc.getStatus());
    }

    @GetMapping({"/listar", "/ativos"})
    public String listarAtivos(@RequestParam(required = false) Integer diasSemAcesso,
                               @RequestParam(required = false) Integer diasSemEdicao,
                               Model model) {
        return prepararListagem(diasSemAcesso, diasSemEdicao, STATUS_ATIVOS,
                "Processos ativos",
                "Processos abertos e em andamento.",
                "fas fa-play-circle",
                "/processos/ativos",
                "ativos",
                "Nenhum processo aberto ou em andamento foi encontrado.",
                model);
    }

    @GetMapping("/suspensos")
    public String listarSuspensos(@RequestParam(required = false) Integer diasSemAcesso,
                                  @RequestParam(required = false) Integer diasSemEdicao,
                                  Model model) {
        return prepararListagem(diasSemAcesso, diasSemEdicao, STATUS_SUSPENSOS,
                "Processos suspensos",
                "Processos que estão temporariamente suspensos.",
                "fas fa-pause-circle",
                "/processos/suspensos",
                "suspensos",
                "Nenhum processo suspenso foi encontrado.",
                model);
    }

    @GetMapping("/obitos")
    public String listarObitos(@RequestParam(required = false) Integer diasSemAcesso,
                               @RequestParam(required = false) Integer diasSemEdicao,
                               Model model) {
        return prepararListagem(diasSemAcesso, diasSemEdicao, STATUS_OBITOS,
                "Processos de óbito",
                "Processos encerrados em razão de óbito.",
                "fas fa-ribbon",
                "/processos/obitos",
                "obitos",
                "Nenhum processo de óbito foi encontrado.",
                model);
    }

    private String prepararListagem(Integer diasSemAcesso,
                                    Integer diasSemEdicao,
                                    List<StatusProcesso> statuses,
                                    String titulo,
                                    String descricao,
                                    String icone,
                                    String rota,
                                    String tipo,
                                    String mensagemVazia,
                                    Model model) {
        List<Processo> processos = processoService.listarProcessosComFiltroAcompanhamentoPorStatus(
                diasSemAcesso, diasSemEdicao, statuses);

        LocalDateTime agora = LocalDateTime.now();
        Map<Long, Long> diasSemAcessoMap = new HashMap<>();
        for (Processo p : processos) {
            if (p.getUltimoAcessoEm() != null) {
                diasSemAcessoMap.put(p.getId(), ChronoUnit.DAYS.between(p.getUltimoAcessoEm(), agora));
            }
        }

        model.addAttribute("processos", processos);
        model.addAttribute("diasSemAcesso", diasSemAcesso);
        model.addAttribute("diasSemEdicao", diasSemEdicao);
        model.addAttribute("diasSemAcessoMap", diasSemAcessoMap);
        model.addAttribute("tituloListagem", titulo);
        model.addAttribute("descricaoListagem", descricao);
        model.addAttribute("iconeListagem", icone);
        model.addAttribute("rotaListagem", rota);
        model.addAttribute("tipoListagem", tipo);
        model.addAttribute("mensagemVazia", mensagemVazia);
        return "processos/listar-processos";
    }

    @GetMapping("/pendencias")
    public String listarPendencias(@RequestParam(name = "tipo", required = false, defaultValue = "documentacao") String tipo,
                                   Model model) {
        List<ProcessoPendenciaListagemDTO> pendencias;
        String tipoSelecionado;
        String titulo;

        if ("sem-numero-processo".equalsIgnoreCase(tipo)) {
            pendencias = processoService.listarPendenciasSemNumeroProcesso();
            tipoSelecionado = "sem-numero-processo";
            titulo = "Pendências por Falta de Número de Processo";
        } else {
            pendencias = processoService.listarPendenciasDocumentais();
            tipoSelecionado = "documentacao";
            titulo = "Pendências por Falta de Documentação";
        }

        model.addAttribute("pendencias", pendencias);
        model.addAttribute("tipoSelecionado", tipoSelecionado);
        model.addAttribute("tituloPendencias", titulo);
        model.addAttribute("totalPendencias", pendencias.size());
        return "processos/pendencias-processos";
    }

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Integer diasSemAcesso,
            @RequestParam(required = false) Integer diasSemEdicao,
            @RequestParam(required = false) StatusProcesso status,
            @RequestParam(required = false) String tipo) {

        List<StatusProcesso> statuses = statusesDaListagem(tipo);
        List<Processo> processos = statuses == null
                ? processoService.listarProcessosComFiltroAcompanhamento(diasSemAcesso, diasSemEdicao, status)
                : processoService.listarProcessosComFiltroAcompanhamentoPorStatus(diasSemAcesso, diasSemEdicao, statuses);
        String identificador = tipo == null || tipo.isBlank() ? "processos" : tipo;
        byte[] arquivo = processoExcelService.gerarRelatorioCompleto(processos, "Relatório de Processos");
        String data = LocalDate.now().toString();
        String nomeArquivo = "relatorio-processos-" + identificador + "-" + data + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }

    private List<StatusProcesso> statusesDaListagem(String tipo) {
        if (tipo == null) {
            return null;
        }
        return switch (tipo.toLowerCase()) {
            case "ativos" -> STATUS_ATIVOS;
            case "suspensos" -> STATUS_SUSPENSOS;
            case "obitos" -> STATUS_OBITOS;
            default -> null;
        };
    }

    @GetMapping("/editar/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Processo proc = processoRepository.findById(id).orElseThrow();
        processoLogService.registrarAcesso(proc);
        proc = processoRepository.findById(id).orElseThrow();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<Map<String, String>> deferimentosView = proc.getDeferimentos().stream()
                .map(def -> Map.of(
                        "numero", def.getNumeroDeferimento() != null ? String.valueOf(def.getNumeroDeferimento()) : "-",
                        "tipo", def.getTipo() == null ? "-" : (def.getTipo() == TipoDeferimento.GRUPO_PROD ? "Grupo Prod" : "Juiz"),
                        "mensagem", (def.getMensagem() == null || def.getMensagem().trim().isEmpty()) ? "-" : def.getMensagem(),
                        "dataDeferimento", def.getDataDeferimento() != null ? def.getDataDeferimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-",
                        "dataRegistro", def.getDataRegistro() != null ? def.getDataRegistro().format(formatter) : "-"
                ))
                .collect(Collectors.toList());

        model.addAttribute("processo", proc);
        model.addAttribute("deferimentosView", deferimentosView);
        model.addAttribute(
                "ultimoAcessoFormatado",
                proc.getUltimoAcessoEm() != null
                        ? proc.getUltimoAcessoEm().format(formatter)
                        : "Sem registro"
        );
        model.addAttribute(
                "ultimaEdicaoFormatada",
                proc.getUltimaEdicaoEm() != null
                        ? proc.getUltimaEdicaoEm().format(formatter)
                        : "Sem registro"
        );

        model.addAttribute("pacientes", pessoaRepository.findAll().stream()
                .filter(p -> p instanceof Paciente).map(p -> (Paciente) p).collect(Collectors.toList()));
        model.addAttribute("advogados", pessoaRepository.findAll().stream()
                .filter(p -> p instanceof Advogado).map(p -> (Advogado) p).collect(Collectors.toList()));
        model.addAttribute("medicos", pessoaRepository.findAll().stream()
                .filter(p -> p instanceof Medico).map(p -> (Medico) p).collect(Collectors.toList()));

        model.addAttribute("locais", localRepository.findAll());
        model.addAttribute("produtos", produtoRepository.findAll());
        model.addAttribute("statusValues", statusValuesForForm());

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

            @RequestParam(required = false) String deferimentoMensagem,
            @RequestParam(required = false) TipoDeferimento deferimentoTipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deferimentoData,

            @RequestParam(defaultValue = "false") boolean obito,
            @RequestParam(required = false) String observacaoObito,

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
        proc.setObito(obito);
        proc.setObservacaoObito(observacaoObito); // opcional

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

        if (deferimentoMensagem != null
                && !deferimentoMensagem.isBlank()) {
            Integer proximoNumero = deferimentoRepository.findMaxNumeroByProcessoId(proc.getId()) + 1;

            Deferimento deferimento = new Deferimento();
            deferimento.setNumeroDeferimento(proximoNumero);
            deferimento.setMensagem(deferimentoMensagem.trim());
            deferimento.setTipo(deferimentoTipo != null ? deferimentoTipo : TipoDeferimento.JUIZ);
            deferimento.setDataRegistro(LocalDateTime.now());
            deferimento.setDataDeferimento(deferimentoData != null ? deferimentoData : LocalDate.now());

            proc.addDeferimento(deferimento);
        }

        Processo processoSalvo = processoRepository.save(proc);
        processoLogService.registrarEdicao(processoSalvo);
        return redirecionarParaListagem(processoSalvo.getStatus());
    }

    private String redirecionarParaListagem(StatusProcesso status) {
        if (status == StatusProcesso.SUSPENSO) {
            return "redirect:/processos/suspensos";
        }
        if (status == StatusProcesso.OBITO || status == StatusProcesso.CONCLUIDO) {
            return "redirect:/processos/obitos";
        }
        return "redirect:/processos/ativos";
    }

    private List<StatusProcesso> statusValuesForForm() {
        return Arrays.stream(StatusProcesso.values())
                .filter(status -> status != StatusProcesso.CONCLUIDO)
                .collect(Collectors.toList());
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
