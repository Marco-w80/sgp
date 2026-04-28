package com.sgp.controller;

import com.sgp.model.Processo;
import com.sgp.model.StatusProcesso;
import com.sgp.model.Local;
import com.sgp.repository.ProcessoRepository;
import com.sgp.repository.LocalRepository;
import com.sgp.service.ProcessoExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/relatorios/processos")
public class RelatorioProcessosController {

    private final ProcessoRepository processoRepository;
    private final LocalRepository localRepository;
    private final ProcessoExcelService processoExcelService;

    public RelatorioProcessosController(ProcessoRepository processoRepository,
                                       LocalRepository localRepository,
                                       ProcessoExcelService processoExcelService) {
        this.processoRepository = processoRepository;
        this.localRepository = localRepository;
        this.processoExcelService = processoExcelService;
    }

    @GetMapping
    public String relatorioProcessos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) StatusProcesso status,
            @RequestParam(required = false) String paciente,
            @RequestParam(required = false) Long localId,
            @RequestParam(required = false) Boolean cpfAnexado,
            @RequestParam(required = false) Boolean compResidenciaAnexado,
            @RequestParam(required = false) Boolean compRendaAnexado,
            @RequestParam(required = false) Boolean procuracaoAnexado,
            @RequestParam(required = false) Boolean declaracaoInsuficienciaAnexado,
            Model model) {

        // 1) Busca os locais e enum para popular os filtros
        List<Local> locais = localRepository.findAll();
        StatusProcesso[] statusValues = StatusProcesso.values();

        // 2) Filtra processos conforme parâmetros
        List<Processo> processos = processoRepository
            .findByFiltros(de, ate, status, paciente, localId, cpfAnexado, compResidenciaAnexado, compRendaAnexado, procuracaoAnexado, declaracaoInsuficienciaAnexado);

        // 3) Adiciona ao model
        model.addAttribute("locais", locais);
        model.addAttribute("statusValues", statusValues);
        model.addAttribute("processos", processos);
        model.addAttribute("totalProcessos", processos.size());

        // repassa os parâmetros para manter os filtros selecionados
        model.addAttribute("param", new ParamWrapper(de, ate, status, paciente, localId, cpfAnexado, compResidenciaAnexado, compRendaAnexado, procuracaoAnexado, declaracaoInsuficienciaAnexado));

        return "relatorios/processo";
    }

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) StatusProcesso status,
            @RequestParam(required = false) String paciente,
            @RequestParam(required = false) Long localId,
            @RequestParam(required = false) Boolean cpfAnexado,
            @RequestParam(required = false) Boolean compResidenciaAnexado,
            @RequestParam(required = false) Boolean compRendaAnexado,
            @RequestParam(required = false) Boolean procuracaoAnexado,
            @RequestParam(required = false) Boolean declaracaoInsuficienciaAnexado) {

        List<Processo> processos = processoRepository
                .findByFiltros(de, ate, status, paciente, localId, cpfAnexado, compResidenciaAnexado, compRendaAnexado, procuracaoAnexado, declaracaoInsuficienciaAnexado);

        byte[] arquivo = processoExcelService.gerarRelatorioCompleto(processos, "Relatorio de Processos");
        String data = LocalDate.now().toString();
        String nomeArquivo = "relatorio-processos-" + data + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }

    // Classe utilitária para repassar filtros ao template (para manter valores no form)
    public static class ParamWrapper {
        public LocalDate de, ate;
        public StatusProcesso status;
        public String paciente;
        public Long localId;
        public Boolean cpfAnexado, compResidenciaAnexado, compRendaAnexado, procuracaoAnexado, declaracaoInsuficienciaAnexado;
        public ParamWrapper(LocalDate de, LocalDate ate, StatusProcesso status, String paciente, Long localId,
                            Boolean cpfAnexado, Boolean compResidenciaAnexado, Boolean compRendaAnexado, Boolean procuracaoAnexado, Boolean declaracaoInsuficienciaAnexado) {
            this.de = de;
            this.ate = ate;
            this.status = status;
            this.paciente = paciente;
            this.localId = localId;
            this.cpfAnexado = cpfAnexado;
            this.compResidenciaAnexado = compResidenciaAnexado;
            this.compRendaAnexado = compRendaAnexado;
            this.procuracaoAnexado = procuracaoAnexado;
            this.declaracaoInsuficienciaAnexado = declaracaoInsuficienciaAnexado;
        }
    }
}
