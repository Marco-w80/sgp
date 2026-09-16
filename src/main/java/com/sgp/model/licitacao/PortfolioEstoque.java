package com.sgp.model.licitacao;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="lic_portfolio_estoque",indexes=@Index(name="idx_lic_port_nome",columnList="nome"))
public class PortfolioEstoque {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=255) private String nome;
    @Column(length=120) private String marca;
    @Column(length=180) private String fabricante;
    @Column(nullable=false,precision=19,scale=4) private BigDecimal quantidade=BigDecimal.ZERO;
    @Column(precision=19,scale=4) private BigDecimal preco;
    @Column(name="criado_em",nullable=false) private LocalDateTime criadoEm;
    @Column(name="atualizado_em",nullable=false) private LocalDateTime atualizadoEm;
    @Column(name="legado_id",unique=true) private Long legadoId;
    @PrePersist void aoCriar(){LocalDateTime a=LocalDateTime.now();criadoEm=criadoEm==null?a:criadoEm;atualizadoEm=a;}
    @PreUpdate void aoAtualizar(){atualizadoEm=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getNome(){return nome;} public void setNome(String nome){this.nome=nome;}
    public String getMarca(){return marca;} public void setMarca(String marca){this.marca=marca;}
    public String getFabricante(){return fabricante;} public void setFabricante(String fabricante){this.fabricante=fabricante;}
    public BigDecimal getQuantidade(){return quantidade;} public void setQuantidade(BigDecimal quantidade){this.quantidade=quantidade;}
    public BigDecimal getPreco(){return preco;} public void setPreco(BigDecimal preco){this.preco=preco;}
    public LocalDateTime getCriadoEm(){return criadoEm;} public void setCriadoEm(LocalDateTime criadoEm){this.criadoEm=criadoEm;}
    public LocalDateTime getAtualizadoEm(){return atualizadoEm;} public void setAtualizadoEm(LocalDateTime atualizadoEm){this.atualizadoEm=atualizadoEm;}
    public Long getLegadoId(){return legadoId;} public void setLegadoId(Long legadoId){this.legadoId=legadoId;}
}
