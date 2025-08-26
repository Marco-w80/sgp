package com.sgp.controller;

import com.sgp.model.StatusProcesso;
import com.sgp.service.DashboardService;
import com.sgp.service.ProcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.Map;

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
        return "login";
    }

    @GetMapping("/intranet")
    public String intranet(@RequestParam(name = "dias", required = false, defaultValue = "0") Integer diasMinimos,
                           Model model) {

        var pendencias = processoService.listarProcessosComDocumentacaoPendenteComMinDias(diasMinimos);

        // =========================================================================
        //           ⭐ INÍCIO DA CORREÇÃO PARA FORMATAR OS NOMES DOS STATUS
        // =========================================================================

        // 1. Busca os status como antes
        Map<StatusProcesso, Long> statusCounts = processoService.contarPorStatus();

        // 2. Cria um novo mapa para guardar os nomes formatados
        Map<String, Long> statusCountsFormatado = new LinkedHashMap<>();

        // 3. Percorre o mapa original, formata cada chave e adiciona ao novo mapa
        for (Map.Entry<StatusProcesso, Long> entry : statusCounts.entrySet()) {
            // Transforma o nome do Enum, por ex. "EM_ANDAMENTO", em "EM ANDAMENTO"
            String chaveFormatada = entry.getKey().name().replace('_', ' ');
            statusCountsFormatado.put(chaveFormatada, entry.getValue());
        }

        // 4. Adiciona o NOVO mapa formatado ao model para ser usado na página
        model.addAttribute("statusCounts",
                deepSanitizeForJson(statusCountsFormatado, "SEM_STATUS"));

        // =========================================================================
        //           ⭐ FIM DA CORREÇÃO
        // =========================================================================


        model.addAttribute("docResumo",
                deepSanitizeForJson(dashboardService.documentacaoResumo(), "DESCONHECIDO"));

        model.addAttribute("leadResumo",
                deepSanitizeForJson(dashboardService.leadTimeResumo(), "TOTAL"));

        model.addAttribute("distTipoHospital",
                deepSanitizeForJson(dashboardService.distribuicaoTipoHospital(), "(sem tipo)"));

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
                deepSanitizeForJson(dashboardService.perfilGenero(), "DESCONHECIDO"));

        // --- o resto permanece ---
        model.addAttribute("pendencias", pendencias);
        model.addAttribute("totalPendentes", pendencias.size());
        model.addAttribute("diasMinimos", diasMinimos);

        model.addAttribute("idadePorSexo", dashboardService.mediaIdadePorSexo());

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