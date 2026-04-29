package com.sgp.controller;

import com.sgp.service.AlertaEmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/intranet/alertas")
public class AlertaTesteController {

    private final AlertaEmailService alertaEmailService;
    private final String destinatarioTeste;

    public AlertaTesteController(AlertaEmailService alertaEmailService,
            @Value("${app.alertas.email.teste.to}") String destinatarioTeste) {
        this.alertaEmailService = alertaEmailService;
        this.destinatarioTeste = destinatarioTeste;
    }

    @GetMapping("/teste-email")
    public ResponseEntity<String> dispararTesteEmail() {
        try {
            alertaEmailService.enviarEmailTeste(destinatarioTeste);
            return ResponseEntity.ok("E-mail de teste enviado para: " + destinatarioTeste);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Falha ao enviar e-mail de teste: " + montarDetalhesErro(e));
        }
    }

    private String montarDetalhesErro(Throwable throwable) {
        StringBuilder detalhes = new StringBuilder();
        Throwable atual = throwable;
        int nivel = 0;

        while (atual != null && nivel < 5) {
            if (nivel > 0) {
                detalhes.append(" | causa ").append(nivel).append(": ");
            }
            detalhes.append(atual.getClass().getSimpleName())
                    .append(" - ")
                    .append(atual.getMessage());

            if (atual instanceof MessagingException me && me.getNextException() != null) {
                detalhes.append(" | smtp: ").append(me.getNextException().getMessage());
            }

            atual = atual.getCause();
            nivel++;
        }

        return detalhes.toString();
    }
}
