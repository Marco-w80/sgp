package com.sgp.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomErrorController.class);
    private static final ZoneId ZONA_SISTEMA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA_EXIBICAO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z");
    private static final DateTimeFormatter DATA_HORA_CODIGO =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @RequestMapping("/error")
    public String exibirErro(HttpServletRequest request, Model model) {
        int status = resolverStatus(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
        String rota = resolverRota(request);
        Object erroOriginal = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Throwable excecao = erroOriginal instanceof Throwable throwable ? throwable : null;
        ZonedDateTime agora = ZonedDateTime.now(ZONA_SISTEMA);
        String codigoSuporte = gerarCodigoSuporte(agora);
        String titulo = tituloAmigavel(status);
        String causaProvavel = causaProvavel(status, excecao);
        String dataHora = agora.format(DATA_HORA_EXIBICAO);

        model.addAttribute("status", status);
        model.addAttribute("titulo", titulo);
        model.addAttribute("causaProvavel", causaProvavel);
        model.addAttribute("codigoSuporte", codigoSuporte);
        model.addAttribute("dataHora", dataHora);
        model.addAttribute("rota", rota);
        model.addAttribute("detalhesParaSuporte", montarDetalhes(
                codigoSuporte, dataHora, status, rota, causaProvavel));

        if (excecao != null) {
            LOGGER.error("Erro SGP [{}] - status={} rota={}", codigoSuporte, status, rota, excecao);
        } else {
            LOGGER.warn("Erro SGP [{}] - status={} rota={} sem exceção associada",
                    codigoSuporte, status, rota);
        }

        return "error/erro";
    }

    private int resolverStatus(Object valor) {
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        if (valor != null) {
            try {
                return Integer.parseInt(valor.toString());
            } catch (NumberFormatException ignored) {
                // Usa o status genérico abaixo.
            }
        }
        return 500;
    }

    private String resolverRota(HttpServletRequest request) {
        Object rotaOriginal = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        return rotaOriginal != null ? rotaOriginal.toString() : request.getRequestURI();
    }

    private String gerarCodigoSuporte(ZonedDateTime agora) {
        String sufixo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "SGP-" + agora.format(DATA_HORA_CODIGO) + "-" + sufixo;
    }

    private String tituloAmigavel(int status) {
        return switch (status) {
            case 400 -> "Não foi possível processar os dados";
            case 401 -> "Sua sessão precisa ser iniciada novamente";
            case 403 -> "Acesso não permitido";
            case 404 -> "Página não encontrada";
            case 405 -> "Operação não permitida nesta página";
            case 409 -> "Os dados entram em conflito";
            case 503 -> "Serviço temporariamente indisponível";
            default -> "O sistema encontrou um problema";
        };
    }

    private String causaProvavel(int status, Throwable excecao) {
        if (possuiCausa(excecao, DataIntegrityViolationException.class)) {
            return "Os dados informados entram em conflito com um registro existente ou com um vínculo obrigatório.";
        }
        if (possuiCausa(excecao, TypeMismatchException.class)
                || possuiCausa(excecao, BindException.class)) {
            return "Um campo foi enviado com valor vazio ou em um formato diferente do esperado.";
        }

        return switch (status) {
            case 400 -> "Algum dado obrigatório está ausente ou foi informado em um formato inválido.";
            case 401 -> "A sessão expirou ou a autenticação não foi reconhecida.";
            case 403 -> "Seu usuário não possui permissão para executar esta operação.";
            case 404 -> "O endereço acessado não existe ou o registro solicitado não foi encontrado.";
            case 405 -> "A página recebeu um tipo de operação diferente do esperado.";
            case 409 -> "Outro registro já utiliza esses dados ou existe um vínculo que impede a operação.";
            case 503 -> "O sistema ou um serviço necessário está indisponível no momento.";
            default -> "Ocorreu uma falha inesperada durante o processamento da solicitação.";
        };
    }

    private boolean possuiCausa(Throwable excecao, Class<? extends Throwable> tipo) {
        Throwable atual = excecao;
        while (atual != null) {
            if (tipo.isInstance(atual)) {
                return true;
            }
            if (atual.getCause() == atual) {
                break;
            }
            atual = atual.getCause();
        }
        return false;
    }

    private String montarDetalhes(String codigoSuporte, String dataHora, int status,
                                  String rota, String causaProvavel) {
        return "Erro no SGP\n"
                + "Código de suporte: " + codigoSuporte + "\n"
                + "Data e hora: " + dataHora + "\n"
                + "Status HTTP: " + status + "\n"
                + "Página: " + rota + "\n"
                + "Causa provável: " + causaProvavel;
    }
}
