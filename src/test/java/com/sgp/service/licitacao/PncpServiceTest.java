package com.sgp.service.licitacao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PncpServiceTest {
    private static final String LINK = "https://pncp.gov.br/app/editais/46374500000194/2026/7477";
    private static final String API_BUSCA = "https://pncp.gov.br/api/search/";
    private static final String API_ATUAL = "https://pncp.gov.br/api/consulta/v1/orgaos/";
    private static final String API_ITENS = "https://pncp.gov.br/api/pncp/v1/orgaos/";

    private HttpClient http;
    private PncpService service;

    @BeforeEach
    void preparar() {
        http = mock(HttpClient.class);
        service = new PncpService(new ObjectMapper(), http, API_ATUAL, API_ITENS, Duration.ofSeconds(12));
    }

    @Test
    void consultaEndpointControladoEMapeiaCampos() throws Exception {
        when(http.send(any(HttpRequest.class), anyStringBodyHandler())).thenAnswer(invocacao -> {
            HttpRequest request = invocacao.getArgument(0);
            return request.uri().getPath().endsWith("/itens")
                    ? resposta(200, respostaItensValida())
                    : resposta(200, respostaValida());
        });

        var resultado = service.buscarContratacaoPncp(LINK);

        assertThat(resultado.dados().orgao()).isEqualTo("SECRETARIA DE ESTADO DA SAUDE");
        assertThat(resultado.dados().numeroEdital()).isEqualTo("30");
        assertThat(resultado.dados().uasg()).isEqualTo("090110");
        assertThat(resultado.dados().modalidade()).isEqualTo("Pregão - Eletrônico");
        assertThat(resultado.dados().objeto()).isEqualTo("Aquisição de medicamentos");
        assertThat(resultado.dados().dataDisputa()).isEqualTo("2026-09-24T09:00");
        assertThat(resultado.dados().valorEstimadoTotal()).isEqualByComparingTo("0.00");
        assertThat(resultado.dados().linkEdital()).isEqualTo(LINK);
        assertThat(resultado.pncp().cnpj()).isEqualTo("46374500000194");
        assertThat(resultado.pncp().ano()).isEqualTo(2026);
        assertThat(resultado.pncp().sequencial()).isEqualTo(7477);
        assertThat(resultado.pncp().numeroControlePNCP()).isEqualTo("46374500000194-1-007477/2026");
        assertThat(resultado.itens()).hasSize(2);
        assertThat(resultado.itens().get(0).numeroItem()).isEqualTo(1);
        assertThat(resultado.itens().get(0).descricao()).isEqualTo("Dexclorfeniramina Maleato 2 mg");
        assertThat(resultado.itens().get(0).quantidade()).isEqualByComparingTo("9000.0000");
        assertThat(resultado.itens().get(0).unidade()).isEqualTo("Comprimido");
        assertThat(resultado.itens().get(0).valorReferencia()).isEqualByComparingTo("0.25");
    }

    @Test
    void aceitaBarraFinalQueryEHashEPreservaLinkNormalizado() throws Exception {
        String link = LINK + "/?origem=sgp#detalhes";
        when(http.send(any(HttpRequest.class), anyStringBodyHandler())).thenAnswer(invocacao -> {
            HttpRequest request = invocacao.getArgument(0);
            return request.uri().getPath().endsWith("/itens")
                    ? resposta(200, respostaItensValida())
                    : resposta(200, respostaValida());
        });

        var resultado = service.buscarContratacaoPncp(link);

        assertThat(resultado.dados().linkEdital()).isEqualTo(link);
    }

    @Test
    void rejeitaFormatoInvalidoSemConsultarRede() throws Exception {
        assertThatThrownBy(() -> service.buscarContratacaoPncp("https://pncp.gov.br/app/editais/teste"))
                .isInstanceOf(PncpService.PncpConsultaException.class)
                .hasMessage("Informe um link válido do PNCP.");
        verify(http, never()).send(any(HttpRequest.class), anyStringBodyHandler());
    }

    @Test
    void rejeitaSiteExternoSemConsultarRede() throws Exception {
        assertThatThrownBy(() -> service.buscarContratacaoPncp("https://google.com/teste"))
                .isInstanceOf(PncpService.PncpConsultaException.class)
                .hasMessage("Informe um link válido do PNCP.");
        verify(http, never()).send(any(HttpRequest.class), anyStringBodyHandler());
    }

    @Test
    void informaQuandoContratacaoNaoExiste() throws Exception {
        HttpResponse<String> resposta = resposta(404, "{}");
        when(http.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(resposta);

        assertThatThrownBy(() -> service.buscarContratacaoPncp(LINK))
                .isInstanceOfSatisfying(PncpService.PncpConsultaException.class, erro -> {
                    assertThat(erro.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(erro).hasMessage("Não foi possível localizar essa contratação no PNCP.");
                });
    }

    @Test
    void trataTimeoutComoIndisponibilidade() throws Exception {
        when(http.send(any(HttpRequest.class), anyStringBodyHandler()))
                .thenThrow(new HttpTimeoutException("timeout"));

        assertThatThrownBy(() -> service.buscarContratacaoPncp(LINK))
                .isInstanceOfSatisfying(PncpService.PncpConsultaException.class, erro -> {
                    assertThat(erro.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(erro).hasMessageContaining("Você ainda pode preencher a licitação manualmente");
                });
    }

    @Test
    void usaSomenteEndpointOficialAtualFixo() throws Exception {
        when(http.send(any(HttpRequest.class), anyStringBodyHandler())).thenAnswer(invocacao -> {
            HttpRequest request = invocacao.getArgument(0);
            return request.uri().getPath().endsWith("/itens")
                    ? resposta(200, respostaItensValida())
                    : resposta(200, respostaValida());
        });

        service.buscarContratacaoPncp(LINK);

        var captor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(http, org.mockito.Mockito.times(2)).send(captor.capture(), anyStringBodyHandler());
        assertThat(captor.getAllValues()).extracting(request -> request.uri().toString()).containsExactly(
                "https://pncp.gov.br/api/consulta/v1/orgaos/46374500000194/compras/2026/7477",
                "https://pncp.gov.br/api/pncp/v1/orgaos/46374500000194/compras/2026/7477/itens?pagina=1&tamanhoPagina=50");
    }

    @Test
    void usaIndiceOficialDoPortalSemEsperarApiDetalhada() throws Exception {
        service = new PncpService(new ObjectMapper(), http, API_BUSCA, API_ATUAL,
                API_ITENS, Duration.ofSeconds(25));
        when(http.send(any(HttpRequest.class), anyStringBodyHandler())).thenAnswer(invocacao -> {
            HttpRequest request = invocacao.getArgument(0);
            if (request.uri().toString().startsWith(API_BUSCA)) {
                return resposta(200, respostaBuscaValida());
            }
            if (request.uri().getPath().endsWith("/itens")) {
                return resposta(200, respostaItensValida());
            }
            throw new AssertionError("A API detalhada lenta não deveria ser chamada");
        });

        var resultado = service.buscarContratacaoPncp(LINK);
        var resultadoEmCache = service.buscarContratacaoPncp(LINK);

        assertThat(resultado.dados().orgao()).isEqualTo("SECRETARIA DE ESTADO DA SAUDE");
        assertThat(resultado.dados().numeroEdital()).isEqualTo("30");
        assertThat(resultado.dados().uasg()).isEqualTo("090110");
        assertThat(resultado.dados().modalidade()).isEqualTo("Pregão - Eletrônico");
        assertThat(resultado.dados().objeto()).isEqualTo("Aquisição de medicamentos (DEXCLORFENIRAMINA e outros)");
        assertThat(resultado.dados().dataDisputa()).isEqualTo("2026-09-24T09:00");
        assertThat(resultado.pncp().numeroControlePNCP()).isEqualTo("46374500000194-1-007477/2026");
        assertThat(resultadoEmCache).isEqualTo(resultado);

        var captor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(http, org.mockito.Mockito.times(2)).send(captor.capture(), anyStringBodyHandler());
        assertThat(captor.getAllValues()).extracting(request -> request.uri().toString())
                .first().asString().startsWith(API_BUSCA + "?q=46374500000194-1-007477%2F2026");
        assertThat(captor.getAllValues()).extracting(request -> request.uri().toString())
                .noneMatch(uri -> uri.startsWith(API_ATUAL));
    }

    @Test
    void repeteConsultaDoIndiceAposTimeoutTemporario() throws Exception {
        service = new PncpService(new ObjectMapper(), http, API_BUSCA, API_ATUAL,
                API_ITENS, Duration.ofSeconds(25));
        AtomicInteger chamada = new AtomicInteger();
        when(http.send(any(HttpRequest.class), anyStringBodyHandler())).thenAnswer(invocacao -> switch (chamada.getAndIncrement()) {
            case 0 -> throw new HttpTimeoutException("timeout temporário");
            case 1 -> resposta(200, respostaBuscaValida());
            default -> resposta(200, respostaItensValida());
        });

        var resultado = service.buscarContratacaoPncp(LINK);

        assertThat(resultado.dados().numeroEdital()).isEqualTo("30");
        var captor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(http, org.mockito.Mockito.times(3)).send(captor.capture(), anyStringBodyHandler());
        assertThat(captor.getAllValues()).extracting(request -> request.uri().toString()).containsExactly(
                "https://pncp.gov.br/api/search/?q=46374500000194-1-007477%2F2026&tipos_documento=edital&pagina=1&tam_pagina=10",
                "https://pncp.gov.br/api/search/?q=46374500000194-1-007477%2F2026&tipos_documento=edital&pagina=1&tam_pagina=10",
                "https://pncp.gov.br/api/pncp/v1/orgaos/46374500000194/compras/2026/7477/itens?pagina=1&tamanhoPagina=50");
    }

    @Test
    void rejeitaJsonInesperado() throws Exception {
        HttpResponse<String> resposta = resposta(200, "não é json");
        when(http.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(resposta);

        assertThatThrownBy(() -> service.buscarContratacaoPncp(LINK))
                .isInstanceOfSatisfying(PncpService.PncpConsultaException.class,
                        erro -> assertThat(erro.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void carregaTodasAsPaginasDeUmEditalComMaisDeCemItens() throws Exception {
        when(http.send(any(HttpRequest.class), anyStringBodyHandler())).thenAnswer(invocacao -> {
            HttpRequest request = invocacao.getArgument(0);
            String query = request.uri().getRawQuery();
            if (query.contains("pagina=1&")) return resposta(200, respostaItens(1, 50));
            if (query.contains("pagina=2&")) return resposta(200, respostaItens(51, 100));
            if (query.contains("pagina=3&")) return resposta(200, respostaItens(101, 114));
            return resposta(500, "{}");
        });

        var itens = service.buscarItensContratacaoPncp(LINK);

        assertThat(itens).hasSize(114);
        assertThat(itens.get(113).numeroItem()).isEqualTo(114);
        var captor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(http, org.mockito.Mockito.times(3)).send(captor.capture(), anyStringBodyHandler());
        assertThat(captor.getAllValues()).extracting(request -> request.uri().toString()).containsExactly(
                "https://pncp.gov.br/api/pncp/v1/orgaos/46374500000194/compras/2026/7477/itens?pagina=1&tamanhoPagina=50",
                "https://pncp.gov.br/api/pncp/v1/orgaos/46374500000194/compras/2026/7477/itens?pagina=2&tamanhoPagina=50",
                "https://pncp.gov.br/api/pncp/v1/orgaos/46374500000194/compras/2026/7477/itens?pagina=3&tamanhoPagina=50");
    }

    @Test
    void naoAceitaImportacaoParcialSePaginaPosteriorFalhar() throws Exception {
        when(http.send(any(HttpRequest.class), anyStringBodyHandler())).thenAnswer(invocacao -> {
            HttpRequest request = invocacao.getArgument(0);
            return request.uri().getRawQuery().contains("pagina=1&")
                    ? resposta(200, respostaItens(1, 50)) : resposta(503, "{}");
        });

        assertThatThrownBy(() -> service.buscarItensContratacaoPncp(LINK))
                .isInstanceOfSatisfying(PncpService.PncpConsultaException.class, erro -> {
                    assertThat(erro.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(erro).hasMessageContaining("página 2");
                });
    }

    @Test
    void validaEConverteItensRecebidosPeloFormulario() {
        var linhas = service.lerItensImportados("""
                [{"numeroItem":1,"descricao":"Medicamento","quantidade":10.5,
                  "unidade":"Unidade com nome maior do que trinta caracteres","valorReferencia":2.75}]
                """);

        assertThat(linhas).hasSize(1);
        assertThat(linhas.get(0).numero()).isEqualTo(1);
        assertThat(linhas.get(0).quantidade()).isEqualByComparingTo("10.5");
        assertThat(linhas.get(0).unidade()).hasSize(30);
        assertThat(linhas.get(0).valor()).isEqualByComparingTo("2.75");
    }

    @Test
    void rejeitaNumerosDeItemDuplicadosNoFormulario() {
        assertThatThrownBy(() -> service.lerItensImportados("""
                [{"numeroItem":1,"descricao":"A","quantidade":1,"unidade":"UN"},
                 {"numeroItem":1,"descricao":"B","quantidade":1,"unidade":"UN"}]
                """))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("itens recebidos do PNCP são inválidos");
    }

    @SuppressWarnings("unchecked")
    private HttpResponse.BodyHandler<String> anyStringBodyHandler() {
        return any(HttpResponse.BodyHandler.class);
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> resposta(int status, String corpo) {
        HttpResponse<String> resposta = mock(HttpResponse.class);
        when(resposta.statusCode()).thenReturn(status);
        when(resposta.body()).thenReturn(corpo);
        return resposta;
    }

    private String respostaValida() {
        return """
                {
                  "orgaoEntidade": {"razaoSocial": "SECRETARIA DE ESTADO DA SAUDE"},
                  "numeroCompra": "30",
                  "unidadeOrgao": {"codigoUnidade": "090110"},
                  "modalidadeNome": "Pregão - Eletrônico",
                  "objetoCompra": "Aquisição de medicamentos",
                  "dataEncerramentoProposta": "2026-09-24T09:00:00",
                  "valorTotalEstimado": 0.00,
                  "numeroControlePNCP": "46374500000194-1-007477/2026"
                }
                """;
    }

    private String respostaItensValida() {
        return """
                [
                  {"numeroItem":1,"descricao":"Dexclorfeniramina Maleato 2 mg","quantidade":9000.0000,"unidadeMedida":"Comprimido","valorUnitarioEstimado":0.25},
                  {"numeroItem":2,"descricao":"Dipirona 500 mg","quantidade":5000,"unidadeMedida":"Comprimido","valorUnitarioEstimado":0.18}
                ]
                """;
    }

    private String respostaBuscaValida() {
        return """
                {
                  "items": [{
                    "title": "Edital nº 30/2026",
                    "description": "Aquisição de medicamentos (DEXCLORFENIRAMINA e outros)",
                    "item_url": "/compras/46374500000194/2026/7477",
                    "document_type": "edital",
                    "numero": null,
                    "ano": "2026",
                    "numero_sequencial": "7477",
                    "numero_controle_pncp": "46374500000194-1-007477/2026",
                    "orgao_cnpj": "46374500000194",
                    "orgao_nome": "SECRETARIA DE ESTADO DA SAUDE",
                    "unidade_codigo": "090110",
                    "modalidade_licitacao_nome": "Pregão - Eletrônico",
                    "data_fim_vigencia": "2026-09-24T09:00",
                    "valor_global": null
                  }],
                  "total": 1
                }
                """;
    }

    private String respostaItens(int quantidade) {
        return respostaItens(1, quantidade);
    }

    private String respostaItens(int inicio, int fim) {
        return IntStream.rangeClosed(inicio, fim)
                .mapToObj(numero -> "{\"numeroItem\":" + numero
                        + ",\"descricao\":\"Item " + numero
                        + "\",\"quantidade\":1,\"unidadeMedida\":\"UN\",\"valorUnitarioEstimado\":1}")
                .collect(Collectors.joining(",", "[", "]"));
    }
}
