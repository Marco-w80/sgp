package com.sgp.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
}
