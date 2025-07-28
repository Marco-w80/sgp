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

    // obrigatório: número interno
    @Column(name = "numero_interno", nullable = false, unique = true)
    private String numeroInterno;

    // opcional: número do processo
    @Column(name = "numero_processo", nullable = true)
    private String numeroProcesso;

    // obrigatório: paciente
    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    // opcional: advogado
    @ManyToOne(optional = true)
    @JoinColumn(name = "advogado_id", nullable = true)
    private Advogado advogado;

    // opcional: médico
    @ManyToOne(optional = true)
    @JoinColumn(name = "medico_id", nullable = true)
    private Medico medico;

    // opcional: hospital
    @ManyToOne(optional = true)
    @JoinColumn(name = "hospital_id", nullable = true)
    private Hospital hospital;

    // obrigatório: doença
    @ManyToOne(optional = false)
    @JoinColumn(name = "doenca_id", nullable = false)
    private Doenca doenca;

    // opcional: local
    @ManyToOne(optional = true)
    @JoinColumn(name = "local_id", nullable = true)
    private Local local;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "cpf_anexado", nullable = false)
    private boolean cpfAnexado = false;

    @Column(name = "comp_residencia_anexado", nullable = false)
    private boolean compResidenciaAnexado = false;

    @Column(name = "comp_renda_anexado", nullable = false)
    private boolean compRendaAnexado = false;

    @Column(name = "procuracao_anexado", nullable = false)
    private boolean procuracaoAnexado = false;

    @Column(name = "declaracao_insuficiencia_anexado", nullable = false)
    private boolean declaracaoInsuficienciaAnexado = false;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProcessoLog> logs = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcesso status;

    // opcional: tipo de hospital
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TipoHospital tipoHospital;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcessoProduto> itens = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String obs;

    public Processo() {}

    // construtor apenas com campos obrigatórios
    public Processo(String numeroInterno,
                    Paciente paciente,
                    LocalDate dataInicio,
                    StatusProcesso status) {
        this.numeroInterno = numeroInterno;
        this.paciente      = paciente;
        this.dataInicio    = dataInicio;
        this.status        = status;
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
    public void setId(Long id) { this.id = id; }

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

    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }

    public Doenca getDoenca() { return doenca; }
    public void setDoenca(Doenca doenca) { this.doenca = doenca; }

    public Local getLocal() { return local; }
    public void setLocal(Local local) { this.local = local; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public StatusProcesso getStatus() { return status; }
    public void setStatus(StatusProcesso status) { this.status = status; }

    public TipoHospital getTipoHospital() { return tipoHospital; }
    public void setTipoHospital(TipoHospital tipoHospital) { this.tipoHospital = tipoHospital; }

    public List<ProcessoLog> getLogs() { return logs; }
    public void setLogs(List<ProcessoLog> logs) { this.logs = logs; }

    public List<ProcessoProduto> getItens() { return itens; }
    public void setItens(List<ProcessoProduto> itens) { this.itens = itens; }

    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }

    public boolean isCpfAnexado() {
        return cpfAnexado;
    }

    public void setCpfAnexado(boolean cpfAnexado) {
        this.cpfAnexado = cpfAnexado;
    }

    public boolean isCompResidenciaAnexado() {
        return compResidenciaAnexado;
    }

    public void setCompResidenciaAnexado(boolean compResidenciaAnexado) {
        this.compResidenciaAnexado = compResidenciaAnexado;
    }

    public boolean isCompRendaAnexado() {
        return compRendaAnexado;
    }

    public void setCompRendaAnexado(boolean compRendaAnexado) {
        this.compRendaAnexado = compRendaAnexado;
    }

    public boolean isProcuracaoAnexado() {
        return procuracaoAnexado;
    }

    public void setProcuracaoAnexado(boolean procuracaoAnexado) {
        this.procuracaoAnexado = procuracaoAnexado;
    }

    public boolean isDeclaracaoInsuficienciaAnexado() {
        return declaracaoInsuficienciaAnexado;
    }

    public void setDeclaracaoInsuficienciaAnexado(boolean declaracaoInsuficienciaAnexado) {
        this.declaracaoInsuficienciaAnexado = declaracaoInsuficienciaAnexado;
    }
}
