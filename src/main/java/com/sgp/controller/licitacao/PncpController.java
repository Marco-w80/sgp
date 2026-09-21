package com.sgp.controller.licitacao;

import com.sgp.service.licitacao.PncpService;
import com.sgp.service.licitacao.PncpService.PncpConsultaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<PncpService.ItemPncp> todos = pncp.buscarItensContratacaoPncp(request == null ? null : request.link());
        return new ConsultaItensResponse(
                selecionarItens(todos, request == null ? null : request.numerosItens()), todos.size());
    }

    private List<PncpService.ItemPncp> selecionarItens(List<PncpService.ItemPncp> itens, String texto) {
        if (texto == null || texto.isBlank()) return itens;
        String numeros = texto.trim();
        if (numeros.length() > 2000 || !numeros.matches("\\d+(?:[\\s,;]+\\d+)*")) {
            throw new PncpConsultaException(HttpStatus.BAD_REQUEST,
                    "Informe apenas números de itens separados por espaço, vírgula ou ponto e vírgula.");
        }
        Set<Integer> solicitados = new LinkedHashSet<>();
        try {
            for (String numero : numeros.split("[\\s,;]+")) {
                int valor = Integer.parseInt(numero);
                if (valor < 1) throw new NumberFormatException();
                solicitados.add(valor);
            }
        } catch (NumberFormatException e) {
            throw new PncpConsultaException(HttpStatus.BAD_REQUEST,
                    "Informe números de itens válidos e maiores que zero.");
        }
        Set<Integer> encontrados = itens.stream().map(PncpService.ItemPncp::numeroItem).collect(Collectors.toSet());
        List<Integer> ausentes = solicitados.stream().filter(numero -> !encontrados.contains(numero)).toList();
        if (!ausentes.isEmpty()) {
            throw new PncpConsultaException(HttpStatus.BAD_REQUEST,
                    "Itens não encontrados no PNCP: " + ausentes.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ".");
        }
        return itens.stream().filter(item -> solicitados.contains(item.numeroItem())).toList();
    }

    @ExceptionHandler(PncpConsultaException.class)
    public ResponseEntity<Map<String, String>> erroPncp(PncpConsultaException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Informe um link válido do PNCP."));
    }

    public record ConsultaRequest(String link, String numerosItens) {}

    public record ConsultaItensResponse(List<PncpService.ItemPncp> itens, int totalItens) {}
}
