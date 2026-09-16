package com.sgp.model.licitacao;

import com.sgp.model.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="lic_cotacoes", indexes={@Index(name="idx_lic_cot_item",columnList="item_id"),@Index(name="idx_lic_cot_fornecedor",columnList="fornecedor_id")})
public class CotacaoLicitacao {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="item_id",nullable=false) private LicitacaoItem item;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="fornecedor_id") private FornecedorLicitacao fornecedor;
    @Column(name="fornecedor_nome",nullable=false,length=180) private String fornecedorNome;
    @Column(name="valor_unitario",nullable=false,precision=19,scale=4) private BigDecimal valorUnitario;
    @Column(name="data_cotacao",nullable=false) private LocalDate dataCotacao;
    @Column(columnDefinition="TEXT") private String observacoes;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="registrado_por_id") private Usuario registradoPor;
    @Column(name="criado_em",nullable=false) private LocalDateTime criadoEm;
    @Column(name="legado_id",unique=true) private Long legadoId;
    @PrePersist void aoCriar(){if(criadoEm==null)criadoEm=LocalDateTime.now();if(dataCotacao==null)dataCotacao=LocalDate.now();}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public LicitacaoItem getItem(){return item;} public void setItem(LicitacaoItem item){this.item=item;}
    public FornecedorLicitacao getFornecedor(){return fornecedor;} public void setFornecedor(FornecedorLicitacao fornecedor){this.fornecedor=fornecedor;}
    public String getFornecedorNome(){return fornecedorNome;} public void setFornecedorNome(String fornecedorNome){this.fornecedorNome=fornecedorNome;}
    public BigDecimal getValorUnitario(){return valorUnitario;} public void setValorUnitario(BigDecimal valorUnitario){this.valorUnitario=valorUnitario;}
    public LocalDate getDataCotacao(){return dataCotacao;} public void setDataCotacao(LocalDate dataCotacao){this.dataCotacao=dataCotacao;}
    public String getObservacoes(){return observacoes;} public void setObservacoes(String observacoes){this.observacoes=observacoes;}
    public Usuario getRegistradoPor(){return registradoPor;} public void setRegistradoPor(Usuario registradoPor){this.registradoPor=registradoPor;}
    public LocalDateTime getCriadoEm(){return criadoEm;} public void setCriadoEm(LocalDateTime criadoEm){this.criadoEm=criadoEm;}
    public Long getLegadoId(){return legadoId;} public void setLegadoId(Long legadoId){this.legadoId=legadoId;}
}
