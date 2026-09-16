package com.sgp.model.licitacao;

import com.sgp.model.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "lic_licitacoes", indexes = {
        @Index(name = "idx_lic_etapa_arquivada", columnList = "etapa_atual, arquivada"),
        @Index(name = "idx_lic_data_disputa", columnList = "data_disputa")
})
public class Licitacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180) private String orgao;
    @Column(name = "numero_edital", nullable = false, length = 100) private String numeroEdital;
    @Column(length = 50) private String uasg;
    @Column(length = 100) private String modalidade;
    @Column(nullable = false, columnDefinition = "TEXT") private String objeto;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "data_disputa", nullable = false) private LocalDateTime dataDisputa;
    @Column(name = "prazo_entrega", length = 150) private String prazoEntrega;
    @Column(name = "valor_estimado_total", precision = 19, scale = 2) private BigDecimal valorEstimadoTotal;
    @Column(name = "precisa_amostra", nullable = false) private boolean precisaAmostra;
    @Column(name = "link_edital", length = 1000) private String linkEdital;
    @Column(columnDefinition = "TEXT") private String observacoes;
    @Enumerated(EnumType.STRING) @Column(name = "etapa_atual", nullable = false, length = 30)
    private EtapaLicitacao etapaAtual = EtapaLicitacao.CADASTRO;
    @Column(nullable = false) private boolean arquivada;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "criado_por_id") private Usuario criadoPor;
    @Column(name = "criado_em", nullable = false) private LocalDateTime criadoEm;
    @Column(name = "atualizado_em", nullable = false) private LocalDateTime atualizadoEm;
    @Column(name = "legado_id", unique = true) private Long legadoId;

    @OneToMany(mappedBy = "licitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroItem ASC")
    private List<LicitacaoItem> itens = new ArrayList<>();
    @OneToMany(mappedBy = "licitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataHora DESC")
    private List<LicitacaoHistorico> historico = new ArrayList<>();
    @OneToMany(mappedBy = "licitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("enviadoEm DESC")
    private List<LicitacaoAnexo> anexos = new ArrayList<>();

    @PrePersist void aoCriar() { LocalDateTime agora = LocalDateTime.now(); criadoEm = criadoEm == null ? agora : criadoEm; atualizadoEm = agora; }
    @PreUpdate void aoAtualizar() { atualizadoEm = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrgao() { return orgao; }
    public void setOrgao(String orgao) { this.orgao = orgao; }
    public String getNumeroEdital() { return numeroEdital; }
    public void setNumeroEdital(String numeroEdital) { this.numeroEdital = numeroEdital; }
    public String getUasg() { return uasg; }
    public void setUasg(String uasg) { this.uasg = uasg; }
    public String getModalidade() { return modalidade; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }
    public String getObjeto() { return objeto; }
    public void setObjeto(String objeto) { this.objeto = objeto; }
    public LocalDateTime getDataDisputa() { return dataDisputa; }
    public void setDataDisputa(LocalDateTime dataDisputa) { this.dataDisputa = dataDisputa; }
    public String getPrazoEntrega() { return prazoEntrega; }
    public void setPrazoEntrega(String prazoEntrega) { this.prazoEntrega = prazoEntrega; }
    public BigDecimal getValorEstimadoTotal() { return valorEstimadoTotal; }
    public void setValorEstimadoTotal(BigDecimal valorEstimadoTotal) { this.valorEstimadoTotal = valorEstimadoTotal; }
    public boolean isPrecisaAmostra() { return precisaAmostra; }
    public void setPrecisaAmostra(boolean precisaAmostra) { this.precisaAmostra = precisaAmostra; }
    public String getLinkEdital() { return linkEdital; }
    public void setLinkEdital(String linkEdital) { this.linkEdital = linkEdital; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public EtapaLicitacao getEtapaAtual() { return etapaAtual; }
    public void setEtapaAtual(EtapaLicitacao etapaAtual) { this.etapaAtual = etapaAtual; }
    public boolean isArquivada() { return arquivada; }
    public void setArquivada(boolean arquivada) { this.arquivada = arquivada; }
    public Usuario getCriadoPor() { return criadoPor; }
    public void setCriadoPor(Usuario criadoPor) { this.criadoPor = criadoPor; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public Long getLegadoId() { return legadoId; }
    public void setLegadoId(Long legadoId) { this.legadoId = legadoId; }
    public List<LicitacaoItem> getItens() { return itens; }
    public List<LicitacaoHistorico> getHistorico() { return historico; }
    public List<LicitacaoAnexo> getAnexos() { return anexos; }
}
