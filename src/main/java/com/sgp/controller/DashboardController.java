package com.sgp.controller;

import com.sgp.dto.DocumentComplianceDto;
import com.sgp.dto.MonthCountDto;
import com.sgp.dto.StatusCountDto;
import com.sgp.model.Processo;
import com.sgp.repository.ProcessoRepository;
import com.sgp.repository.PacienteRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DashboardController {

    private final ProcessoRepository processoRepo;
    private final PacienteRepository pacienteRepo;

    public DashboardController(ProcessoRepository processoRepo,
                               PacienteRepository pacienteRepo) {
        this.processoRepo = processoRepo;
        this.pacienteRepo  = pacienteRepo;
    }

    @GetMapping("/api/dashboard/processos-por-status")
    public List<StatusCountDto> getProcessosPorStatus() {
        return processoRepo.countByStatus();
    }

    @GetMapping("/api/dashboard/compliance-documentos")
    public List<DocumentComplianceDto> getComplianceDocumentos() {
        List<Processo> todos = (List<Processo>) processoRepo.findAll();
        int total = todos.size();
        if (total == 0) {
            return new ArrayList<>();
        }

        long cpfCount          = todos.stream().filter(Processo::isCpfAnexado).count();
        long resCount          = todos.stream().filter(Processo::isCompResidenciaAnexado).count();
        long rendaCount        = todos.stream().filter(Processo::isCompRendaAnexado).count();
        long procCount         = todos.stream().filter(Processo::isProcuracaoAnexado).count();
        long insufCount        = todos.stream().filter(Processo::isDeclaracaoInsuficienciaAnexado).count();

        List<DocumentComplianceDto> lista = new ArrayList<>();
        lista.add(new DocumentComplianceDto("CPF",               cpfCount   * 100.0 / total));
        lista.add(new DocumentComplianceDto("Residência",        resCount   * 100.0 / total));
        lista.add(new DocumentComplianceDto("Renda",             rendaCount * 100.0 / total));
        lista.add(new DocumentComplianceDto("Procuração",        procCount  * 100.0 / total));
        lista.add(new DocumentComplianceDto("Declaração Insuf.", insufCount * 100.0 / total));

        return lista;
    }

    @GetMapping("/api/dashboard/processos-por-mes")
    public List<MonthCountDto> getProcessosPorMes() {
        return processoRepo.countByMonth();
    }

    @GetMapping("/api/dashboard/total-pacientes")
    public long getTotalPacientes() {
        return pacienteRepo.count();
    }
}
