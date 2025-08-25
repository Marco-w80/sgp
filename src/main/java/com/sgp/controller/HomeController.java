package com.sgp.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sgp.dto.ProcessoPendenteDTO;
import com.sgp.model.StatusProcesso;
import com.sgp.service.DashboardService;
import com.sgp.service.ProcessoService;

@Controller
public class HomeController {
    

    @Autowired
    private ProcessoService processoService;

    private final DashboardService dashboardService;   // novo Service do BI

    public HomeController(ProcessoService processoService,
                              DashboardService dashboardService) {
        this.processoService = processoService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String home() {
        // return "site/home";
        return "login";
    }

      @GetMapping("/intranet")
public String intranet(@RequestParam(name = "dias", required = false, defaultValue = "0") Integer diasMinimos,
                       Model model) {

    var pendencias = processoService.listarProcessosComDocumentacaoPendenteComMinDias(diasMinimos);
    Map<StatusProcesso, Long> statusCounts = processoService.contarPorStatus();

    // --- SANITIZAÇÃO GERAL DE MAPAS/LISTAS PARA THYMELEAF/JS ---
    model.addAttribute("statusCounts",
        deepSanitizeForJson(statusCounts, "SEM_STATUS"));

    model.addAttribute("docResumo",
        deepSanitizeForJson(dashboardService.documentacaoResumo(), "DESCONHECIDO"));

    model.addAttribute("leadResumo",
        deepSanitizeForJson(dashboardService.leadTimeResumo(), "TOTAL"));

    // ✅ importantíssimo: esse vinha com chave null quando tipoHospital é null
    model.addAttribute("distTipoHospital",
        deepSanitizeForJson(dashboardService.distribuicaoTipoHospital(), "(sem tipo)"));

    // (opc) também passo pelos helpers — não faz mal e garante robustez
    model.addAttribute("serieNovos",
        deepSanitizeForJson(dashboardService.novosProcessosPorMes(), "N/A"));
    model.addAttribute("topDoencas",
        deepSanitizeForJson(dashboardService.topDoencas(), "N/A"));
    model.addAttribute("leadTimeDoenca",
        deepSanitizeForJson(dashboardService.leadTimeMedioPorDoenca(), "N/A"));
    model.addAttribute("prodAdvogado",
        deepSanitizeForJson(dashboardService.produtividadePorAdvogado(), "N/A"));
    model.addAttribute("consumoMensalTotal",
        deepSanitizeForJson(dashboardService.consumoMensalTotal(), "N/A"));
    model.addAttribute("consumoProdutos",
        deepSanitizeForJson(dashboardService.consumoTotalPorProduto(), "N/A"));
        model.addAttribute(
    "distGenero",
    deepSanitizeForJson(dashboardService.perfilGenero(), "DESCONHECIDO")
);


    // --- o resto permanece ---
    model.addAttribute("pendencias", pendencias);
    model.addAttribute("totalPendentes", pendencias.size());
    model.addAttribute("diasMinimos", diasMinimos);

    model.addAttribute("idadePorSexo", dashboardService.mediaIdadePorSexo());


    Map<StatusProcesso, String> css = Map.of(
        StatusProcesso.ABERTO, "border-left-info",
        StatusProcesso.EM_ANDAMENTO, "border-left-primary",
        StatusProcesso.CONCLUIDO, "border-left-success",
        StatusProcesso.SUSPENSO, "border-left-danger"
    );
    model.addAttribute("statusCss", css);

    return "intranet/dashboard";
}

/** 
 * Sanitiza recursivamente:
 * - Converte Map<?,?> em Map<String,Object>, trocando chave null por nullKeyLabel;
 * - Percorre List e arrays, aplicando a mesma lógica nos elementos;
 * - Demais tipos retornam como estão (DTOs, números, strings, etc.).
 */
private Object deepSanitizeForJson(Object value, String nullKeyLabel) {
    if (value == null) return null;

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

