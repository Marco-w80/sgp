package com.sgp.service;

import com.sgp.model.AlertaResumoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class ProcessoResumoPendenteEmailScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProcessoResumoPendenteEmailScheduler.class);
    private static final ZoneId ZONE_BR = ZoneId.of("America/Sao_Paulo");

    private final ProcessoResumoPendenteEmailService processoResumoPendenteEmailService;

    public ProcessoResumoPendenteEmailScheduler(ProcessoResumoPendenteEmailService processoResumoPendenteEmailService) {
        this.processoResumoPendenteEmailService = processoResumoPendenteEmailService;
    }

    @Scheduled(cron = "0 * * * * *", zone = "${app.alertas.email.resumo.zone:America/Sao_Paulo}")
    public void executarEnvioDiario() {
        try {
            AlertaResumoConfig config = processoResumoPendenteEmailService.obterOuCriarConfiguracao();
            if (!config.isAtivo()) {
                return;
            }

            LocalDateTime agora = LocalDateTime.now(ZONE_BR).withSecond(0).withNano(0);
            LocalTime horarioConfigurado = parseHorario(config.getHorarioEnvio());
            LocalTime horarioAtual = agora.toLocalTime();

            if (!horarioAtual.equals(horarioConfigurado)) {
                return;
            }

            if (jaExecutouHoje(config, agora.toLocalDate())) {
                return;
            }

            int total = processoResumoPendenteEmailService.enviarResumoDiarioPendencias();
            config.setUltimaExecucaoEm(agora);
            processoResumoPendenteEmailService.salvarConfiguracao(config);
            log.info("Rotina de resumo diario concluida. processosNoEmail={}", total);
        } catch (Exception e) {
            log.error("Falha nao tratada na rotina diaria de resumo de processos pendentes.", e);
        }
    }

    private LocalTime parseHorario(String horario) {
        try {
            return LocalTime.parse(horario);
        } catch (Exception e) {
            return LocalTime.of(9, 0);
        }
    }

    private boolean jaExecutouHoje(AlertaResumoConfig config, LocalDate hoje) {
        return config.getUltimaExecucaoEm() != null
                && config.getUltimaExecucaoEm().toLocalDate().isEqual(hoje);
    }
}
