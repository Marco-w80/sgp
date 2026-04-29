package com.sgp.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class AlertaEmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public AlertaEmailService(JavaMailSender mailSender,
            @Value("${app.alertas.email.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void enviarEmailTeste(String destino) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(destino);
        message.setSubject("SGP - Teste de alerta por e-mail");
        message.setText("Teste de alerta do SGP enviado em: " + LocalDateTime.now());
        mailSender.send(message);
    }

    public void enviarEmailHtml(String[] destinos, String assunto, String html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom(from);
        helper.setTo(destinos);
        helper.setSubject(assunto);
        helper.setText(html, true);
        mailSender.send(message);
    }

    public void enviarEmailHtmlComAnexo(String[] destinos,
                                        String assunto,
                                        String html,
                                        String nomeArquivoExcel,
                                        byte[] excelBytes) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(destinos);
        helper.setSubject(assunto);
        helper.setText(html, true);

        ClassPathResource logo = new ClassPathResource("static/img/logo-branca-complet.png");
        if (logo.exists()) {
            helper.addInline("logoGrupoProd", logo);
        }

        if (excelBytes != null && excelBytes.length > 0) {
            helper.addAttachment(
                    nomeArquivoExcel,
                    new ByteArrayResource(excelBytes),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
        }

        mailSender.send(message);
    }
}
