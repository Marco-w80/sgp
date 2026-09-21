package com.sgp.controller.licitacao;

import com.sgp.service.licitacao.FornecedorConsultaService;
import com.sgp.service.licitacao.FornecedorConsultaService.ConsultaCnpjException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/licitacoes/fornecedores")
public class FornecedorConsultaController {
    private final FornecedorConsultaService consulta;

    public FornecedorConsultaController(FornecedorConsultaService consulta) { this.consulta = consulta; }

    @PostMapping("/consultar-cnpj")
    public FornecedorConsultaService.DadosFornecedorCnpj consultar(@RequestBody(required=false) ConsultaRequest request) {
        return consulta.consultar(request == null ? null : request.cnpj());
    }

    @ExceptionHandler({ConsultaCnpjException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String,String>> erro(RuntimeException e) {
        int status = e instanceof ConsultaCnpjException c ? c.getStatus().value() : 400;
        return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
    }

    public record ConsultaRequest(String cnpj) {}
}
