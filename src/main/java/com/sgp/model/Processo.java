package com.sgp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "processos")
public class Processo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_interno", nullable = false, unique = true)
    private String numeroInterno;

    @Column(name = "numero_processo", nullable = false)
    private String numeroProcesso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "advogado_id")
    private Advogado advogado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcesso status;

    // **Somente** um-para-muitos para a tabela de junção com dados extras
    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcessoProduto> itens = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "local_id")
    private Local local;

    @Column(columnDefinition = "TEXT")
    private String obs;

    public Processo() {}

    public Processo(String numeroInterno,
                    String numeroProcesso,
                    Paciente paciente,
                    Advogado advogado,
                    Medico medico,
                    Local local,
                    LocalDate dataInicio,
                    StatusProcesso status) {
        this.numeroInterno  = numeroInterno;
        this.numeroProcesso = numeroProcesso;
        this.paciente       = paciente;
        this.advogado       = advogado;
        this.medico         = medico;
        this.local          = local;
        this.dataInicio     = dataInicio;
        this.status         = status;
    }

    // método ajustado para receber quantidade também
    public void addItem(Produto produto, LocalDate dataEnvio, Integer quantidade) {
        ProcessoProduto pp = new ProcessoProduto(this, produto, dataEnvio, quantidade);
        itens.add(pp);
    }

    public void clearItems() {
        itens.clear();
    }

    // ----- getters & setters -----

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

    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }

    public List<ProcessoProduto> getItens() { return itens; }
    public void setItens(List<ProcessoProduto> itens) { this.itens = itens; }
}
