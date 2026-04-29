package com.sgp.service;

import com.sgp.model.AlertaResumoConfig;
import com.sgp.model.Deferimento;
import com.sgp.model.Processo;
import com.sgp.model.StatusProcesso;
import com.sgp.repository.AlertaResumoConfigRepository;
import com.sgp.repository.ProcessoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessoResumoPendenteEmailService {

    private static final Logger log = LoggerFactory.getLogger(ProcessoResumoPendenteEmailService.class);
    private static final ZoneId ZONE_BR = ZoneId.of("America/Sao_Paulo");
    private static final int DIAS_MINIMOS_SEM_ACESSO_PADRAO = 10;
    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter ASSUNTO_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProcessoRepository processoRepository;
    private final AlertaResumoConfigRepository alertaResumoConfigRepository;
    private final AlertaEmailService alertaEmailService;
    private final ProcessoExcelService processoExcelService;
    private final String destinatarios;

    public ProcessoResumoPendenteEmailService(ProcessoRepository processoRepository,
                                              AlertaResumoConfigRepository alertaResumoConfigRepository,
                                              AlertaEmailService alertaEmailService,
                                              ProcessoExcelService processoExcelService,
                                              @Value("${app.alertas.email.resumo.to}") String destinatarios) {
        this.processoRepository = processoRepository;
        this.alertaResumoConfigRepository = alertaResumoConfigRepository;
        this.alertaEmailService = alertaEmailService;
        this.processoExcelService = processoExcelService;
        this.destinatarios = destinatarios;
    }

    @Transactional(readOnly = true)
    public int enviarResumoDiarioPendencias() {
        return enviarResumo(false);
    }

    @Transactional(readOnly = true)
    public int enviarResumoTesteManual() {
        return enviarResumo(true);
    }

    @Transactional(readOnly = true)
    public AlertaResumoConfig obterOuCriarConfiguracao() {
        return alertaResumoConfigRepository.findById(1L).orElseGet(this::salvarPadrao);
    }

    @Transactional
    public AlertaResumoConfig salvarConfiguracao(AlertaResumoConfig config) {
        config.setId(1L);
        if (config.getHorarioEnvio() == null || !config.getHorarioEnvio().matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            config.setHorarioEnvio("09:00");
        }
        return alertaResumoConfigRepository.save(config);
    }

    private int enviarResumo(boolean envioManual) {
        AlertaResumoConfig config = obterOuCriarConfiguracao();
        if (!envioManual && !config.isAtivo()) {
            log.info("Resumo diario de pendencias desativado na configuracao.");
            return 0;
        }

        LocalDate hoje = LocalDate.now(ZONE_BR);
        List<Processo> processos = processoRepository.findByStatusInOrderByIdDesc(
                List.of(StatusProcesso.ABERTO, StatusProcesso.EM_ANDAMENTO));

        int diasMinimosSemAcesso = config.getDiasSemAcesso() != null && config.getDiasSemAcesso() >= 0
                ? config.getDiasSemAcesso()
                : DIAS_MINIMOS_SEM_ACESSO_PADRAO;

        List<ProcessoResumoLinha> linhas = processos.stream()
                .map(p -> montarLinha(p, hoje))
                .filter(l -> l.diasSemAcesso() >= diasMinimosSemAcesso)
                .toList();

        if (!config.isEnviarSemResultados() && linhas.isEmpty()) {
            log.info("Resumo diario nao enviado: nenhuma pendencia encontrada e enviarSemResultados=false.");
            return 0;
        }

        String[] destinos = parseDestinatarios(config.getEmailsDestino());
        if (destinos.length == 0) {
            destinos = parseDestinatarios(destinatarios);
        }
        if (destinos.length == 0) {
            log.warn("Resumo diario nao enviado: nenhum destinatario configurado.");
            return 0;
        }

        String assunto = "SGP - Resumo diario de processos pendentes - " + LocalDateTime.now(ZONE_BR).format(ASSUNTO_FMT);
        String html = montarHtml(hoje, linhas, diasMinimosSemAcesso);
        byte[] excel = processoExcelService.gerarRelatorioCompleto(
                processos.stream()
                        .filter(p -> calcularDiasSemAcesso(p, hoje) >= diasMinimosSemAcesso)
                        .toList(),
                "Resumo diario - Processos pendentes sem acesso"
        );
        String nomeArquivo = "sgp-resumo-pendentes-" + hoje + ".xlsx";

        try {
            alertaEmailService.enviarEmailHtmlComAnexo(destinos, assunto, html, nomeArquivo, excel);
            log.info("Resumo diario de processos pendentes enviado com sucesso. totalProcessos={}, destinos={}",
                    linhas.size(), String.join(",", destinos));
            return linhas.size();
        } catch (Exception e) {
            log.error("Falha ao enviar resumo diario de processos pendentes.", e);
            return 0;
        }
    }

    private ProcessoResumoLinha montarLinha(Processo p, LocalDate hoje) {
        LocalDateTime ultimoAcesso = p.getUltimoAcessoEm();
        long diasSemAcesso = calcularDiasSemAcesso(p, hoje);
        String pendencias = montarPendenciasDocumentais(p);
        String deferimentos = montarDeferimentos(p.getDeferimentos());

        return new ProcessoResumoLinha(
                p.getId(),
                p.getNumeroInterno(),
                vazioComoTraco(p.getNumeroProcesso()),
                p.getPaciente() != null ? vazioComoTraco(p.getPaciente().getNome()) : "-",
                p.getAdvogado() != null ? vazioComoTraco(p.getAdvogado().getNome()) : "-",
                p.getMedico() != null ? vazioComoTraco(p.getMedico().getNome()) : "-",
                p.getDataInicio() != null ? p.getDataInicio().format(DATA_FMT) : "-",
                p.getStatus() != null ? p.getStatus().name() : "-",
                ultimoAcesso != null ? ultimoAcesso.format(DATA_HORA_FMT) : "Sem registro de acesso",
                diasSemAcesso,
                deferimentos,
                pendencias
        );
    }

    private long calcularDiasSemAcesso(Processo processo, LocalDate hoje) {
        LocalDateTime ultimoAcesso = processo.getUltimoAcessoEm();
        LocalDate referencia = ultimoAcesso != null ? ultimoAcesso.toLocalDate() : processo.getDataInicio();
        if (referencia == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(referencia, hoje);
    }

    private String montarPendenciasDocumentais(Processo p) {
        List<String> pendencias = new ArrayList<>();
        if (!p.isCpfAnexado()) pendencias.add("CPF");
        if (!p.isCompResidenciaAnexado()) pendencias.add("Comprovante de Residencia");
        if (!p.isCompRendaAnexado()) pendencias.add("Comprovante de Renda");
        if (!p.isProcuracaoAnexado()) pendencias.add("Procuracao");
        if (!p.isDeclaracaoInsuficienciaAnexado()) pendencias.add("Declaracao de Insuficiencia");
        return pendencias.isEmpty() ? "Sem pendencias documentais" : String.join(", ", pendencias);
    }

    private String montarDeferimentos(List<Deferimento> deferimentos) {
        if (deferimentos == null || deferimentos.isEmpty()) {
            return "-";
        }
        return deferimentos.stream()
                .sorted(Comparator.comparing(Deferimento::getNumeroDeferimento, Comparator.nullsLast(Integer::compareTo)))
                .map(d -> "#" + (d.getNumeroDeferimento() != null ? d.getNumeroDeferimento() : "-") + " - " + (d.getTipo() != null ? d.getTipo().name() : "-"))
                .collect(Collectors.joining(", "));
    }

    private String montarHtml(LocalDate dataGeracao, List<ProcessoResumoLinha> linhas, int diasMinimosSemAcesso) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='margin:0;padding:0;background:#f5f7fb;font-family:Segoe UI,Arial,sans-serif;color:#1f2937;'>");
        sb.append("<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='background:#f5f7fb;padding:22px 0;'>");
        sb.append("<tr><td align='center'>");
        sb.append("<table role='presentation' width='1120' cellspacing='0' cellpadding='0' style='max-width:1120px;width:96%;background:#ffffff;border-radius:14px;overflow:hidden;border:1px solid #e5e7eb;'>");
        sb.append("<tr><td style='background:linear-gradient(135deg,#0b3d91 0%,#166298 100%);padding:18px 24px;color:#fff;'>");
        sb.append("<table role='presentation' width='100%'><tr>");
        sb.append("<td style='vertical-align:middle;'>");
        sb.append("<img src='cid:logoGrupoProd' alt='GrupoProd' style='height:38px;max-width:190px;object-fit:contain;'>");
        sb.append("</td>");
        sb.append("<td align='right' style='font-size:12px;opacity:.95;'>Relatorio automatico SGP</td>");
        sb.append("</tr></table>");
        sb.append("</td></tr>");
        sb.append("<tr><td style='padding:18px 24px;'>");
        sb.append("<h2 style='margin:0 0 8px;font-size:21px;color:#0f172a;'>Resumo diario de processos pendentes</h2>");
        sb.append("<p style='margin:0;color:#334155;font-size:13px;'>Data de geracao: <strong>").append(dataGeracao.format(DATA_FMT)).append("</strong></p>");
        sb.append("<p style='margin:6px 0 0;color:#334155;font-size:13px;'>Filtro: <strong>").append(diasMinimosSemAcesso).append(" dias ou mais sem acesso</strong></p>");
        sb.append("</td></tr>");

        sb.append("<tr><td style='padding:0 24px 14px;'>");
        sb.append("<table role='presentation' width='100%'><tr>");
        sb.append("<td style='background:#eef6ff;border:1px solid #dbeafe;border-radius:10px;padding:12px 14px;'>");
        sb.append("<div style='font-size:12px;color:#1e3a8a;margin-bottom:3px;'>Processos no resumo</div>");
        sb.append("<div style='font-size:24px;font-weight:700;color:#0b3d91;'>").append(linhas.size()).append("</div>");
        sb.append("</td>");
        sb.append("<td style='padding-left:10px;'>");
        sb.append("<a href='#' style='display:inline-block;background:#16a34a;color:#fff;text-decoration:none;padding:10px 14px;border-radius:8px;font-size:13px;font-weight:600;'>Baixar Excel (anexo no e-mail)</a>");
        sb.append("</td>");
        sb.append("</tr></table>");
        sb.append("</td></tr>");

        if (linhas.isEmpty()) {
            sb.append("<tr><td style='padding:0 24px 24px;'>");
            sb.append("<div style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;padding:18px;color:#334155;font-size:14px;'>");
            sb.append("Nenhum processo com ").append(diasMinimosSemAcesso).append(" dias ou mais sem acesso foi encontrado hoje.");
            sb.append("</div></td></tr>");
            sb.append("</table></td></tr></table></body></html>");
            return sb.toString();
        }

        sb.append("<tr><td style='padding:0 24px 24px;'>");
        sb.append("<div style='overflow-x:auto;border:1px solid #e5e7eb;border-radius:10px;'>");
        sb.append("<table cellspacing='0' cellpadding='0' style='border-collapse:separate;border-spacing:0;width:100%;font-size:12px;'>");
        sb.append("<thead><tr style='background:#0b3d91;color:#fff;'>");
        sb.append("<th>ID</th><th>Interno</th><th>Processo</th><th>Paciente</th><th>Advogado</th><th>Medico</th>");
        sb.append("<th>Inicio</th><th>Status</th><th>Ultimo acesso</th><th>Dias sem acesso</th><th>Deferimentos</th><th>Pendencias documentais</th>");
        sb.append("</tr></thead><tbody>");

        int idx = 0;
        for (ProcessoResumoLinha l : linhas) {
            String bg = (idx++ % 2 == 0) ? "#ffffff" : "#f8fafc";
            sb.append("<tr style='background:").append(bg).append(";'>");
            sb.append(td(String.valueOf(l.id())));
            sb.append(td(l.interno()));
            sb.append(td(l.processo()));
            sb.append(td(l.paciente()));
            sb.append(td(l.advogado()));
            sb.append(td(l.medico()));
            sb.append(td(l.inicio()));
            sb.append(td(l.status()));
            sb.append(td(l.ultimoAcesso()));
            sb.append(td(String.valueOf(l.diasSemAcesso())));
            sb.append(td(l.deferimentos()));
            sb.append(td(l.pendenciasDocumentais()));
            sb.append("</tr>");
        }

        sb.append("</tbody></table></div>");
        sb.append("<p style='font-size:11px;color:#64748b;margin:10px 0 0;'>O arquivo Excel completo deste resumo segue anexado neste e-mail.</p>");
        sb.append("</td></tr>");
        sb.append("</table></td></tr></table></body></html>");
        return sb.toString();
    }

    private String td(String value) {
        return "<td style='padding:9px 8px;border-top:1px solid #e5e7eb;vertical-align:top;line-height:1.35;'>" + escapeHtml(vazioComoTraco(value)) + "</td>";
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String vazioComoTraco(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }

    private String[] parseDestinatarios(String valor) {
        if (valor == null || valor.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    private AlertaResumoConfig salvarPadrao() {
        AlertaResumoConfig config = new AlertaResumoConfig();
        config.setId(1L);
        config.setEmailsDestino(destinatarios != null ? destinatarios : "");
        config.setDiasSemAcesso(DIAS_MINIMOS_SEM_ACESSO_PADRAO);
        config.setAtivo(true);
        config.setEnviarSemResultados(true);
        config.setHorarioEnvio("09:00");
        return alertaResumoConfigRepository.save(config);
    }

    private record ProcessoResumoLinha(Long id,
                                       String interno,
                                       String processo,
                                       String paciente,
                                       String advogado,
                                       String medico,
                                       String inicio,
                                       String status,
                                       String ultimoAcesso,
                                       long diasSemAcesso,
                                       String deferimentos,
                                       String pendenciasDocumentais) {
    }
}
