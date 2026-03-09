package com.sgp.controller;

import com.sgp.model.Processo;
import com.sgp.service.DashboardService;
import com.sgp.service.ProcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private ProcessoService processoService;

    private final DashboardService dashboardService;

    public HomeController(ProcessoService processoService,
                          DashboardService dashboardService) {
        this.processoService = processoService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/intranet")
    public String intranet(@RequestParam(name = "dias", required = false, defaultValue = "0") Integer diasMinimos,
                           @RequestParam(name = "diasSemVisita", required = false, defaultValue = "7") Integer diasSemVisita,
                           @RequestParam(name = "anos", required = false) List<Integer> anos,
                           @RequestParam(name = "trimestre", required = false) Integer trimestre,
                           @RequestParam(name = "mes", required = false) Integer mes,
                           Model model) {

        List<Integer> anosDisponiveis = dashboardService.anosDisponiveis();
        List<Integer> anosSelecionados = resolveAnosSelecionados(anos, anosDisponiveis);

        boolean multiAno = anosSelecionados.size() > 1;
        Integer trimestreValido = multiAno ? null : ((trimestre != null && trimestre >= 1 && trimestre <= 4) ? trimestre : null);
        Integer mesValido = multiAno ? null : ((mes != null && mes >= 1 && mes <= 12) ? mes : null);
        Integer anoSelecionado = anosSelecionados.get(0);

        LocalDate[] periodo = resolvePeriodo(anosSelecionados, trimestreValido, mesValido);
        LocalDate de = periodo[0];
        LocalDate ate = periodo[1];

        var pendencias = processoService.listarProcessosComDocumentacaoPendenteComMinDias(diasMinimos, de, ate);
        var processosSemVisita = processoService.listarProcessosComFiltroAcompanhamento(diasSemVisita, null, null);

        LocalDateTime agora = LocalDateTime.now();
        Map<Long, Long> diasSemVisitaMap = new LinkedHashMap<>();
        for (Processo processo : processosSemVisita) {
            LocalDateTime referencia = processo.getUltimoAcessoEm() != null
                    ? processo.getUltimoAcessoEm()
                    : (processo.getDataInicio() != null ? processo.getDataInicio().atStartOfDay() : null);
            if (referencia != null) {
                diasSemVisitaMap.put(processo.getId(), ChronoUnit.DAYS.between(referencia, agora));
            }
        }

        Map<String, Long> processosSemVisitaPorStatus = processosSemVisita.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus() != null ? p.getStatus().name() : "SEM_STATUS",
                        LinkedHashMap::new,
                        Collectors.counting()));

        var statusCountsFormatado = dashboardService.contarPorStatusFormatado(de, ate);
        long processosGanhos = statusCountsFormatado.getOrDefault("CONCLUIDO", 0L);
        long processosEmAndamento = statusCountsFormatado.getOrDefault("EM ANDAMENTO", 0L);

        model.addAttribute("statusCounts", deepSanitizeForJson(statusCountsFormatado, "SEM_STATUS"));
        model.addAttribute("docResumo",
                deepSanitizeForJson(dashboardService.documentacaoResumo(de, ate), "DESCONHECIDO"));

        model.addAttribute("leadResumo",
                deepSanitizeForJson(dashboardService.leadTimeResumo(de, ate), "TOTAL"));

        model.addAttribute("distTipoHospital",
                deepSanitizeForJson(dashboardService.distribuicaoTipoHospital(de, ate), "(sem tipo)"));

        model.addAttribute("serieNovos",
                deepSanitizeForJson(dashboardService.novosProcessosPorMes(de, ate), "N/A"));
        model.addAttribute("topDoencas",
                deepSanitizeForJson(dashboardService.topDoencas(de, ate), "N/A"));
        model.addAttribute("leadTimeDoenca",
                deepSanitizeForJson(dashboardService.leadTimeMedioPorDoenca(de, ate), "N/A"));
        model.addAttribute("prodAdvogado",
                deepSanitizeForJson(dashboardService.produtividadePorAdvogado(de, ate), "N/A"));
        model.addAttribute("consumoMensalTotal",
                deepSanitizeForJson(dashboardService.consumoMensalTotal(de, ate), "N/A"));
        model.addAttribute("consumoProdutos",
                deepSanitizeForJson(dashboardService.consumoTotalPorProduto(de, ate), "N/A"));
        model.addAttribute(
                "distGenero",
                deepSanitizeForJson(dashboardService.perfilGenero(de, ate), "DESCONHECIDO"));

        // --- o resto permanece ---
        model.addAttribute("pendencias", pendencias);
        model.addAttribute("totalPendentes", pendencias.size());
        model.addAttribute("diasMinimos", diasMinimos);
        model.addAttribute("diasSemVisita", diasSemVisita);
        model.addAttribute("processosSemVisita", processosSemVisita);
        model.addAttribute("diasSemVisitaMap", diasSemVisitaMap);
        model.addAttribute("processosSemVisitaPorStatus", deepSanitizeForJson(processosSemVisitaPorStatus, "SEM_STATUS"));

        model.addAttribute("idadePorSexo", dashboardService.mediaIdadePorSexo(de, ate));
        model.addAttribute("anosDisponiveis", anosDisponiveis);
        model.addAttribute("anosSelecionados", anosSelecionados);
        model.addAttribute("anoSelecionado", anoSelecionado);
        model.addAttribute("trimestreSelecionado", trimestreValido);
        model.addAttribute("mesSelecionado", mesValido);

        String periodoDescricao = montarDescricaoPeriodo(anosSelecionados, trimestreValido, mesValido);
        model.addAttribute("periodoDescricao", periodoDescricao);
        model.addAttribute("processosGanhos", processosGanhos);
        model.addAttribute("processosEmAndamento", processosEmAndamento);

        // Bloco de código atualizado com as novas classes de CSS para os cards
        Map<String, String> css = Map.of(
                "ABERTO", "card-theme-secondary",
                "EM ANDAMENTO", "card-theme-primary", // Corrigido para "EM ANDAMENTO"
                "CONCLUIDO", "card-theme-success",
                "SUSPENSO", "card-theme-danger"
        );
        model.addAttribute("statusCss", css);

        return "intranet/dashboard";
    }

    private Integer resolveAnoSelecionado(Integer ano) {
        if (ano != null && ano >= 2000 && ano <= 2100) {
            return ano;
        }

        List<Integer> anos = dashboardService.anosDisponiveis();
        if (anos != null && !anos.isEmpty()) {
            return anos.get(0);
        }

        return LocalDate.now().getYear();
    }

    private List<Integer> resolveAnosSelecionados(List<Integer> anos, List<Integer> anosDisponiveis) {
        List<Integer> fallback = (anosDisponiveis != null && !anosDisponiveis.isEmpty())
                ? List.of(anosDisponiveis.get(0))
                : List.of(LocalDate.now().getYear());

        if (anos == null || anos.isEmpty()) {
            return fallback;
        }

        var validos = anos.stream()
                .filter(a -> a != null && a >= 2000 && a <= 2100)
                .distinct()
                .sorted(java.util.Collections.reverseOrder())
                .toList();

        return validos.isEmpty() ? fallback : validos;
    }

    private LocalDate[] resolvePeriodo(List<Integer> anosSelecionados, Integer trimestre, Integer mes) {
        if (anosSelecionados == null || anosSelecionados.isEmpty()) {
            return new LocalDate[] { null, null };
        }

        if (anosSelecionados.size() > 1) {
            int minAno = anosSelecionados.stream().min(Integer::compareTo).orElse(LocalDate.now().getYear());
            int maxAno = anosSelecionados.stream().max(Integer::compareTo).orElse(LocalDate.now().getYear());
            return new LocalDate[] {
                    LocalDate.of(minAno, 1, 1),
                    LocalDate.of(maxAno, 12, 31)
            };
        }

        Integer anoSelecionado = anosSelecionados.get(0);
        if (anoSelecionado == null) {
            return new LocalDate[] { null, null };
        }

        if (mes != null) {
            LocalDate inicio = LocalDate.of(anoSelecionado, mes, 1);
            return new LocalDate[] { inicio, inicio.withDayOfMonth(inicio.lengthOfMonth()) };
        }

        if (trimestre != null) {
            int mesInicial = ((trimestre - 1) * 3) + 1;
            LocalDate inicio = LocalDate.of(anoSelecionado, mesInicial, 1);
            LocalDate fim = inicio.plusMonths(2).withDayOfMonth(inicio.plusMonths(2).lengthOfMonth());
            return new LocalDate[] { inicio, fim };
        }

        LocalDate inicioAno = LocalDate.of(anoSelecionado, 1, 1);
        return new LocalDate[] { inicioAno, inicioAno.withMonth(12).withDayOfMonth(31) };
    }

    private String montarDescricaoPeriodo(List<Integer> anosSelecionados, Integer trimestre, Integer mes) {
        if (anosSelecionados == null || anosSelecionados.isEmpty()) {
            return "Período geral";
        }

        if (anosSelecionados.size() > 1) {
            String listaAnos = anosSelecionados.stream().sorted(java.util.Collections.reverseOrder())
                    .map(String::valueOf)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("-");
            return "Anos selecionados: " + listaAnos;
        }

        Integer ano = anosSelecionados.get(0);
        if (mes != null) {
            return String.format("Mês %02d/%d", mes, ano);
        }
        if (trimestre != null) {
            return String.format("%dº Trimestre de %d", trimestre, ano);
        }
        return "Ano de " + ano;
    }

    /**
     * Sanitiza recursivamente:
     * - Converte Map<?,?> em Map<String,Object>, trocando chave null por nullKeyLabel;
     * - Percorre List e arrays, aplicando a mesma lógica nos elementos;
     * - Demais tipos retornam como estão (DTOs, números, strings, etc.).
     */
    private Object deepSanitizeForJson(Object value, String nullKeyLabel) {
        if (value == null)
            return null;

        if (value instanceof java.util.Map<?, ?> m) {
            java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
            for (var e : m.entrySet()) {
                String key = (e.getKey() == null) ? nullKeyLabel : String.valueOf(e.getKey());
                out.put(key, deepSanitizeForJson(e.getValue(), nullKeyLabel));
            }
            return out;
        }

        if (value instanceof java.util.List<?> list) {
            java.util.List<Object> out = new java.util.ArrayList<>(list.size());
            for (Object item : list) {
                out.add(deepSanitizeForJson(item, nullKeyLabel));
            }
            return out;
        }

        if (value.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(value);
            java.util.List<Object> out = new java.util.ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                out.add(deepSanitizeForJson(java.lang.reflect.Array.get(value, i), nullKeyLabel));
            }
            return out;
        }

        return value;
    }
}