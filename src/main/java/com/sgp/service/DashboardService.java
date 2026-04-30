package com.sgp.service;

import com.sgp.dto.DTOs;
import com.sgp.model.StatusProcesso;
import com.sgp.projections.Projections;
import com.sgp.repository.ProcessoProdutoRepository;
import com.sgp.repository.ProcessoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public Map<String, Long> contarPorStatusFormatado(LocalDate de, LocalDate ate) {
        var rows = (de == null && ate == null)
                ? processoRepository.contarPorStatus()
                : processoRepository.contarPorStatusPeriodo(de, ate);

        Map<String, Long> out = new LinkedHashMap<>();
        out.put("ABERTO", 0L);
        out.put("EM ANDAMENTO", 0L);
        out.put("OBITO", 0L);
        out.put("SUSPENSO", 0L);

        for (var row : rows) {
            String key = row.getStatus() == null ? "SEM STATUS" : row.getStatus().replace('_', ' ');
            if ("CONCLUIDO".equals(key)) {
                key = "OBITO";
            }
            out.merge(key, row.getTotal(), Long::sum);
        }

        return out;
    }

    public List<DTOs.ProcessoPendenteDTO> listarPendencias(int diasMinimos) {
        return listarPendencias(diasMinimos, null, null);
    }

    public List<DTOs.ProcessoPendenteDTO> listarPendencias(int diasMinimos, LocalDate de, LocalDate ate) {
        return processoRepository.pendenciasDocumento(diasMinimos, de, ate).stream()
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
        return documentacaoResumo(null, null);
    }

    public DTOs.DocumentacaoResumoDTO documentacaoResumo(LocalDate de, LocalDate ate) {
        long total = (de == null && ate == null)
                ? processoRepository.totalProcessos()
                : processoRepository.totalProcessosPeriodo(de, ate);

        long completos = (de == null && ate == null)
                ? processoRepository.totalProcessosCompletos()
                : processoRepository.totalProcessosCompletosPeriodo(de, ate);

        double pct = total == 0 ? 0.0 : (100.0 * completos / (double) total);
        return new DTOs.DocumentacaoResumoDTO(completos, total, round2(pct));
    }

    public DTOs.SerieDTO novosProcessosPorMes() {
        return novosProcessosPorMes(null, null);
    }

    public DTOs.SerieDTO novosProcessosPorMes(LocalDate de, LocalDate ate) {
        var data = (de == null && ate == null)
                ? processoRepository.novosPorMes()
                : processoRepository.novosPorMesPeriodo(de, ate);

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
        return distribuicaoTipoHospital(null, null);
    }

    public Map<String, Long> distribuicaoTipoHospital(LocalDate de, LocalDate ate) {
        var rows = (de == null && ate == null)
                ? processoRepository.distribuicaoPorTipoHospital()
                : processoRepository.distribuicaoPorTipoHospitalPeriodo(de, ate);

        return rows.stream()
                .collect(Collectors.toMap(
                        Projections.ChaveValorLongProjection::getNome,
                        Projections.ChaveValorLongProjection::getTotal,
                        Long::sum,
                        LinkedHashMap::new
                ));
    }

    public List<Map<String, Object>> topDoencas() {
        return topDoencas(null, null);
    }

    public List<Map<String, Object>> topDoencas(LocalDate de, LocalDate ate) {
        var rows = (de == null && ate == null)
                ? processoRepository.topDoencas()
                : processoRepository.topDoencasPeriodo(de, ate);

        return rows.stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("doenca", p.getNome());
                    m.put("total", p.getTotal());
                    return m;
                })
                .toList();
    }

    public List<Map<String, Object>> leadTimeMedioPorDoenca() {
        return leadTimeMedioPorDoenca(null, null);
    }

    public List<Map<String, Object>> leadTimeMedioPorDoenca(LocalDate de, LocalDate ate) {
        var rows = (de == null && ate == null)
                ? processoRepository.leadTimeMedioPorDoenca()
                : processoRepository.leadTimeMedioPorDoencaPeriodo(de, ate);

        return rows.stream()
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
        return produtividadePorAdvogado(null, null);
    }

    public List<Map<String, Object>> produtividadePorAdvogado(LocalDate de, LocalDate ate) {
        var rows = (de == null && ate == null)
                ? processoRepository.produtividadePorAdvogado()
                : processoRepository.produtividadePorAdvogadoPeriodo(de, ate);

        return rows.stream()
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
        return consumoTotalPorProduto(null, null);
    }

    public List<Map<String, Object>> consumoTotalPorProduto(LocalDate de, LocalDate ate) {
        var rows = (de == null && ate == null)
                ? processoProdutoRepository.consumoTotalPorProduto()
                : processoProdutoRepository.consumoTotalPorProdutoPeriodo(de, ate);

        return rows.stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("produto", p.getProduto());
                    m.put("quantidade", p.getQuantidadeTotal());
                    return m;
                })
                .toList();
    }


    public DTOs.LeadTimeResumoDTO leadTimeResumo() {
        return leadTimeResumo(null, null);
    }

    public DTOs.LeadTimeResumoDTO leadTimeResumo(LocalDate de, LocalDate ate) {
        var rows = (de == null && ate == null)
                ? processoRepository.leadTimePorProcesso()
                : processoRepository.leadTimePorProcessoPeriodo(de, ate);

        var lts = rows.stream()
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
        return consumoMensalTotal(null, null);
    }

    public DTOs.SerieDTO consumoMensalTotal(LocalDate de, LocalDate ate) {
        var rows = (de == null && ate == null)
                ? processoProdutoRepository.consumoMensalPorProduto()
                : processoProdutoRepository.consumoMensalPorProdutoPeriodo(de, ate);

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
        return perfilGenero(null, null);
    }

    public Map<String, Long> perfilGenero(LocalDate de, LocalDate ate) {
        var rows = (de == null && ate == null)
                ? processoRepository.distribuicaoPorSexoPaciente()
                : processoRepository.distribuicaoPorSexoPacientePeriodo(de, ate);

        Map<String, Long> out = new LinkedHashMap<>();
        for (var r : rows) {
            String k = r.getNome();
            if (k == null || k.isBlank()) k = "DESCONHECIDO";
            String keyNorm = switch (k.trim().toUpperCase()) {
                case "M", "MASCULINO" -> "Masculino";
                case "F", "FEMININO" -> "Feminino";
                default -> k;
            };
            out.merge(keyNorm, r.getTotal(), Long::sum);
        }
        return out;
    }

    public Map<String, Object> mediaIdadePorSexo() {
        return mediaIdadePorSexo(null, null);
    }

    public Map<String, Object> mediaIdadePorSexo(LocalDate de, LocalDate ate) {
        var bruto = (de == null && ate == null)
                ? processoRepository.mediaIdadePorSexo()
                : processoRepository.mediaIdadePorSexoPeriodo(de, ate);

        List<String> categorias = new ArrayList<>();
        List<Number> valores = new ArrayList<>();
        List<Number> qts = new ArrayList<>();

        java.util.function.Function<String, String> label = s -> {
            if ("MASCULINO".equalsIgnoreCase(s)) return "Masculino";
            if ("FEMININO".equalsIgnoreCase(s)) return "Feminino";
            return "Não informado";
        };

        var ordem = List.of("MASCULINO", "FEMININO", "NAO_INFORMADO");
        var map = new LinkedHashMap<String, Projections.SexoMediaIdadeProjection>();
        for (var it : bruto) map.put(it.getSexo(), it);

        for (var sx : ordem) {
            var it = map.getOrDefault(sx, null);
            categorias.add(label.apply(sx));
            valores.add(it != null && it.getMedia() != null ? Math.round(it.getMedia() * 10.0) / 10.0 : 0);
            qts.add(it != null ? it.getQtde() : 0);
        }

        return Map.of(
                "categorias", categorias,
                "valores", valores,
                "qtde", qts,
                "label", "Média de idade"
        );
    }

    public List<Integer> anosDisponiveis() {
        return processoRepository.anosComProcessos();
    }

}
