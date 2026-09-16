package com.sgp.model.licitacao;

import com.sgp.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="lic_historicos",indexes=@Index(name="idx_lic_hist_licitacao_data",columnList="licitacao_id,data_hora"))
public class LicitacaoHistorico {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="licitacao_id",nullable=false) private Licitacao licitacao;
    @Enumerated(EnumType.STRING) @Column(name="etapa_origem",length=30) private EtapaLicitacao etapaOrigem;
    @Enumerated(EnumType.STRING) @Column(name="etapa_destino",nullable=false,length=30) private EtapaLicitacao etapaDestino;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="usuario_id") private Usuario usuario;
    @Column(name="data_hora",nullable=false) private LocalDateTime dataHora;
    @Column(length=1000) private String observacoes;
    @Column(columnDefinition="LONGTEXT") private String snapshot;
    @Column(name="legado_id",unique=true) private Long legadoId;
    @PrePersist void aoCriar(){if(dataHora==null)dataHora=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Licitacao getLicitacao(){return licitacao;} public void setLicitacao(Licitacao licitacao){this.licitacao=licitacao;}
    public EtapaLicitacao getEtapaOrigem(){return etapaOrigem;} public void setEtapaOrigem(EtapaLicitacao etapaOrigem){this.etapaOrigem=etapaOrigem;}
    public EtapaLicitacao getEtapaDestino(){return etapaDestino;} public void setEtapaDestino(EtapaLicitacao etapaDestino){this.etapaDestino=etapaDestino;}
    public Usuario getUsuario(){return usuario;} public void setUsuario(Usuario usuario){this.usuario=usuario;}
    public LocalDateTime getDataHora(){return dataHora;} public void setDataHora(LocalDateTime dataHora){this.dataHora=dataHora;}
    public String getObservacoes(){return observacoes;} public void setObservacoes(String observacoes){this.observacoes=observacoes;}
    public String getSnapshot(){return snapshot;} public void setSnapshot(String snapshot){this.snapshot=snapshot;}
    public Long getLegadoId(){return legadoId;} public void setLegadoId(Long legadoId){this.legadoId=legadoId;}
}
