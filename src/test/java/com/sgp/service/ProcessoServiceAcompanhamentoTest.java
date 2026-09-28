package com.sgp.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.sgp.model.Doenca;
import com.sgp.model.Paciente;
import com.sgp.model.Processo;
import com.sgp.model.Sexo;
import com.sgp.model.StatusProcesso;
import com.sgp.repository.DoencaRepository;
import com.sgp.repository.PacienteRepository;
import com.sgp.repository.ProcessoRepository;

@SpringBootTest
@Transactional
class ProcessoServiceAcompanhamentoTest {

    @Autowired
    private ProcessoService processoService;

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private DoencaRepository doencaRepository;

    @Test
    void listaSomenteProcessosSemVisitaHaMaisDiasQueOFiltroEExcluiObitos() {
        Paciente paciente = pacienteRepository.save(new Paciente(
                "Paciente acompanhamento",
                Sexo.OUTRO,
                LocalDate.of(1990, 1, 1),
                "12345678901",
                "TESTE-ACOMPANHAMENTO"));

        Doenca doenca = new Doenca();
        doenca.setNome("Doença acompanhamento");
        doenca = doencaRepository.save(doenca);

        Processo antigoSemAcesso = salvarProcesso(
                "ACOMP-ANTIGO-SEM-ACESSO", paciente, doenca,
                LocalDate.now().minusDays(10), StatusProcesso.ABERTO, null);
        Processo recenteSemAcesso = salvarProcesso(
                "ACOMP-RECENTE-SEM-ACESSO", paciente, doenca,
                LocalDate.now().minusDays(2), StatusProcesso.ABERTO, null);
        Processo antigoComVisitaRecente = salvarProcesso(
                "ACOMP-VISITA-RECENTE", paciente, doenca,
                LocalDate.now().minusDays(30), StatusProcesso.EM_ANDAMENTO,
                LocalDateTime.now().minusDays(2));
        Processo antigoComVisitaAntiga = salvarProcesso(
                "ACOMP-VISITA-ANTIGA", paciente, doenca,
                LocalDate.now().minusDays(30), StatusProcesso.EM_ANDAMENTO,
                LocalDateTime.now().minusDays(10));
        Processo obitoSemAcesso = salvarProcesso(
                "ACOMP-OBITO", paciente, doenca,
                LocalDate.now().minusDays(30), StatusProcesso.OBITO, null);
        Processo obitoLegadoSemAcesso = salvarProcesso(
                "ACOMP-OBITO-LEGADO", paciente, doenca,
                LocalDate.now().minusDays(30), StatusProcesso.CONCLUIDO, null);

        List<Long> idsEncontrados = processoService.listarProcessosSemVisita(7).stream()
                .map(Processo::getId)
                .toList();

        assertThat(idsEncontrados)
                .contains(antigoSemAcesso.getId(), antigoComVisitaAntiga.getId())
                .doesNotContain(
                        recenteSemAcesso.getId(),
                        antigoComVisitaRecente.getId(),
                        obitoSemAcesso.getId(),
                        obitoLegadoSemAcesso.getId());
    }

    private Processo salvarProcesso(String numeroInterno,
                                     Paciente paciente,
                                     Doenca doenca,
                                     LocalDate dataInicio,
                                     StatusProcesso status,
                                     LocalDateTime ultimoAcessoEm) {
        Processo processo = new Processo(numeroInterno, paciente, dataInicio, status);
        processo.setDoenca(doenca);
        processo.setUltimoAcessoEm(ultimoAcessoEm);
        return processoRepository.save(processo);
    }
}
