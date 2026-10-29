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
    void listaSomenteProcessosAtivosSemVisitaHaMaisDiasQueOFiltro() {
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
        Processo suspensoSemAcesso = salvarProcesso(
                "ACOMP-SUSPENSO", paciente, doenca,
                LocalDate.now().minusDays(30), StatusProcesso.SUSPENSO, null);

        List<Long> idsEncontrados = processoService.listarProcessosSemVisita(7).stream()
                .map(Processo::getId)
                .toList();

        assertThat(idsEncontrados)
                .contains(antigoSemAcesso.getId(), antigoComVisitaAntiga.getId())
                .doesNotContain(
                        recenteSemAcesso.getId(),
                        antigoComVisitaRecente.getId(),
                        obitoSemAcesso.getId(),
                        obitoLegadoSemAcesso.getId(),
                        suspensoSemAcesso.getId());
    }

    @Test
    void separaProcessosAtivosSuspensosEObitosPelosStatus() {
        Paciente paciente = pacienteRepository.save(new Paciente(
                "Paciente listagens por status",
                Sexo.OUTRO,
                LocalDate.of(1985, 5, 10),
                "10987654321",
                "TESTE-LISTAGENS-STATUS"));

        Doenca doenca = new Doenca();
        doenca.setNome("Doença para listagens");
        doenca = doencaRepository.save(doenca);

        Processo aberto = salvarProcesso("LISTA-ABERTO", paciente, doenca,
                LocalDate.now(), StatusProcesso.ABERTO, null);
        Processo andamento = salvarProcesso("LISTA-ANDAMENTO", paciente, doenca,
                LocalDate.now(), StatusProcesso.EM_ANDAMENTO, null);
        Processo suspenso = salvarProcesso("LISTA-SUSPENSO", paciente, doenca,
                LocalDate.now(), StatusProcesso.SUSPENSO, null);
        Processo obito = salvarProcesso("LISTA-OBITO", paciente, doenca,
                LocalDate.now(), StatusProcesso.OBITO, null);
        Processo obitoLegado = salvarProcesso("LISTA-CONCLUIDO", paciente, doenca,
                LocalDate.now(), StatusProcesso.CONCLUIDO, null);

        List<Long> ativos = ids(processoService.listarProcessosComFiltroAcompanhamentoPorStatus(
                null, null, List.of(StatusProcesso.ABERTO, StatusProcesso.EM_ANDAMENTO)));
        List<Long> suspensos = ids(processoService.listarProcessosComFiltroAcompanhamentoPorStatus(
                null, null, List.of(StatusProcesso.SUSPENSO)));
        List<Long> obitos = ids(processoService.listarProcessosComFiltroAcompanhamentoPorStatus(
                null, null, List.of(StatusProcesso.OBITO, StatusProcesso.CONCLUIDO)));

        assertThat(ativos).containsExactlyInAnyOrder(aberto.getId(), andamento.getId());
        assertThat(suspensos).containsExactly(suspenso.getId());
        assertThat(obitos).containsExactlyInAnyOrder(obito.getId(), obitoLegado.getId());
    }

    private List<Long> ids(List<Processo> processos) {
        return processos.stream().map(Processo::getId).toList();
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
