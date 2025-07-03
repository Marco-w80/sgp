package com.sgp.api;

import org.springframework.web.bind.annotation.*;

import com.sgp.dto.DoencaDTO;
import com.sgp.model.Doenca;
import com.sgp.repository.DoencaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/api/doencas")
public class DoencaApiController {

    @Autowired
    private DoencaRepository doencaRepository;

    @GetMapping("/por-grupo/{grupoId}")
    public List<DoencaDTO> buscarPorGrupo(@PathVariable Long grupoId) {
        List<Doenca> doencas = doencaRepository.findByGrupoId(grupoId);
        return doencas.stream()
                .map(d -> new DoencaDTO(d.getId(), d.getNome()))
                .collect(Collectors.toList());
    }
}
