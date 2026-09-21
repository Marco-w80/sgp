package com.sgp.model.licitacao;

import com.sgp.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="lic_fornecedores",indexes=@Index(name="idx_lic_forn_nome",columnList="nome"))
public class FornecedorLicitacao {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(unique=true,length=14) private String cnpj;
    @Column(nullable=false,length=180) private String nome;
    @Column(length=255) private String contato;
    @Column(length=1000) private String resumo;
    @Column(columnDefinition="TEXT") private String observacoes;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="criado_por_id") private Usuario criadoPor;
    @Column(name="criado_em",nullable=false) private LocalDateTime criadoEm;
    @Column(name="atualizado_em",nullable=false) private LocalDateTime atualizadoEm;
    @Column(name="legado_id",unique=true) private Long legadoId;
    @OneToMany(mappedBy="fornecedor",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("nome ASC")
    private List<FornecedorItem> itens=new ArrayList<>();
    @PrePersist void aoCriar(){LocalDateTime a=LocalDateTime.now();criadoEm=criadoEm==null?a:criadoEm;atualizadoEm=atualizadoEm==null?a:atualizadoEm;}
    @PreUpdate void aoAtualizar(){atualizadoEm=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getCnpj(){return cnpj;} public void setCnpj(String cnpj){this.cnpj=cnpj;}
    public String getCnpjFormatado(){return cnpj==null||cnpj.length()!=14?cnpj:cnpj.substring(0,2)+"."+cnpj.substring(2,5)+"."+cnpj.substring(5,8)+"/"+cnpj.substring(8,12)+"-"+cnpj.substring(12);}
    public String getNome(){return nome;} public void setNome(String nome){this.nome=nome;}
    public String getContato(){return contato;} public void setContato(String contato){this.contato=contato;}
    public String getResumo(){return resumo;} public void setResumo(String resumo){this.resumo=resumo;}
    public String getObservacoes(){return observacoes;} public void setObservacoes(String observacoes){this.observacoes=observacoes;}
    public Usuario getCriadoPor(){return criadoPor;} public void setCriadoPor(Usuario criadoPor){this.criadoPor=criadoPor;}
    public LocalDateTime getCriadoEm(){return criadoEm;} public void setCriadoEm(LocalDateTime criadoEm){this.criadoEm=criadoEm;}
    public LocalDateTime getAtualizadoEm(){return atualizadoEm;} public void setAtualizadoEm(LocalDateTime atualizadoEm){this.atualizadoEm=atualizadoEm;}
    public Long getLegadoId(){return legadoId;} public void setLegadoId(Long legadoId){this.legadoId=legadoId;}
    public List<FornecedorItem> getItens(){return itens;}
}
