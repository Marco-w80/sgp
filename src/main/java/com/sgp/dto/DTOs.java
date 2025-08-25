package com.sgp.dto;


import java.util.List;

/**
 * DTOs usados no dashboard/BI do SGP.
 * Se preferir, futuramente pode separar cada DTO em seu próprio arquivo.
 */
public class DTOs {

    /**
     * Resumo de documentação (quantos processos completos, total, %).
     */
    public record DocumentacaoResumoDTO(long completos, long total, double percentual) {}

    /**
     * Série para gráficos (linha, barras etc.).
     */
    public record SerieDTO(String label, List<String> categorias, List<Number> valores) {}

    /**
     * Resumo de lead time (média, percentil 50, percentil 90).
     */
    public record LeadTimeResumoDTO(double mediaDias, int p50, int p90) {}

    /**
     * Processos com documentação pendente.
     */
    public record ProcessoPendenteDTO(
            Long id,
            String numeroInterno,
            String paciente,
            int diasDesdeInicio,
            boolean cpf,
            boolean compResidencia,
            boolean compRenda,
            boolean procuracao,
            boolean declaracaoInsuficiencia
    ) {}
}
