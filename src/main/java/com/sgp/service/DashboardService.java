package com.sgp.service;

import com.sgp.dto.DTOs;
import com.sgp.model.StatusProcesso;
import com.sgp.projections.Projections;
import com.sgp.repository.ProcessoProdutoRepository;
import com.sgp.repository.ProcessoRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ProcessoRepository processoRepository;
    private final ProcessoProdutoRepository processoProdutoRepository;

    public DashboardService(ProcessoRepository processoRepository,
                            ProcessoProdutoRepository processoProdutoRepository) {
        this.processoRepository = processoRepository;
        this.processoProdutoRepository = processoProdutoRepository;
    }

    // ========== Helpers ==========
    private static boolean toBool(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        return false;
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    private static int percentile(List<Integer> sorted, double p) {
        int n = sorted.size();
        int idx = (int) Math.ceil(p * n) - 1;
        if (idx < 0) idx = 0;
        if (idx >= n) idx = n - 1;
        return sorted.get(idx);
    }

    // ========== Já existentes na sua tela ==========
    public Map<StatusProcesso, Long> contarPorStatusEnum() {
        var list = processoRepository.contarPorStatus();
        Map<StatusProcesso, Long> map = new EnumMap<>(StatusProcesso.class);
        for (var sc : list) {
            StatusProcesso st = StatusProcesso.valueOf(sc.getStatus());
            map.put(st, sc.getTotal());
        }
        return map;
    }

    public List<DTOs.ProcessoPendenteDTO> listarPendencias(int diasMinimos) {
        return processoRepository.pendenciasDocumento(diasMinimos).stream()
                .map(arr -> new DTOs.ProcessoPendenteDTO(
                        ((Number) arr[0]).longValue(),
                        (String)  arr[1],
                        (String)  arr[2],
                        ((Number) arr[3]).intValue(),
                        toBool(arr[4]),
                        toBool(arr[5]),
                        toBool(arr[6]),
                        toBool(arr[7]),
                        toBool(arr[8])
                )).toList();
    }

    // ========== KPIs / Séries ==========
    public DTOs.DocumentacaoResumoDTO documentacaoResumo() {
        long total = processoRepository.totalProcessos();
        long completos = processoRepository.totalProcessosCompletos();
        double pct = total == 0 ? 0.0 : (100.0 * completos / (double) total);
        return new DTOs.DocumentacaoResumoDTO(completos, total, round2(pct));
    }

    public DTOs.SerieDTO novosProcessosPorMes() {
        var data = processoRepository.novosPorMes();

        List<String> categorias = data.stream()
                .map(Projections.SerieMensalProjection::getAnoMes)
                .toList();

        // Projections.SerieMensalProjection#getValor retorna Long -> precisamos "upar" para Number
        List<Number> valores = data.stream()
                .map(Projections.SerieMensalProjection::getValor)
                .map(Number.class::cast)
                .toList();

        return new DTOs.SerieDTO("Novos processos", categorias, valores);
    }

    public Map<String, Long> distribuicaoTipoHospital() {
        return processoRepository.distribuicaoPorTipoHospital().stream()
                .collect(Collectors.toMap(
                        Projections.ChaveValorLongProjection::getNome,
                        Projections.ChaveValorLongProjection::getTotal,
                        Long::sum,
                        LinkedHashMap::new
                ));
    }

    public List<Map<String, Object>> topDoencas() {
    return processoRepository.topDoencas().stream()
            .map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("doenca", p.getNome());
                m.put("total", p.getTotal());
                return m;
            })
            .toList();
}

public List<Map<String, Object>> leadTimeMedioPorDoenca() {
    return processoRepository.leadTimeMedioPorDoenca().stream()
            .map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("doenca", p.getNome());
                m.put("mediaDias", round2(p.getMediaDias() == null ? 0.0 : p.getMediaDias()));
                m.put("qtde", p.getQtde());
                return m;
            })
            .toList();
}

public List<Map<String, Object>> produtividadePorAdvogado() {
    return processoRepository.produtividadePorAdvogado().stream()
            .map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("advogado", p.getNome());
                m.put("mediaDias", round2(p.getMediaDias() == null ? 0.0 : p.getMediaDias()));
                m.put("qtde", p.getQtde());
                return m;
            })
            .toList();
}

public List<Map<String, Object>> consumoTotalPorProduto() {
    return processoProdutoRepository.consumoTotalPorProduto().stream()
            .map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("produto", p.getProduto());
                m.put("quantidade", p.getQuantidadeTotal());
                return m;
            })
            .toList();
}


    public DTOs.LeadTimeResumoDTO leadTimeResumo() {
        var lts = processoRepository.leadTimePorProcesso().stream()
                .map(Projections.LeadTimeProjection::getLeadTimeDias)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        if (lts.isEmpty()) return new DTOs.LeadTimeResumoDTO(0, 0, 0);

        double media = lts.stream().mapToInt(Integer::intValue).average().orElse(0);
        int p50 = percentile(lts, 0.50);
        int p90 = percentile(lts, 0.90);
        return new DTOs.LeadTimeResumoDTO(round2(media), p50, p90);
    }

    // Série mensal de consumo (agregado total)
    public DTOs.SerieDTO consumoMensalTotal() {
        var rows = processoProdutoRepository.consumoMensalPorProduto();

        // Pivot simples: mês -> soma total
        Map<String, Long> porMes = new LinkedHashMap<>();
        for (var r : rows) {
            porMes.merge(r.getAnoMes(), r.getQuantidade(), Long::sum);
        }

        List<String> meses = new ArrayList<>(porMes.keySet());
        // Long -> Number (necessário para o DTO)
        List<Number> valores = meses.stream()
                .map(porMes::get)
                .map(Number.class::cast)
                .toList();

        return new DTOs.SerieDTO("Consumo total (todos produtos)", meses, valores);
    }

public Map<String, Long> perfilGenero() {
    var rows = processoRepository.distribuicaoPorSexoPaciente();
    Map<String, Long> out = new LinkedHashMap<>();
    for (var r : rows) {
        String k = r.getNome();
        if (k == null || k.isBlank()) k = "DESCONHECIDO";
        // Normalização extra (se um dia entrar algo fora do enum)
        String keyNorm = switch (k.trim().toUpperCase()) {
            case "M", "MASCULINO" -> "Masculino";
            case "F", "FEMININO"  -> "Feminino";
            default -> k;
        };
        out.merge(keyNorm, r.getTotal(), Long::sum);
    }
    return out;
}

///Idade projeção
public Map<String, Object> mediaIdadePorSexo() {
    var bruto = processoRepository.mediaIdadePorSexo();

    // Ordena por label amigável para manter M/F/Não informado
    List<String> categorias = new java.util.ArrayList<>();
    List<Number> valores = new java.util.ArrayList<>();
    List<Number> qts     = new java.util.ArrayList<>();

    java.util.function.Function<String,String> label = s -> {
        if ("MASCULINO".equalsIgnoreCase(s)) return "Masculino";
        if ("FEMININO".equalsIgnoreCase(s))  return "Feminino";
        return "Não informado";
    };

    // Garante ordem: M, F, Não informado
    var ordem = java.util.List.of("MASCULINO","FEMININO","NAO_INFORMADO");
    var map = new java.util.LinkedHashMap<String, Projections.SexoMediaIdadeProjection>();
    for (var it : bruto) map.put(it.getSexo(), it);

    for (var sx : ordem) {
        var it = map.getOrDefault(sx, null);
        categorias.add(label.apply(sx));
        valores.add(it != null && it.getMedia()!=null ? Math.round(it.getMedia()*10.0)/10.0 : 0);
        qts.add(it != null ? it.getQtde() : 0);
    }

    return Map.of(
        "categorias", categorias,
        "valores", valores,
        "qtde", qts,
        "label", "Média de idade"
    );
}

}
