package com.sgp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "processos")
public class Processo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número interno sequencial do processo */
    @Column(name = "numero_interno", nullable = false, unique = true)
    private String numeroInterno;

    /** Número de processo externo (formato 0802234-22.2024.8.19.0083) */
    @Column(name = "numero_processo", nullable = false)
    private String numeroProcesso;

    /** Paciente principal (Pessoa do tipo Paciente) */
    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    /** Advogado responsável */
    @ManyToOne(optional = false)
    @JoinColumn(name = "advogado_id")
    private Advogado advogado;

    /** Médico responsável */
    @ManyToOne(optional = false)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    /** Data de início do processo */
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    /** Status atual do processo */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcesso status;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcessoProduto> itens = new ArrayList<>();

    public void addItem(Produto produto, LocalDate dataEnvio) {
        ProcessoProduto pp = new ProcessoProduto(this, produto, dataEnvio);
        itens.add(pp);
    }
    public void clearItems() {
        itens.clear();
    }

    /** Local onde o processo tramita */
    @ManyToOne(optional = false)
    @JoinColumn(name = "local_id")
    private Local local;

    /** Produtos associados ao processo */
    @ManyToMany
    @JoinTable(
        name = "processo_produtos",
        joinColumns = @JoinColumn(name = "processo_id"),
        inverseJoinColumns = @JoinColumn(name = "produto_id")
    )
    private Set<Produto> produtos = new HashSet<>();

    /** Observações adicionais */
    @Column(columnDefinition = "TEXT")
    private String obs;

    // Construtor padrão
    public Processo() {}

    // Construtor com todos os campos obrigatórios
    public Processo(String numeroInterno, String numeroProcesso, Paciente paciente,
                    Advogado advogado, Medico medico, Local local,
                    LocalDate dataInicio, StatusProcesso status) {
        this.numeroInterno = numeroInterno;
        this.numeroProcesso = numeroProcesso;
        this.paciente = paciente;
        this.advogado = advogado;
        this.medico = medico;
        this.local = local;
        this.dataInicio = dataInicio;
        this.status = status;
    }

    // Getters e setters
    public Long getId() { return id; }
    public String getNumeroInterno() { return numeroInterno; }
    public void setNumeroInterno(String numeroInterno) { this.numeroInterno = numeroInterno; }
    public String getNumeroProcesso() { return numeroProcesso; }
    public void setNumeroProcesso(String numeroProcesso) { this.numeroProcesso = numeroProcesso; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Advogado getAdvogado() { return advogado; }
    public void setAdvogado(Advogado advogado) { this.advogado = advogado; }
    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public StatusProcesso getStatus() { return status; }
    public void setStatus(StatusProcesso status) { this.status = status; }
    public Local getLocal() { return local; }
    public void setLocal(Local local) { this.local = local; }
    public Set<Produto> getProdutos() { return produtos; }
    public void setProdutos(Set<Produto> produtos) { this.produtos = produtos; }
    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }

    public List<ProcessoProduto> getItens() { return itens; }
    public void setItens(List<ProcessoProduto> itens) { this.itens = itens; }
}
