package com.sgp.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sgp.repository.ProcessoRepository;

public class ProcessoApiController {

    @Autowired
    private ProcessoRepository processoRepository;

@GetMapping("/api/processos/verifica-numero/{numero}")
@ResponseBody
public Map<String, Boolean> verificaNumeroInterno(@PathVariable String numero) {
    boolean existe = processoRepository.existsByNumeroInterno(numero);
    return Map.of("existe", existe);
}

    
}
