package com.sgp.dto;

import java.util.List;

import com.sgp.model.Processo;

public class ProcessoPendenteDTO {
    private Processo processo;
    private List<String> documentosFaltando;
    private long diasAguardando;

    public ProcessoPendenteDTO(Processo processo, List<String> documentosFaltando, long diasAguardando) {
        this.processo = processo;
        this.documentosFaltando = documentosFaltando;
        this.diasAguardando = diasAguardando;
    }

    // Getters
    public Processo getProcesso() { return processo; }
    public List<String> getDocumentosFaltando() { return documentosFaltando; }
    public long getDiasAguardando() { return diasAguardando; }
}
