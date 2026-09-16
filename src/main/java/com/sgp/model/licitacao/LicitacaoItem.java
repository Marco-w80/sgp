package com.sgp.model.licitacao;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lic_itens", indexes = @Index(name = "idx_lic_item_licitacao", columnList = "licitacao_id"),
       uniqueConstraints = @UniqueConstraint(name = "uk_lic_item_numero", columnNames = {"licitacao_id", "numero_item"}))
public class LicitacaoItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "licitacao_id", nullable = false) private Licitacao licitacao;
    @Column(name = "numero_item", nullable = false) private Integer numeroItem;
    @Column(nullable = false, columnDefinition = "TEXT") private String descricao;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantidade;
    @Column(nullable = false, length = 30) private String unidade;
    @Column(name = "valor_referencia", precision = 19, scale = 4) private BigDecimal valorReferencia;
    @Column(name = "selecionado_cotacao", nullable = false, columnDefinition = "BIT DEFAULT 1") private boolean selecionadoCotacao = true;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private DecisaoItem decisao = DecisaoItem.PENDENTE;
    @Column(name = "preco_maximo", precision = 19, scale = 4) private BigDecimal precoMaximo;
    @Column(name = "percentual_desconto", precision = 8, scale = 4) private BigDecimal percentualDesconto;
    @Column(name = "estrategia_lance", length = 500) private String estrategiaLance;
    @Enumerated(EnumType.STRING) @Column(length = 20) private ResultadoItem resultado;
    @Column(name = "preco_final", precision = 19, scale = 4) private BigDecimal precoFinal;
    @Column(name = "motivo_perda", length = 1000) private String motivoPerda;
    @Column(name = "criado_em", nullable = false) private LocalDateTime criadoEm;
    @Column(name = "atualizado_em", nullable = false) private LocalDateTime atualizadoEm;
    @Column(name = "legado_id", unique = true) private Long legadoId;
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true) @OrderBy("dataCotacao DESC")
    private List<CotacaoLicitacao> cotacoes = new ArrayList<>();

    @PrePersist void aoCriar() { LocalDateTime agora=LocalDateTime.now(); criadoEm=criadoEm==null?agora:criadoEm; atualizadoEm=agora; if(quantidade==null) quantidade=BigDecimal.ONE; if(unidade==null||unidade.isBlank()) unidade="UN"; }
    @PreUpdate void aoAtualizar() { atualizadoEm=LocalDateTime.now(); }
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Licitacao getLicitacao(){return licitacao;} public void setLicitacao(Licitacao licitacao){this.licitacao=licitacao;}
    public Integer getNumeroItem(){return numeroItem;} public void setNumeroItem(Integer numeroItem){this.numeroItem=numeroItem;}
    public String getDescricao(){return descricao;} public void setDescricao(String descricao){this.descricao=descricao;}
    public BigDecimal getQuantidade(){return quantidade;} public void setQuantidade(BigDecimal quantidade){this.quantidade=quantidade;}
    public String getUnidade(){return unidade;} public void setUnidade(String unidade){this.unidade=unidade;}
    public BigDecimal getValorReferencia(){return valorReferencia;} public void setValorReferencia(BigDecimal valorReferencia){this.valorReferencia=valorReferencia;}
    public boolean isSelecionadoCotacao(){return selecionadoCotacao;} public void setSelecionadoCotacao(boolean selecionadoCotacao){this.selecionadoCotacao=selecionadoCotacao;}
    public DecisaoItem getDecisao(){return decisao;} public void setDecisao(DecisaoItem decisao){this.decisao=decisao;}
    public BigDecimal getPrecoMaximo(){return precoMaximo;} public void setPrecoMaximo(BigDecimal precoMaximo){this.precoMaximo=precoMaximo;}
    public BigDecimal getPercentualDesconto(){return percentualDesconto;} public void setPercentualDesconto(BigDecimal percentualDesconto){this.percentualDesconto=percentualDesconto;}
    public String getEstrategiaLance(){return estrategiaLance;} public void setEstrategiaLance(String estrategiaLance){this.estrategiaLance=estrategiaLance;}
    public ResultadoItem getResultado(){return resultado;} public void setResultado(ResultadoItem resultado){this.resultado=resultado;}
    public BigDecimal getPrecoFinal(){return precoFinal;} public void setPrecoFinal(BigDecimal precoFinal){this.precoFinal=precoFinal;}
    public String getMotivoPerda(){return motivoPerda;} public void setMotivoPerda(String motivoPerda){this.motivoPerda=motivoPerda;}
    public LocalDateTime getCriadoEm(){return criadoEm;} public void setCriadoEm(LocalDateTime criadoEm){this.criadoEm=criadoEm;}
    public LocalDateTime getAtualizadoEm(){return atualizadoEm;} public void setAtualizadoEm(LocalDateTime atualizadoEm){this.atualizadoEm=atualizadoEm;}
    public Long getLegadoId(){return legadoId;} public void setLegadoId(Long legadoId){this.legadoId=legadoId;}
    public List<CotacaoLicitacao> getCotacoes(){return cotacoes;}
}
