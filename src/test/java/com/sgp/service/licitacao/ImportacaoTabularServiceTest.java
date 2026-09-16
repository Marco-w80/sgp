package com.sgp.service.licitacao;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class ImportacaoTabularServiceTest {
    private final ImportacaoTabularService service=new ImportacaoTabularService();
    @Test void converteNumerosBrasileiros(){assertThat(service.decimal("45,00")).isEqualByComparingTo("45.00");assertThat(service.decimal("1.250,50")).isEqualByComparingTo("1250.50");assertThat(service.decimal("6.600")).isEqualByComparingTo("6600");}
    @Test void importaCabecalhoEGeraSequencia(){var linhas=service.lerItens("Descrição\tQtd\tUnidade\tValor\nProduto A\t2\tCX\t1.250,50",ImportacaoTabularService.Delimitador.TAB,true,-1,0,1,2,3);assertThat(linhas).hasSize(1);assertThat(linhas.get(0).numero()).isEqualTo(1);assertThat(linhas.get(0).valor()).isEqualByComparingTo(new BigDecimal("1250.50"));}
    @Test void exigeDescricao(){assertThatThrownBy(()->service.lerItens("1\t\t2",ImportacaoTabularService.Delimitador.TAB,false,0,1,2,-1,-1)).isInstanceOf(IllegalArgumentException.class);}
    @Test void respeitaAspasEmCsvComDecimalBrasileiro(){var l=service.lerItens("1,\"Produto, hospitalar\",2,UN,\"45,00\"",ImportacaoTabularService.Delimitador.VIRGULA,false,0,1,2,3,4);assertThat(l.get(0).descricao()).isEqualTo("Produto, hospitalar");assertThat(l.get(0).valor()).isEqualByComparingTo("45.00");}
}
