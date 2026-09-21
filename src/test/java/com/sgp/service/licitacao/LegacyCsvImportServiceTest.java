package com.sgp.service.licitacao;

import com.sgp.model.licitacao.DecisaoItem;
import com.sgp.model.licitacao.EtapaLicitacao;
import com.sgp.repository.licitacao.CotacaoLicitacaoRepository;
import com.sgp.repository.licitacao.FornecedorItemRepository;
import com.sgp.repository.licitacao.FornecedorLicitacaoRepository;
import com.sgp.repository.licitacao.LicitacaoHistoricoRepository;
import com.sgp.repository.licitacao.LicitacaoItemRepository;
import com.sgp.repository.licitacao.LicitacaoRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class LegacyCsvImportServiceTest {
    @Autowired LegacyCsvImportService service;
    @Autowired LicitacaoRepository licitacoes;
    @Autowired LicitacaoItemRepository itens;
    @Autowired CotacaoLicitacaoRepository cotacoes;
    @Autowired LicitacaoHistoricoRepository historicos;
    @Autowired FornecedorLicitacaoRepository fornecedores;
    @Autowired FornecedorItemRepository fornecedorItens;

    @Test
    void importaRelacionamentosMultilinhasEPermiteReexecucao() {
        var arquivos = new LegacyCsvImportService.Arquivos(
                bytes("id;orgao;numero_edital;uasg;modalidade;objeto;data_disputa;prazo_entrega;valor_estimado_total;precisa_amostra;link_edital;observacoes;current_stage;arquivada;created_by;created_at;updated_at\n"
                        + "9001;Órgão legado;77/2026;123;Pregão;\"Objeto com; ponto e\nquebra de linha\";2026-09-25T10:00;10 dias;100.50;1;https://exemplo/77;;analise_cotacao;0;;2026-09-01 10:00:00;2026-09-02 11:00:00\n"),
                bytes("id;licitacao_id;numero_item;descricao;quantidade;unidade;valor_referencia_edital;preco_maximo;percentual_desconto;estrategia_lance;resultado;preco_final;motivo_perda;created_at;updated_at;aprovado\n"
                        + "9101;9001;1;Medicamento legado;100.0000;UN;2.50;2.00;20;;;;;2026-09-01 10:01:00;2026-09-02 11:01:00;1\n"),
                bytes("id;item_id;fornecedor;valor_unitario;data_cotacao;observacoes;created_by;created_at\n"
                        + "9201;9101;BLAU;1.75;;; ;2026-09-03 12:30:00\n"),
                bytes("id;licitacao_id;from_stage;to_stage;changed_by;changed_at;observacoes;snapshot_json\n"
                        + "9301;9001;analise_cotacao;preco_maximo;;2026-09-03 13:00:00;;\"{\"\"licitacao\"\":{\"\"id\"\":9001}}\"\n"),
                bytes("id;nome;contato;observacoes;created_by;created_at;updated_at;resumo\n"
                        + "9401;BLAU ;;;;2026-09-01 09:00:00;2026-09-01 09:10:00;Medicamentos\n"),
                bytes("id;fornecedor_id;nome;marca;preco;created_at;updated_at\n"
                        + "9501;9401;Produto legado;Marca A;1.25;2026-09-01 09:20:00;2026-09-01 09:30:00\n"));

        var primeira = service.importar(arquivos);
        assertThat(primeira.totalImportado()).isEqualTo(6);
        assertThat(primeira.avisos()).anyMatch(a -> a.contains("sem data"));

        var licitacao = licitacoes.findByLegadoId(9001L).orElseThrow();
        assertThat(licitacao.getObjeto()).contains("ponto e\nquebra");
        assertThat(licitacao.getEtapaAtual()).isEqualTo(EtapaLicitacao.COTACAO);
        assertThat(licitacao.isPrecisaAmostra()).isTrue();

        var item = itens.findByLegadoId(9101L).orElseThrow();
        assertThat(item.getLicitacao().getId()).isEqualTo(licitacao.getId());
        assertThat(item.getDecisao()).isEqualTo(DecisaoItem.APROVADO);
        assertThat(item.getQuantidade()).isEqualByComparingTo("100");

        var fornecedor = fornecedores.findByLegadoId(9401L).orElseThrow();
        assertThat(fornecedorItens.findByLegadoId(9501L).orElseThrow().getFornecedor().getId()).isEqualTo(fornecedor.getId());
        var cotacao = cotacoes.findByLegadoId(9201L).orElseThrow();
        assertThat(cotacao.getFornecedor().getId()).isEqualTo(fornecedor.getId());
        assertThat(cotacao.getDataCotacao()).hasToString("2026-09-03");

        var historico = historicos.findByLegadoId(9301L).orElseThrow();
        assertThat(historico.getEtapaOrigem()).isEqualTo(EtapaLicitacao.COTACAO);
        assertThat(historico.getEtapaDestino()).isEqualTo(EtapaLicitacao.APROVACAO);
        assertThat(historico.getSnapshot()).contains("\"id\":9001");

        var segunda = service.importar(arquivos);
        assertThat(segunda.totalImportado()).isZero();
        assertThat(segunda.licitacoes().jaImportados()).isEqualTo(1);
        assertThat(segunda.itens().jaImportados()).isEqualTo(1);
        assertThat(segunda.cotacoes().jaImportados()).isEqualTo(1);
        assertThat(segunda.historicos().jaImportados()).isEqualTo(1);
        assertThat(segunda.fornecedores().jaImportados()).isEqualTo(1);
        assertThat(segunda.fornecedorItens().jaImportados()).isEqualTo(1);
    }

    @Test
    void validaOpcionalmenteOsArquivosReaisFornecidos() throws Exception {
        String diretorio = System.getProperty("legacy.csv.dir");
        Assumptions.assumeTrue(diretorio != null && !diretorio.isBlank());
        var relatorio = service.importar(LegacyCsvImportService.Arquivos.doDiretorio(Path.of(diretorio)));
        assertThat(relatorio.licitacoes().totalProcessado()).isEqualTo(41);
        assertThat(relatorio.itens().totalProcessado()).isEqualTo(160);
        assertThat(relatorio.cotacoes().totalProcessado()).isEqualTo(63);
        assertThat(relatorio.historicos().totalProcessado()).isEqualTo(70);
        assertThat(relatorio.fornecedores().totalProcessado()).isEqualTo(6);
        assertThat(relatorio.fornecedorItens().totalProcessado()).isEqualTo(399);
    }

    private byte[] bytes(String valor) {
        return valor.getBytes(StandardCharsets.UTF_8);
    }
}
