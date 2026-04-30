package com.sgp.dto;

import com.sgp.model.Processo;

import java.util.List;

public class ProcessoPendenciaListagemDTO {
    private final Processo processo;
    private final String tipoPendencia;
    private final List<String> documentosFaltantes;

    public ProcessoPendenciaListagemDTO(Processo processo, String tipoPendencia, List<String> documentosFaltantes) {
        this.processo = processo;
        this.tipoPendencia = tipoPendencia;
        this.documentosFaltantes = documentosFaltantes;
    }

    public Processo getProcesso() {
        return processo;
    }

    public String getTipoPendencia() {
        return tipoPendencia;
    }

    public List<String> getDocumentosFaltantes() {
        return documentosFaltantes;
    }
}
