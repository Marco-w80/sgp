package com.sgp.projections;

/**
 * Todas as projections usadas nos gráficos/consultas de BI do SGP.
 * Você pode separar em arquivos diferentes futuramente se preferir.
 */
public class Projections {

    public interface StatusCountProjection {
        String getStatus(); // nome do enum como string
        Long getTotal();
    }

    public interface SerieMensalProjection {
        String getAnoMes(); // ex: "2025-08"
        Long getValor();
    }

    public interface ChaveValorLongProjection {
        String getNome();
        Long getTotal();
    }

    public interface LeadTimeProjection {
        Long getProcessoId();
        Integer getLeadTimeDias();
    }

    public interface LeadTimeMedioProjection {
        String getNome();      // doença, advogado, hospital...
        Double getMediaDias();
        Long getQtde();
    }

    public interface ProdutoConsumoProjection {
        String getProduto();
        Long getQuantidadeTotal();
    }

    public interface ProdutoConsumoMensalProjection {
        String getProduto();
        String getAnoMes(); // "2025-08"
        Long getQuantidade();
    }

    public interface SexoMediaIdadeProjection {
    String getSexo();   // "MASCULINO", "FEMININO" ou null
    Double getMedia();  // média de idade
    Long getQtde();     // contagem (opcional, pode exibir em tooltip)
}

}
