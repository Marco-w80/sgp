package com.sgp.service.licitacao;

import com.sgp.model.licitacao.*;
import com.sgp.repository.licitacao.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PortfolioService {
    private final PortfolioEstoqueRepository estoque;private final FornecedorLicitacaoRepository fornecedores;private final FornecedorItemRepository itens;private final CotacaoLicitacaoRepository cotacoes;private final UsuarioAtualService usuarioAtual;private final JdbcTemplate jdbc;
    public PortfolioService(PortfolioEstoqueRepository estoque,FornecedorLicitacaoRepository fornecedores,FornecedorItemRepository itens,CotacaoLicitacaoRepository cotacoes,UsuarioAtualService usuarioAtual,JdbcTemplate jdbc){this.estoque=estoque;this.fornecedores=fornecedores;this.itens=itens;this.cotacoes=cotacoes;this.usuarioAtual=usuarioAtual;this.jdbc=jdbc;}
    @Transactional(readOnly=true)public List<PortfolioEstoque> estoque(String q){return estoque.pesquisar(q==null?"":q.trim());}
    @Transactional public PortfolioEstoque salvarEstoque(Long id,String nome,String marca,String fabricante,BigDecimal quantidade,BigDecimal preco){if(nome==null||nome.isBlank())throw new IllegalArgumentException("Nome é obrigatório.");PortfolioEstoque p=id==null?new PortfolioEstoque():estoque.findById(id).orElseThrow();p.setNome(nome);p.setMarca(marca);p.setFabricante(fabricante);p.setQuantidade(quantidade==null?BigDecimal.ZERO:quantidade);p.setPreco(preco);return estoque.save(p);}
    @Transactional public void excluirEstoque(Long id){estoque.deleteById(id);}
    @Transactional(readOnly=true)public List<FornecedorLicitacao> fornecedores(String q){return fornecedores.pesquisar(q==null?"":q.trim());}
    @Transactional public FornecedorLicitacao salvarFornecedor(Long id,String cnpj,String nome,String contato,String resumo,String observacoes){if(nome==null||nome.isBlank())throw new IllegalArgumentException("Nome é obrigatório.");String cnpjNormalizado=CnpjUtil.validarENormalizar(cnpj);if(cnpjNormalizado!=null){var existente=fornecedores.findByCnpj(cnpjNormalizado);if(existente.isPresent()&&!existente.get().getId().equals(id))throw new IllegalArgumentException("Já existe um fornecedor cadastrado com este CNPJ.");}FornecedorLicitacao f=id==null?new FornecedorLicitacao():fornecedores.findById(id).orElseThrow();f.setCnpj(cnpjNormalizado);f.setNome(nome.trim());f.setContato(contato);f.setResumo(resumo);f.setObservacoes(observacoes);if(id==null)f.setCriadoPor(usuarioAtual.obter());return fornecedores.save(f);}
    @Transactional public void excluirFornecedor(Long id){cotacoes.desvincularFornecedor(id);itens.deleteByFornecedorId(id);fornecedores.deleteById(id);}
    @Transactional public FornecedorItem salvarItem(Long fornecedorId,Long id,String nome,String marca,BigDecimal preco){if(nome==null||nome.isBlank())throw new IllegalArgumentException("Nome é obrigatório.");FornecedorLicitacao f=fornecedores.findById(fornecedorId).orElseThrow();FornecedorItem i=id==null?new FornecedorItem():itens.findById(id).orElseThrow();if(id!=null&&!i.getFornecedor().getId().equals(fornecedorId))throw new IllegalArgumentException("Item inválido.");i.setFornecedor(f);i.setNome(nome);i.setMarca(marca);i.setPreco(preco);FornecedorItem salvo=itens.save(i);if(id==null)f.getItens().add(salvo);return salvo;}
    @Transactional public int importarItens(Long fornecedorId,List<ImportacaoTabularService.LinhaFornecedor> linhas){fornecedores.findById(fornecedorId).orElseThrow();LocalDateTime agora=LocalDateTime.now();jdbc.batchUpdate("insert into lic_fornecedor_itens (fornecedor_id,nome,marca,preco,criado_em,atualizado_em) values (?,?,?,?,?,?)",new BatchPreparedStatementSetter(){public void setValues(PreparedStatement ps,int n)throws SQLException{var x=linhas.get(n);ps.setLong(1,fornecedorId);ps.setString(2,x.nome());ps.setString(3,x.marca());ps.setBigDecimal(4,x.preco());ps.setTimestamp(5,Timestamp.valueOf(agora));ps.setTimestamp(6,Timestamp.valueOf(agora));}public int getBatchSize(){return linhas.size();}});return linhas.size();}
    @Transactional public void excluirItem(Long fornecedorId,Long id){FornecedorItem i=itens.findById(id).orElseThrow();if(!i.getFornecedor().getId().equals(fornecedorId))throw new IllegalArgumentException("Item inválido.");itens.delete(i);}
    @Transactional(readOnly=true) public FornecedorLicitacao fornecedor(Long id){return fornecedores.findById(id).orElseThrow();}
}
