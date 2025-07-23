package com.sgp.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sgp.dto.ProcessoPendenteDTO;
import com.sgp.model.Processo;
import com.sgp.model.StatusProcesso;
import com.sgp.repository.ProcessoRepository;

@Service
public class ProcessoService {

    @Autowired
    private ProcessoRepository processoRepository;

public List<ProcessoPendenteDTO> listarProcessosComDocumentacaoPendenteComMinDias(int diasMinimos) {
    LocalDate hoje = LocalDate.now();

    return processoRepository.findAll().stream()
        .filter(this::temPendenciaDocumento) // método auxiliar abaixo
        .map(p -> {
            long dias = ChronoUnit.DAYS.between(p.getDataInicio(), hoje);
            return new AbstractMap.SimpleEntry<>(p, dias);
        })
        .filter(e -> e.getValue() >= diasMinimos) // AQUI é o filtro correto
        .map(e -> new ProcessoPendenteDTO(
                e.getKey(),
                documentosFaltando(e.getKey()),
                e.getValue()
        ))
        .toList();
}

private boolean temPendenciaDocumento(Processo p) {
    return !p.isCpfAnexado()
        || !p.isCompResidenciaAnexado()
        || !p.isCompRendaAnexado()
        || !p.isProcuracaoAnexado()
        || !p.isDeclaracaoInsuficienciaAnexado();
}

private List<String> documentosFaltando(Processo p) {
    List<String> docs = new ArrayList<>();
    if (!p.isCpfAnexado()) docs.add("CPF");
    if (!p.isCompResidenciaAnexado()) docs.add("Comprovante de Residência");
    if (!p.isCompRendaAnexado()) docs.add("Comprovante de Renda");
    if (!p.isProcuracaoAnexado()) docs.add("Procuração");
    if (!p.isDeclaracaoInsuficienciaAnexado()) docs.add("Declaração de Insuficiência");
    return docs;
}

public Map<StatusProcesso, Long> contarPorStatus() {
        Map<StatusProcesso, Long> mapa = new EnumMap<>(StatusProcesso.class);
        for (StatusProcesso st : StatusProcesso.values()) {
            mapa.put(st, processoRepository.countByStatus(st));
        }
        return mapa;
    }

    
}
