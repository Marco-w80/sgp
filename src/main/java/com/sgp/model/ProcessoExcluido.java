package com.sgp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade que representa um "arquivo morto" ou histórico de um processo que foi excluído do sistema.
 * Guarda uma "fotografia" dos dados principais do processo no momento da exclusão para fins de auditoria e consulta.
 */
@Entity
@Table(name = "processos_excluidos")
public class ProcessoExcluido {

    @Id
    private Long id; // ID original do processo, sem auto-geração.

    // --- CÓPIA DOS DADOS DIRETOS ---
    private String numeroInterno;
    private String numeroProcesso;
    private LocalDate dataInicio;
    
    @Enumerated(EnumType.STRING)
    private StatusProcesso status;
    
    @Enumerated(EnumType.STRING)
    private TipoHospital tipoHospital;
    
    @Column(columnDefinition = "TEXT")
    private String obs;

    // --- CÓPIA DOS DOCUMENTOS ANEXADOS ---
    private boolean cpfAnexado;
    private boolean compResidenciaAnexado;
    private boolean compRendaAnexado;
    private boolean procuracaoAnexado;
    private boolean declaracaoInsuficienciaAnexado;

    // --- DADOS "ACHATADOS" DOS RELACIONAMENTOS ---
    private String pacienteNome;
    private String pacienteCpf;
    private String advogadoNome;
    private String advogadoOab;
    private String medicoNome;
    private String medicoCrm;
    private String hospitalNome;
    private String doencaNome;
    private String localDescricao;

    // --- CAMPOS DE AUDITORIA ---
    private LocalDateTime dataExclusao;
    private String usuarioExclusao;

    /**
     * Construtor padrão exigido pelo JPA.
     */
    public ProcessoExcluido() {
    }

    // --- GETTERS E SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroInterno() {
        return numeroInterno;
    }

    public void setNumeroInterno(String numeroInterno) {
        this.numeroInterno = numeroInterno;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public StatusProcesso getStatus() {
        return status;
    }

    public void setStatus(StatusProcesso status) {
        this.status = status;
    }

    public TipoHospital getTipoHospital() {
        return tipoHospital;
    }

    public void setTipoHospital(TipoHospital tipoHospital) {
        this.tipoHospital = tipoHospital;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

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

    public String getPacienteNome() {
        return pacienteNome;
    }

    public void setPacienteNome(String pacienteNome) {
        this.pacienteNome = pacienteNome;
    }

    public String getPacienteCpf() {
        return pacienteCpf;
    }

    public void setPacienteCpf(String pacienteCpf) {
        this.pacienteCpf = pacienteCpf;
    }

    public String getAdvogadoNome() {
        return advogadoNome;
    }

    public void setAdvogadoNome(String advogadoNome) {
        this.advogadoNome = advogadoNome;
    }

    public String getAdvogadoOab() {
        return advogadoOab;
    }

    public void setAdvogadoOab(String advogadoOab) {
        this.advogadoOab = advogadoOab;
    }

    public String getMedicoNome() {
        return medicoNome;
    }

    public void setMedicoNome(String medicoNome) {
        this.medicoNome = medicoNome;
    }

    public String getMedicoCrm() {
        return medicoCrm;
    }

    public void setMedicoCrm(String medicoCrm) {
        this.medicoCrm = medicoCrm;
    }

    public String getHospitalNome() {
        return hospitalNome;
    }

    public void setHospitalNome(String hospitalNome) {
        this.hospitalNome = hospitalNome;
    }

    public String getDoencaNome() {
        return doencaNome;
    }

    public void setDoencaNome(String doencaNome) {
        this.doencaNome = doencaNome;
    }

    public String getLocalDescricao() {
        return localDescricao;
    }

    public void setLocalDescricao(String localDescricao) {
        this.localDescricao = localDescricao;
    }

    public LocalDateTime getDataExclusao() {
        return dataExclusao;
    }

    public void setDataExclusao(LocalDateTime dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

    public String getUsuarioExclusao() {
        return usuarioExclusao;
    }

    public void setUsuarioExclusao(String usuarioExclusao) {
        this.usuarioExclusao = usuarioExclusao;
    }
}