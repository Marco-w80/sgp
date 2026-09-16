package com.sgp.controller.licitacao;

import com.sgp.service.licitacao.PncpService;
import com.sgp.service.licitacao.PncpService.PncpConsultaException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/licitacoes/pncp")
public class PncpController {
    private final PncpService pncp;

    public PncpController(PncpService pncp) {
        this.pncp = pncp;
    }

    @PostMapping("/consultar")
    public PncpService.ConsultaPncpResponse consultar(@RequestBody(required = false) ConsultaRequest request) {
        return pncp.buscarContratacaoPncp(request == null ? null : request.link());
    }

    @PostMapping("/consultar/dados")
    public PncpService.ConsultaDadosPncpResponse consultarDados(
            @RequestBody(required = false) ConsultaRequest request) {
        return pncp.buscarDadosContratacaoPncp(request == null ? null : request.link());
    }

    @PostMapping("/consultar/itens")
    public ConsultaItensResponse consultarItens(@RequestBody(required = false) ConsultaRequest request) {
        return new ConsultaItensResponse(
                pncp.buscarItensContratacaoPncp(request == null ? null : request.link()));
    }

    @ExceptionHandler(PncpConsultaException.class)
    public ResponseEntity<Map<String, String>> erroPncp(PncpConsultaException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Informe um link válido do PNCP."));
    }

    public record ConsultaRequest(String link) {}

    public record ConsultaItensResponse(java.util.List<PncpService.ItemPncp> itens) {}
}
