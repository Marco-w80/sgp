package com.sgp.service.licitacao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgp.model.Usuario;
import com.sgp.model.licitacao.*;
import com.sgp.repository.licitacao.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class LicitacaoService {
    public enum Urgencia { ATRASADO, CRITICO, ATENCAO, NO_PRAZO }
    public record KanbanCard(Licitacao licitacao,Urgencia urgencia,long dias){}
    public record Dashboard(Map<String,List<KanbanCard>> colunas,long total,long emAndamento,long urgentes,long disputasHoje,long exigemAmostra,long semItens){}
    private final LicitacaoRepository licitacoes; private final LicitacaoItemRepository itens; private final CotacaoLicitacaoRepository cotacoes;
    private final LicitacaoHistoricoRepository historicos; private final ObjectMapper json; private final UsuarioAtualService usuarioAtual; private final LicitacaoAnexoService anexos; private final JdbcTemplate jdbc;
    public LicitacaoService(LicitacaoRepository licitacoes,LicitacaoItemRepository itens,CotacaoLicitacaoRepository cotacoes,LicitacaoHistoricoRepository historicos,ObjectMapper json,UsuarioAtualService usuarioAtual,LicitacaoAnexoService anexos,JdbcTemplate jdbc){this.licitacoes=licitacoes;this.itens=itens;this.cotacoes=cotacoes;this.historicos=historicos;this.json=json;this.usuarioAtual=usuarioAtual;this.anexos=anexos;this.jdbc=jdbc;}

    @Transactional(readOnly=true) public Licitacao obter(Long id){return licitacoes.findById(id).orElseThrow(()->new EntityNotFoundException("Licitação não encontrada"));}
    @Transactional public Licitacao criar(Licitacao l){return criarComItens(l,List.of());}
    @Transactional public Licitacao criarComItens(Licitacao l,List<ImportacaoTabularService.LinhaImportada> linhas){validar(l);l.setCriadoPor(usuarioAtual.obter());l.setEtapaAtual(EtapaLicitacao.CADASTRO);l.setArquivada(false);Licitacao salva=licitacoes.save(l);if(linhas!=null&&!linhas.isEmpty())importarItens(salva.getId(),linhas);return salva;}
    @Transactional public Licitacao editar(Long id,Licitacao dados){Licitacao l=obter(id);validar(dados);l.setOrgao(dados.getOrgao());l.setNumeroEdital(dados.getNumeroEdital());l.setUasg(dados.getUasg());l.setModalidade(dados.getModalidade());l.setObjeto(dados.getObjeto());l.setDataDisputa(dados.getDataDisputa());l.setPrazoEntrega(dados.getPrazoEntrega());l.setValorEstimadoTotal(dados.getValorEstimadoTotal());l.setPrecisaAmostra(dados.isPrecisaAmostra());l.setLinkEdital(dados.getLinkEdital());l.setObservacoes(dados.getObservacoes());return l;}
    private void validar(Licitacao l){if(l.getOrgao()==null||l.getOrgao().isBlank()||l.getNumeroEdital()==null||l.getNumeroEdital().isBlank()||l.getObjeto()==null||l.getObjeto().isBlank()||l.getDataDisputa()==null)throw new IllegalArgumentException("Órgão, edital, objeto e data/hora da disputa são obrigatórios.");}

    @Transactional public void transicionar(Long id,EtapaLicitacao destino,String observacoes){Licitacao l=obter(id);if(l.getEtapaAtual()==EtapaLicitacao.CADASTRO&&destino==EtapaLicitacao.COTACAO)throw new IllegalStateException("Abra a licitação e selecione os itens que seguirão para Cotação.");transicionarInterno(id,destino,observacoes);}
    @Transactional public void selecionarItensETransicionarParaCotacao(Long id,List<Long> itemIds,String observacoes){Licitacao l=obter(id);if(l.getEtapaAtual()!=EtapaLicitacao.CADASTRO||itemIds==null||itemIds.isEmpty())throw new IllegalArgumentException("Selecione ao menos um item para avançar para Cotação.");List<LicitacaoItem> lista=itens.findByLicitacaoIdOrderByNumeroItem(id);Set<Long> selecionados=new HashSet<>(itemIds);Set<Long> idsDaLicitacao=new HashSet<>();for(LicitacaoItem item:lista)idsDaLicitacao.add(item.getId());if(!idsDaLicitacao.containsAll(selecionados))throw new IllegalArgumentException("Há itens selecionados que não pertencem à licitação.");for(LicitacaoItem item:lista)item.setSelecionadoCotacao(selecionados.contains(item.getId()));transicionarInterno(id,EtapaLicitacao.COTACAO,observacoes);}
    private void transicionarInterno(Long id,EtapaLicitacao destino,String observacoes){Licitacao l=obter(id);EtapaLicitacao origem=l.getEtapaAtual();if(!origem.adjacenteA(destino))throw new IllegalArgumentException("A licitação só pode avançar ou retornar uma etapa.");if(origem==EtapaLicitacao.APROVACAO&&destino==EtapaLicitacao.DEFINICAO&&itens.existsByLicitacaoIdAndSelecionadoCotacaoTrueAndDecisao(id,DecisaoItem.PENDENTE))throw new IllegalStateException("Todos os itens selecionados precisam ser aprovados ou reprovados antes de enviar para Definição.");LicitacaoHistorico h=new LicitacaoHistorico();h.setLicitacao(l);h.setEtapaOrigem(origem);h.setEtapaDestino(destino);h.setUsuario(usuarioAtual.obter());h.setObservacoes(observacoes);h.setSnapshot(snapshot(l,itens.findByLicitacaoIdOrderByNumeroItem(id)));historicos.save(h);l.setEtapaAtual(destino);}
    private String snapshot(Licitacao l,List<LicitacaoItem> itensAtuais){Map<String,Object> s=new LinkedHashMap<>();s.put("id",l.getId());s.put("orgao",l.getOrgao());s.put("edital",l.getNumeroEdital());s.put("etapa",l.getEtapaAtual());s.put("dataDisputa",l.getDataDisputa());s.put("itens",itensAtuais.stream().map(i->Map.of("id",i.getId(),"numero",i.getNumeroItem(),"descricao",i.getDescricao(),"selecionadoCotacao",i.isSelecionadoCotacao(),"decisao",i.getDecisao().name())).toList());try{return json.writeValueAsString(s);}catch(JsonProcessingException e){return "{\"erro\":\"snapshot indisponível\"}";}}
    @Transactional public void arquivar(Long id,boolean valor){Licitacao l=obter(id);if(valor&&l.getEtapaAtual()!=EtapaLicitacao.DEFINICAO)throw new IllegalStateException("A licitação só pode ser arquivada na etapa Definição.");l.setArquivada(valor);}
    @Transactional public void excluir(Long id){Licitacao l=obter(id);anexos.removerArquivosDaLicitacao(l.getId());licitacoes.delete(l);}

    @Transactional public LicitacaoItem salvarItem(Long licitacaoId,Long itemId,Integer numero,String descricao,BigDecimal quantidade,String unidade,BigDecimal valor){Licitacao l=obter(licitacaoId);if(descricao==null||descricao.isBlank())throw new IllegalArgumentException("Descrição é obrigatória.");LicitacaoItem i=itemId==null?new LicitacaoItem():itens.findById(itemId).orElseThrow();if(itemId!=null&&!i.getLicitacao().getId().equals(licitacaoId))throw new IllegalArgumentException("Item não pertence à licitação.");i.setLicitacao(l);i.setNumeroItem(numero==null?proximoNumero(l):numero);i.setDescricao(descricao);i.setQuantidade(quantidade==null?BigDecimal.ONE:quantidade);i.setUnidade(unidade==null||unidade.isBlank()?"UN":unidade);i.setValorReferencia(valor);LicitacaoItem salvo=itens.save(i);if(itemId==null)l.getItens().add(salvo);return salvo;}
    private int proximoNumero(Licitacao l){return l.getItens().stream().map(LicitacaoItem::getNumeroItem).filter(Objects::nonNull).max(Integer::compareTo).orElse(0)+1;}
    @Transactional public int importarItens(Long licitacaoId,List<ImportacaoTabularService.LinhaImportada> linhas){obter(licitacaoId);List<LicitacaoItem> existentes=itens.findByLicitacaoIdOrderByNumeroItem(licitacaoId);int maior=existentes.stream().map(LicitacaoItem::getNumeroItem).filter(Objects::nonNull).max(Integer::compareTo).orElse(0);maior=Math.max(maior,linhas.stream().filter(x->!x.numeroGerado()).map(ImportacaoTabularService.LinhaImportada::numero).max(Integer::compareTo).orElse(0));List<ImportacaoTabularService.LinhaImportada> resolvidas=new ArrayList<>(linhas.size());int proximo=maior+1;for(var x:linhas)resolvidas.add(x.numeroGerado()?new ImportacaoTabularService.LinhaImportada(proximo++,x.descricao(),x.quantidade(),x.unidade(),x.valor(),true):x);LocalDateTime agora=LocalDateTime.now();jdbc.batchUpdate("insert into lic_itens (licitacao_id,numero_item,descricao,quantidade,unidade,valor_referencia,selecionado_cotacao,decisao,criado_em,atualizado_em) values (?,?,?,?,?,?,true,'PENDENTE',?,?)",new BatchPreparedStatementSetter(){public void setValues(PreparedStatement ps,int n)throws SQLException{var x=resolvidas.get(n);ps.setLong(1,licitacaoId);ps.setInt(2,x.numero());ps.setString(3,x.descricao());ps.setBigDecimal(4,x.quantidade());ps.setString(5,x.unidade());ps.setBigDecimal(6,x.valor());ps.setTimestamp(7,Timestamp.valueOf(agora));ps.setTimestamp(8,Timestamp.valueOf(agora));}public int getBatchSize(){return resolvidas.size();}});return resolvidas.size();}
    @Transactional public void excluirItem(Long licitacaoId,Long itemId){LicitacaoItem i=itens.findById(itemId).orElseThrow();if(!i.getLicitacao().getId().equals(licitacaoId))throw new IllegalArgumentException("Item não pertence à licitação.");anexos.removerArquivosDoItem(itemId);itens.delete(i);}
    @Transactional public void decidirItem(Long licitacaoId,Long itemId,DecisaoItem decisao,BigDecimal precoMaximo,BigDecimal desconto,String estrategia){LicitacaoItem i=itemSelecionadoDaLicitacao(licitacaoId,itemId);i.setPrecoMaximo(precoMaximo);i.setPercentualDesconto(desconto);i.setEstrategiaLance(estrategia);i.setDecisao(decisao==null?DecisaoItem.PENDENTE:decisao);}
    @Transactional public void resultado(Long licitacaoId,Long itemId,ResultadoItem resultado,BigDecimal precoFinal,String motivo){LicitacaoItem i=itemSelecionadoDaLicitacao(licitacaoId,itemId);if(i.getDecisao()!=DecisaoItem.APROVADO)throw new IllegalStateException("Somente itens aprovados podem receber resultado.");i.setResultado(resultado);i.setPrecoFinal(precoFinal);i.setMotivoPerda(motivo);}
    private LicitacaoItem itemDaLicitacao(Long licitacaoId,Long itemId){LicitacaoItem i=itens.findById(itemId).orElseThrow();if(!i.getLicitacao().getId().equals(licitacaoId))throw new IllegalArgumentException("Item não pertence à licitação.");return i;}
    private LicitacaoItem itemSelecionadoDaLicitacao(Long licitacaoId,Long itemId){LicitacaoItem i=itemDaLicitacao(licitacaoId,itemId);if(!i.isSelecionadoCotacao())throw new IllegalStateException("Item não foi selecionado para Cotação.");return i;}

    @Transactional public CotacaoLicitacao salvarCotacao(Long licitacaoId,Long itemId,Long cotacaoId,FornecedorLicitacao fornecedor,String fornecedorNome,BigDecimal valor,LocalDate data,String obs){LicitacaoItem item=itemSelecionadoDaLicitacao(licitacaoId,itemId);if(valor==null||valor.signum()<0)throw new IllegalArgumentException("Valor da cotação é obrigatório.");CotacaoLicitacao c=cotacaoId==null?new CotacaoLicitacao():cotacoes.findById(cotacaoId).orElseThrow();if(cotacaoId!=null&&!c.getItem().getId().equals(itemId))throw new IllegalArgumentException("Cotação não pertence ao item.");c.setItem(item);c.setFornecedor(fornecedor);c.setFornecedorNome(fornecedor!=null?fornecedor.getNome():fornecedorNome);if(c.getFornecedorNome()==null||c.getFornecedorNome().isBlank())throw new IllegalArgumentException("Fornecedor é obrigatório.");c.setValorUnitario(valor);c.setDataCotacao(data);c.setObservacoes(obs);if(cotacaoId==null)c.setRegistradoPor(usuarioAtual.obter());CotacaoLicitacao salva=cotacoes.save(c);if(cotacaoId==null)item.getCotacoes().add(salva);return salva;}
    @Transactional public void excluirCotacao(Long licitacaoId,Long itemId,Long cotacaoId){CotacaoLicitacao c=cotacoes.findById(cotacaoId).orElseThrow();if(!c.getItem().getId().equals(itemId)||!c.getItem().getLicitacao().getId().equals(licitacaoId))throw new IllegalArgumentException("Cotação inválida.");c.getItem().getCotacoes().remove(c);cotacoes.delete(c);}

    @Transactional(readOnly=true) public Dashboard dashboard(String busca,boolean arquivadas){
        List<Licitacao> lista=licitacoes.pesquisar(busca==null?"":busca.trim(),arquivadas);
        Map<String,List<KanbanCard>> col=new LinkedHashMap<>();
        for(EtapaLicitacao e:EtapaLicitacao.values())col.put(e.name(),new ArrayList<>());
        col.put("EM_ANDAMENTO",new ArrayList<>());
        LocalDateTime agora=LocalDateTime.now();
        LocalDate hoje=agora.toLocalDate();
        long andamento=0,urgentes=0,disputasHoje=0,exigemAmostra=0,semItens=0;
        for(Licitacao l:lista){
            LocalDate diaDisputa=l.getDataDisputa().toLocalDate();
            long dias=ChronoUnit.DAYS.between(hoje,diaDisputa);
            Urgencia u=urgencia(dias);
            if(dias>=0&&dias<=7)urgentes++;
            if(diaDisputa.equals(hoje))disputasHoje++;
            if(l.isPrecisaAmostra())exigemAmostra++;
            if(l.getItens().isEmpty())semItens++;
            String chave=!l.getDataDisputa().isAfter(agora)?"EM_ANDAMENTO":l.getEtapaAtual().name();
            if(chave.equals("EM_ANDAMENTO"))andamento++;
            col.get(chave).add(new KanbanCard(l,u,dias));
        }
        return new Dashboard(col,lista.size(),andamento,urgentes,disputasHoje,exigemAmostra,semItens);
    }
    public Urgencia urgencia(long dias){if(dias<0)return Urgencia.ATRASADO;if(dias<=3)return Urgencia.CRITICO;if(dias<=7)return Urgencia.ATENCAO;return Urgencia.NO_PRAZO;}
}
