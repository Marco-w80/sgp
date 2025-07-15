package com.sgp.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sgp.dto.ProcessoDetalhadoDto;
import com.sgp.model.Processo;
import com.sgp.model.ProcessoLog;
import com.sgp.repository.ProcessoRepository;
import com.sgp.repository.ProcessoLogRepository;

@RestController
@RequestMapping("/api/processos") // <-- define o prefixo comum
public class ProcessoApiController {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private ProcessoLogRepository processoLogRepository;

    @GetMapping("/verifica-numero/{numero}")
    public Map<String, Boolean> verificaNumeroInterno(@PathVariable String numero) {
        boolean existe = processoRepository.existsByNumeroInterno(numero);
        return Map.of("existe", existe);
    }

    @GetMapping("/{id}/detalhes")
    public ProcessoDetalhadoDto getDetalhesProcesso(@PathVariable Long id) {
        Processo proc = processoRepository.findById(id).orElseThrow();
        List<ProcessoLog> logs = processoLogRepository.findByProcessoId(id); // preferível aqui
        return new ProcessoDetalhadoDto(proc, logs);
    }
}
