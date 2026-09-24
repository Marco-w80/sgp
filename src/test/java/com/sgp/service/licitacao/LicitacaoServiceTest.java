package com.sgp.service.licitacao;

import com.sgp.model.licitacao.*;
import com.sgp.repository.licitacao.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class LicitacaoServiceTest {
 @Autowired LicitacaoService service;@Autowired LicitacaoRepository licitacoes;@Autowired LicitacaoItemRepository itens;@Autowired LicitacaoHistoricoRepository historicos;@Autowired CotacaoLicitacaoRepository cotacoes;@Autowired ExportacaoCotacaoService exportacao;@Autowired ImportacaoTabularService importacao;
 private Licitacao nova(String edital,LocalDateTime disputa){Licitacao l=new Licitacao();l.setOrgao("Órgão Teste");l.setNumeroEdital(edital);l.setObjeto("Aquisição de equipamentos");l.setDataDisputa(disputa);return service.criar(l);}
 @Test void criaEditaConsultaEArquiva(){Licitacao l=nova("1/2026",LocalDateTime.now().plusDays(10));Licitacao dados=novaDados("2/2026");service.editar(l.getId(),dados);assertThat(service.obter(l.getId()).getNumeroEdital()).isEqualTo("2/2026");assertThatThrownBy(()->service.arquivar(l.getId(),true)).isInstanceOf(IllegalStateException.class);Long item=service.salvarItem(l.getId(),null,1,"Item",BigDecimal.ONE,"UN",BigDecimal.TEN).getId();service.selecionarItensETransicionarParaCotacao(l.getId(),java.util.List.of(item),null);service.transicionar(l.getId(),EtapaLicitacao.APROVACAO,null);service.decidirItem(l.getId(),item,DecisaoItem.APROVADO,BigDecimal.TEN,BigDecimal.ZERO,null);service.transicionar(l.getId(),EtapaLicitacao.DEFINICAO,null);service.arquivar(l.getId(),true);assertThat(service.obter(l.getId()).isArquivada()).isTrue();}
 @Test void respeitaFluxoEExigeDecisao(){Licitacao l=nova("3/2026",LocalDateTime.now().plusDays(10));assertThatThrownBy(()->service.transicionar(l.getId(),EtapaLicitacao.APROVACAO,null)).isInstanceOf(IllegalArgumentException.class);Long item=service.salvarItem(l.getId(),null,1,"Item",BigDecimal.ONE,"UN",BigDecimal.TEN).getId();service.selecionarItensETransicionarParaCotacao(l.getId(),java.util.List.of(item),null);service.transicionar(l.getId(),EtapaLicitacao.APROVACAO,null);assertThatThrownBy(()->service.transicionar(l.getId(),EtapaLicitacao.DEFINICAO,null)).isInstanceOf(IllegalStateException.class);service.decidirItem(l.getId(),item,DecisaoItem.APROVADO,BigDecimal.TEN,BigDecimal.ZERO,"Lance mínimo");service.transicionar(l.getId(),EtapaLicitacao.DEFINICAO,null);service.transicionar(l.getId(),EtapaLicitacao.PARTICIPACAO,null);service.resultado(l.getId(),item,ResultadoItem.GANHO,new BigDecimal("9.5"),null);assertThat(service.obter(l.getId()).getEtapaAtual()).isEqualTo(EtapaLicitacao.PARTICIPACAO);assertThat(historicos.count()).isEqualTo(4);service.transicionar(l.getId(),EtapaLicitacao.DEFINICAO,"retorno");assertThat(service.obter(l.getId()).getEtapaAtual()).isEqualTo(EtapaLicitacao.DEFINICAO);}
 @Test void ignoraItensNaoSelecionadosNasEtapasSeguintes(){Licitacao l=nova("8/2026",LocalDateTime.now().plusDays(10));Long escolhido=service.salvarItem(l.getId(),null,1,"Escolhido",BigDecimal.ONE,"UN",BigDecimal.TEN).getId();Long naoEscolhido=service.salvarItem(l.getId(),null,2,"Não escolhido",BigDecimal.ONE,"UN",BigDecimal.TEN).getId();assertThatThrownBy(()->service.transicionar(l.getId(),EtapaLicitacao.COTACAO,null)).isInstanceOf(IllegalStateException.class);service.selecionarItensETransicionarParaCotacao(l.getId(),java.util.List.of(escolhido),null);assertThat(itens.findById(escolhido).orElseThrow().isSelecionadoCotacao()).isTrue();assertThat(itens.findById(naoEscolhido).orElseThrow().isSelecionadoCotacao()).isFalse();service.transicionar(l.getId(),EtapaLicitacao.APROVACAO,null);service.decidirItem(l.getId(),escolhido,DecisaoItem.APROVADO,BigDecimal.TEN,BigDecimal.ZERO,null);assertThatThrownBy(()->service.decidirItem(l.getId(),naoEscolhido,DecisaoItem.APROVADO,BigDecimal.TEN,BigDecimal.ZERO,null)).isInstanceOf(IllegalStateException.class);service.transicionar(l.getId(),EtapaLicitacao.DEFINICAO,null);assertThat(service.obter(l.getId()).getEtapaAtual()).isEqualTo(EtapaLicitacao.DEFINICAO);}
 @Test void dashboardPesquisaItemEClassificaAndamento(){Licitacao l=nova("4/2026",LocalDateTime.now().minusMinutes(1));service.salvarItem(l.getId(),null,7,"Microscópio especial",BigDecimal.ONE,"UN",null);var d=service.dashboard("Microscópio",false);assertThat(d.total()).isEqualTo(1);assertThat(d.emAndamento()).isEqualTo(1);assertThat(d.colunas().get("EM_ANDAMENTO")).hasSize(1);assertThat(service.urgencia(-1)).isEqualTo(LicitacaoService.Urgencia.ATRASADO);}
 @Test void dashboardContaIndicadoresDentroDaBusca(){
  Licitacao hoje=nova("indicadores-hoje",LocalDate.now().atTime(12,0));
  hoje.setPrecisaAmostra(true);
  Licitacao futura=nova("indicadores-futura",LocalDate.now().plusDays(2).atTime(12,0));
  service.salvarItem(futura.getId(),null,1,"Item cadastrado",BigDecimal.ONE,"UN",null);
  var todos=service.dashboard("indicadores-",false);
  assertThat(todos.total()).isEqualTo(2);
  assertThat(todos.disputasHoje()).isEqualTo(1);
  assertThat(todos.exigemAmostra()).isEqualTo(1);
  assertThat(todos.semItens()).isEqualTo(1);
  var filtrado=service.dashboard("indicadores-futura",false);
  assertThat(filtrado.total()).isEqualTo(1);
  assertThat(filtrado.disputasHoje()).isZero();
  assertThat(filtrado.exigemAmostra()).isZero();
  assertThat(filtrado.semItens()).isZero();
 }
 @Test void excluiEmCascata(){Licitacao l=nova("5/2026",LocalDateTime.now().plusDays(1));service.salvarItem(l.getId(),null,1,"Item",BigDecimal.ONE,"UN",null);service.excluir(l.getId());assertThat(licitacoes.findById(l.getId())).isEmpty();}
 @Test void criaEditaExcluiEExportaCotacao(){Licitacao l=nova("6/2026",LocalDateTime.now().plusDays(1));Long item=service.salvarItem(l.getId(),null,1,"Item",new BigDecimal("150.0000"),"UN",new BigDecimal("1234.5000")).getId();var c=service.salvarCotacao(l.getId(),item,null,null,"Fornecedor X",new BigDecimal("12.50"),java.time.LocalDate.now(),"teste");service.salvarCotacao(l.getId(),item,c.getId(),null,"Fornecedor Y",new BigDecimal("11.50"),java.time.LocalDate.now(),"editada");assertThat(cotacoes.findById(c.getId()).orElseThrow().getFornecedorNome()).isEqualTo("Fornecedor Y");String csv=new String(exportacao.csv(service.obter(l.getId())),java.nio.charset.StandardCharsets.UTF_8);assertThat(csv).contains(";150;UN;1234,5;Fornecedor Y;11,5;").doesNotContain("150.0000","1234.5000").startsWith("\uFEFF");service.excluirCotacao(l.getId(),item,c.getId());assertThat(cotacoes.findById(c.getId())).isEmpty();}
 @Test void importacaoEmLoteContinuaNumeracaoExistente(){Licitacao l=nova("7/2026",LocalDateTime.now().plusDays(1));service.salvarItem(l.getId(),null,5,"Existente",BigDecimal.ONE,"UN",null);var linhas=importacao.lerItens("Novo\t2",ImportacaoTabularService.Delimitador.TAB,false,-1,0,1,-1,-1);service.importarItens(l.getId(),linhas);assertThat(itens.findByLicitacaoIdOrderByNumeroItem(l.getId())).extracting(LicitacaoItem::getNumeroItem).containsExactly(5,6);}
 private Licitacao novaDados(String edital){Licitacao l=new Licitacao();l.setOrgao("Outro órgão");l.setNumeroEdital(edital);l.setObjeto("Outro objeto");l.setDataDisputa(LocalDateTime.now().plusDays(2));return l;}
}
