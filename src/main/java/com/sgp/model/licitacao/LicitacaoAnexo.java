package com.sgp.model.licitacao;

import com.sgp.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="lic_anexos",indexes={@Index(name="idx_lic_anexo_licitacao",columnList="licitacao_id"),@Index(name="idx_lic_anexo_item",columnList="item_id")})
public class LicitacaoAnexo {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="licitacao_id",nullable=false) private Licitacao licitacao;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="item_id") private LicitacaoItem item;
    @Enumerated(EnumType.STRING) @Column(length=30) private EtapaLicitacao etapa;
    @Column(name="nome_original",nullable=false,length=255) private String nomeOriginal;
    @Column(name="nome_armazenado",nullable=false,unique=true,length=255) private String nomeArmazenado;
    @Column(nullable=false,length=1000) private String caminho;
    @Column(name="mime_type",nullable=false,length=150) private String mimeType;
    @Column(nullable=false) private Long tamanho;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="enviado_por_id") private Usuario enviadoPor;
    @Column(name="enviado_em",nullable=false) private LocalDateTime enviadoEm;
    @Column(name="legado_id",unique=true) private Long legadoId;
    @PrePersist void aoCriar(){if(enviadoEm==null)enviadoEm=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Licitacao getLicitacao(){return licitacao;} public void setLicitacao(Licitacao licitacao){this.licitacao=licitacao;}
    public LicitacaoItem getItem(){return item;} public void setItem(LicitacaoItem item){this.item=item;}
    public EtapaLicitacao getEtapa(){return etapa;} public void setEtapa(EtapaLicitacao etapa){this.etapa=etapa;}
    public String getNomeOriginal(){return nomeOriginal;} public void setNomeOriginal(String nomeOriginal){this.nomeOriginal=nomeOriginal;}
    public String getNomeArmazenado(){return nomeArmazenado;} public void setNomeArmazenado(String nomeArmazenado){this.nomeArmazenado=nomeArmazenado;}
    public String getCaminho(){return caminho;} public void setCaminho(String caminho){this.caminho=caminho;}
    public String getMimeType(){return mimeType;} public void setMimeType(String mimeType){this.mimeType=mimeType;}
    public Long getTamanho(){return tamanho;} public void setTamanho(Long tamanho){this.tamanho=tamanho;}
    public Usuario getEnviadoPor(){return enviadoPor;} public void setEnviadoPor(Usuario enviadoPor){this.enviadoPor=enviadoPor;}
    public LocalDateTime getEnviadoEm(){return enviadoEm;} public void setEnviadoEm(LocalDateTime enviadoEm){this.enviadoEm=enviadoEm;}
    public Long getLegadoId(){return legadoId;} public void setLegadoId(Long legadoId){this.legadoId=legadoId;}
}
