package com.sgp.service.licitacao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PncpService {
    private static final String API_BUSCA = "https://pncp.gov.br/api/search/";
    private static final String API_ATUAL = "https://pncp.gov.br/api/consulta/v1/orgaos/";
    private static final String API_ITENS = "https://pncp.gov.br/api/pncp/v1/orgaos/";
    private static final Duration TIMEOUT = Duration.ofSeconds(40);
    private static final Duration TIMEOUT_BUSCA = Duration.ofSeconds(6);
    private static final Duration TEMPO_CACHE = Duration.ofMinutes(5);
    private static final int TENTATIVAS_BUSCA = 2;
    private static final int TAMANHO_PAGINA_ITENS = 50;
    private static final int LIMITE_ITENS = 5_000;
    private static final int LIMITE_JSON_ITENS = 5_000_000;
    private static final Pattern CAMINHO_EDITAL = Pattern.compile("^/app/editais/(\\d{14})/(\\d{4})/(\\d+)/?$");
    private static final Pattern NUMERO_NO_TITULO = Pattern.compile("(?iu)\\bn[º°o.]?\\s*([^/]+?)(?:/\\d{4})?\\s*$");
    private static final DateTimeFormatter DATA_FORMULARIO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");

    private static final Logger LOG = LoggerFactory.getLogger(PncpService.class);

    private final ObjectMapper json;
    private final HttpClient http;
    private final String apiBusca;
    private final String apiAtual;
    private final String apiItens;
    private final Duration timeout;
    private final ConcurrentMap<String, CacheEntry<ConsultaDadosPncpResponse>> cacheDados = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheEntry<List<ItemPncp>>> cacheItens = new ConcurrentHashMap<>();

    @Autowired
    public PncpService(ObjectMapper json) {
        this(json,
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .version(HttpClient.Version.HTTP_1_1)
                        .build(),
                API_BUSCA,
                API_ATUAL,
                API_ITENS,
                TIMEOUT);
    }

    PncpService(ObjectMapper json, HttpClient http, String apiAtual, String apiItens, Duration timeout) {
        this(json, http, null, apiAtual, apiItens, timeout);
    }

    PncpService(ObjectMapper json, HttpClient http, String apiBusca, String apiAtual,
                String apiItens, Duration timeout) {
        this.json = json;
        this.http = http;
        this.apiBusca = apiBusca;
        this.apiAtual = apiAtual;
        this.apiItens = apiItens;
        this.timeout = timeout;
    }

    public ConsultaPncpResponse buscarContratacaoPncp(String link) {
        ConsultaDadosPncpResponse contratacao = buscarDadosContratacaoPncp(link);
        List<ItemPncp> itens = buscarItensContratacaoPncp(link);
        return new ConsultaPncpResponse(contratacao.dados(), contratacao.pncp(), itens);
    }

    public ConsultaDadosPncpResponse buscarDadosContratacaoPncp(String link) {
        ReferenciaPncp referencia = interpretarLink(link);
        String chave = chave(referencia);
        ConsultaDadosPncpResponse dadosEmCache = obterCache(cacheDados, chave);
        if (dadosEmCache != null) {
            return dadosEmCache;
        }
        Optional<ConsultaDadosPncpResponse> dadosDoIndice = buscarDadosNoIndice(referencia);
        if (dadosDoIndice.isPresent()) {
            return guardarCache(cacheDados, chave, dadosDoIndice.get());
        }

        HttpResponse<String> resposta = consultar(apiAtual, referencia, "", "dos dados gerais");

        if (resposta.statusCode() == HttpStatus.NOT_FOUND.value()) {
            throw new PncpConsultaException(HttpStatus.NOT_FOUND,
                    "Não foi possível localizar essa contratação no PNCP.");
        }
        if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
            LOG.warn("PNCP respondeu HTTP {} para a contratação {}/{}/{}",
                    resposta.statusCode(), referencia.cnpj(), referencia.ano(), referencia.sequencial());
            throw indisponivel("Não foi possível consultar os dados gerais no PNCP.");
        }

        DadosEMetadados dados = mapearDados(resposta.body(), referencia);
        return guardarCache(cacheDados, chave, new ConsultaDadosPncpResponse(dados.dados(), dados.pncp()));
    }

    public List<ItemPncp> buscarItensContratacaoPncp(String link) {
        ReferenciaPncp referencia = interpretarLink(link);
        String chave = chave(referencia);
        List<ItemPncp> itensEmCache = obterCache(cacheItens, chave);
        if (itensEmCache != null) {
            return itensEmCache;
        }
        HttpResponse<String> respostaItens = consultar(apiItens, referencia,
                "/itens?pagina=1&tamanhoPagina=" + TAMANHO_PAGINA_ITENS, "dos itens");
        if (respostaItens.statusCode() == HttpStatus.NOT_FOUND.value()) {
            return guardarCache(cacheItens, chave, List.of());
        }
        if (respostaItens.statusCode() < 200 || respostaItens.statusCode() >= 300) {
            LOG.warn("PNCP respondeu HTTP {} ao consultar itens de {}/{}/{}",
                    respostaItens.statusCode(), referencia.cnpj(), referencia.ano(), referencia.sequencial());
            throw indisponivel("Não foi possível consultar os itens no PNCP.");
        }
        List<ItemPncp> primeiraPagina = mapearItens(respostaItens.body());
        if (primeiraPagina.size() < TAMANHO_PAGINA_ITENS) {
            return guardarCache(cacheItens, chave, primeiraPagina);
        }

        int quantidadeTotal = buscarQuantidadeItens(referencia);
        if (quantidadeTotal <= primeiraPagina.size()) {
            return guardarCache(cacheItens, chave, primeiraPagina);
        }

        HttpResponse<String> respostaCompleta = consultar(apiItens, referencia, "/itens", "de todos os itens");
        if (respostaCompleta.statusCode() < 200 || respostaCompleta.statusCode() >= 300) {
            throw indisponivel("Não foi possível consultar todos os itens no PNCP.");
        }
        List<ItemPncp> todosOsItens = mapearItens(respostaCompleta.body());
        if (todosOsItens.size() != quantidadeTotal) {
            throw respostaInesperada();
        }
        return guardarCache(cacheItens, chave, todosOsItens);
    }

    private int buscarQuantidadeItens(ReferenciaPncp referencia) {
        HttpResponse<String> resposta = consultar(apiItens, referencia, "/itens/quantidade", "da quantidade de itens");
        if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
            throw indisponivel("Não foi possível confirmar a quantidade de itens no PNCP.");
        }
        try {
            JsonNode valor = json.readTree(resposta.body());
            int quantidade = valor == null ? -1 : valor.asInt(-1);
            if (quantidade < 0 || quantidade > LIMITE_ITENS) {
                throw respostaInesperada();
            }
            return quantidade;
        } catch (JsonProcessingException e) {
            throw respostaInesperada();
        }
    }

    private String chave(ReferenciaPncp referencia) {
        return referencia.cnpj() + "/" + referencia.ano() + "/" + referencia.sequencial();
    }

    private <T> T obterCache(ConcurrentMap<String, CacheEntry<T>> cache, String chave) {
        CacheEntry<T> entrada = cache.get(chave);
        if (entrada == null) {
            return null;
        }
        if (entrada.expiraEm().isBefore(Instant.now())) {
            cache.remove(chave, entrada);
            return null;
        }
        return entrada.valor();
    }

    private <T> T guardarCache(ConcurrentMap<String, CacheEntry<T>> cache, String chave, T valor) {
        cache.put(chave, new CacheEntry<>(valor, Instant.now().plus(TEMPO_CACHE)));
        return valor;
    }

    private Optional<ConsultaDadosPncpResponse> buscarDadosNoIndice(ReferenciaPncp referencia) {
        if (apiBusca == null || apiBusca.isBlank()) {
            return Optional.empty();
        }

        String numeroControle = referencia.cnpj() + "-1-"
                + String.format(Locale.ROOT, "%06d", referencia.sequencial()) + "/" + referencia.ano();
        URI destino = URI.create(apiBusca + "?q="
                + URLEncoder.encode(numeroControle, StandardCharsets.UTF_8)
                + "&tipos_documento=edital&pagina=1&tam_pagina=10");
        Duration timeoutBusca = timeout.compareTo(TIMEOUT_BUSCA) < 0 ? timeout : TIMEOUT_BUSCA;

        for (int tentativa = 1; tentativa <= TENTATIVAS_BUSCA; tentativa++) {
            try {
                HttpResponse<String> resposta = consultar(destino, "do índice de dados gerais", timeoutBusca);
                boolean temporario = resposta.statusCode() == HttpStatus.TOO_MANY_REQUESTS.value()
                        || resposta.statusCode() >= 500;
                if (resposta.statusCode() >= 200 && resposta.statusCode() < 300) {
                    Optional<ConsultaDadosPncpResponse> dados = mapearDadosBusca(resposta.body(), referencia);
                    if (dados.isPresent()) {
                        return dados;
                    }
                }
                if (!temporario) break;
            } catch (PncpConsultaException e) {
                LOG.warn("Tentativa {} no índice do PNCP falhou para {}/{}/{}: {}", tentativa,
                        referencia.cnpj(), referencia.ano(), referencia.sequencial(), e.getMessage());
            }
        }
        LOG.info("Índice do PNCP não retornou a contratação exata {}/{}/{}; usando API detalhada",
                referencia.cnpj(), referencia.ano(), referencia.sequencial());
        return Optional.empty();
    }

    private ReferenciaPncp interpretarLink(String link) {
        if (link == null || link.isBlank()) {
            throw linkInvalido();
        }

        try {
            URI uri = new URI(link.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || !"pncp.gov.br".equalsIgnoreCase(uri.getHost())
                    || uri.getUserInfo() != null
                    || uri.getPort() != -1) {
                throw linkInvalido();
            }

            Matcher caminho = CAMINHO_EDITAL.matcher(uri.getPath());
            if (!caminho.matches()) {
                throw linkInvalido();
            }

            int ano = Integer.parseInt(caminho.group(2));
            long sequencial = Long.parseLong(caminho.group(3));
            String linkNormalizado = new URI("https", null, "pncp.gov.br", -1,
                    uri.getPath(), uri.getQuery(), uri.getFragment()).toASCIIString();
            return new ReferenciaPncp(caminho.group(1), ano, sequencial, linkNormalizado);
        } catch (URISyntaxException | NumberFormatException e) {
            throw linkInvalido();
        }
    }

    private HttpResponse<String> consultar(String base, ReferenciaPncp referencia, String sufixo, String etapa) {
        URI destino = URI.create(base + referencia.cnpj() + "/compras/"
                + referencia.ano() + "/" + referencia.sequencial() + sufixo);
        return consultar(destino, etapa, timeout);
    }

    private HttpResponse<String> consultar(URI destino, String etapa, Duration limite) {
        HttpRequest requisicao = HttpRequest.newBuilder(destino)
                .timeout(limite)
                .header("Accept", "application/json")
                .header("User-Agent", "SGP/1.0")
                .GET()
                .build();
        try {
            return http.send(requisicao, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpTimeoutException e) {
            LOG.warn("Timeout ao consultar o PNCP em {}", destino);
            throw indisponivel("O PNCP não respondeu à consulta " + etapa + " dentro do prazo.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Consulta ao PNCP interrompida em {}", destino);
            throw indisponivel("A consulta " + etapa + " ao PNCP foi interrompida.");
        } catch (IOException e) {
            LOG.warn("Falha de comunicação com o PNCP em {}: {}", destino, e.toString());
            throw indisponivel("Houve uma falha de comunicação ao consultar " + etapa + " no PNCP.");
        }
    }

    private Optional<ConsultaDadosPncpResponse> mapearDadosBusca(String corpo, ReferenciaPncp referencia) {
        final JsonNode raiz;
        try {
            raiz = json.readTree(corpo);
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
        if (raiz == null || !raiz.isObject() || !raiz.path("items").isArray()) {
            return Optional.empty();
        }

        String caminhoEsperado = "/compras/" + referencia.cnpj() + "/"
                + referencia.ano() + "/" + referencia.sequencial();
        JsonNode itemExato = null;
        for (JsonNode item : raiz.path("items")) {
            if (caminhoEsperado.equals(item.path("item_url").asText())
                    && referencia.cnpj().equals(item.path("orgao_cnpj").asText())
                    && Integer.toString(referencia.ano()).equals(item.path("ano").asText())
                    && Long.toString(referencia.sequencial()).equals(item.path("numero_sequencial").asText())) {
                itemExato = item;
                break;
            }
        }
        if (itemExato == null) {
            return Optional.empty();
        }

        String orgao = texto(itemExato.path("orgao_nome"));
        String numeroEdital = numeroCompraDoIndice(itemExato);
        String objeto = texto(itemExato.path("description"));
        String dataDisputa = dataFormulario(texto(itemExato.path("data_fim_vigencia")));
        if (orgao == null || numeroEdital == null || objeto == null || dataDisputa == null) {
            return Optional.empty();
        }

        DadosPncp dados = new DadosPncp(
                orgao,
                numeroEdital,
                texto(itemExato.path("unidade_codigo")),
                texto(itemExato.path("modalidade_licitacao_nome")),
                objeto,
                dataDisputa,
                decimal(itemExato.path("valor_global")),
                referencia.linkNormalizado());
        MetadadosPncp pncp = new MetadadosPncp(
                referencia.cnpj(),
                referencia.ano(),
                referencia.sequencial(),
                texto(itemExato.path("numero_controle_pncp")));
        return Optional.of(new ConsultaDadosPncpResponse(dados, pncp));
    }

    private String numeroCompraDoIndice(JsonNode item) {
        String numero = texto(item.path("numero"));
        if (numero != null) {
            return numero;
        }
        String titulo = texto(item.path("title"));
        if (titulo == null) {
            return null;
        }
        Matcher matcher = NUMERO_NO_TITULO.matcher(titulo);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private DadosEMetadados mapearDados(String corpo, ReferenciaPncp referencia) {
        final JsonNode raiz;
        try {
            raiz = json.readTree(corpo);
        } catch (JsonProcessingException e) {
            throw respostaInesperada();
        }
        if (raiz == null || !raiz.isObject()) {
            throw respostaInesperada();
        }

        DadosPncp dados = new DadosPncp(
                texto(raiz.path("orgaoEntidade").path("razaoSocial")),
                texto(raiz.path("numeroCompra")),
                texto(raiz.path("unidadeOrgao").path("codigoUnidade")),
                texto(raiz.path("modalidadeNome")),
                texto(raiz.path("objetoCompra")),
                dataFormulario(texto(raiz.path("dataEncerramentoProposta"))),
                decimal(raiz.path("valorTotalEstimado")),
                referencia.linkNormalizado());
        MetadadosPncp pncp = new MetadadosPncp(
                referencia.cnpj(),
                referencia.ano(),
                referencia.sequencial(),
                texto(raiz.path("numeroControlePNCP")));
        return new DadosEMetadados(dados, pncp);
    }

    private List<ItemPncp> mapearItens(String corpo) {
        final JsonNode raiz;
        try {
            raiz = json.readTree(corpo);
        } catch (JsonProcessingException e) {
            throw respostaInesperada();
        }
        if (raiz == null || !raiz.isArray() || raiz.size() > LIMITE_ITENS) {
            throw respostaInesperada();
        }

        List<ItemPncp> itens = new ArrayList<>(raiz.size());
        Set<Integer> numeros = new HashSet<>();
        for (JsonNode no : raiz) {
            Integer numero = inteiro(no.path("numeroItem"));
            String descricao = texto(no.path("descricao"));
            BigDecimal quantidade = decimal(no.path("quantidade"));
            String unidade = normalizarUnidade(texto(no.path("unidadeMedida")));
            BigDecimal valor = decimal(no.path("valorUnitarioEstimado"));
            if (numero == null || numero <= 0 || descricao == null || quantidade == null
                    || quantidade.signum() <= 0 || !numeros.add(numero)) {
                throw respostaInesperada();
            }
            itens.add(new ItemPncp(numero, descricao, quantidade, unidade, valor));
        }
        return List.copyOf(itens);
    }

    public List<ImportacaoTabularService.LinhaImportada> lerItensImportados(String itensJson) {
        if (itensJson == null || itensJson.isBlank()) {
            return List.of();
        }
        if (itensJson.length() > LIMITE_JSON_ITENS) {
            throw itensInvalidos();
        }

        final JsonNode raiz;
        try {
            raiz = json.readTree(itensJson);
        } catch (JsonProcessingException e) {
            throw itensInvalidos();
        }
        if (raiz == null || !raiz.isArray() || raiz.size() > LIMITE_ITENS) {
            throw itensInvalidos();
        }

        List<ImportacaoTabularService.LinhaImportada> linhas = new ArrayList<>(raiz.size());
        Set<Integer> numeros = new HashSet<>();
        for (JsonNode no : raiz) {
            Integer numero = inteiro(no.path("numeroItem"));
            String descricao = textoImportado(no.path("descricao"));
            BigDecimal quantidade = decimalImportado(no.path("quantidade"));
            String unidade = normalizarUnidade(textoImportado(no.path("unidade")));
            BigDecimal valor = decimalImportado(no.path("valorReferencia"));
            if (numero == null || numero <= 0 || descricao == null || quantidade == null
                    || quantidade.signum() <= 0 || (valor != null && valor.signum() < 0)
                    || !numeros.add(numero)) {
                throw itensInvalidos();
            }
            linhas.add(new ImportacaoTabularService.LinhaImportada(
                    numero, descricao, quantidade, unidade, valor, false));
        }
        return List.copyOf(linhas);
    }

    private String textoImportado(JsonNode no) {
        if (no == null || no.isMissingNode() || no.isNull()) {
            return null;
        }
        if (!no.isValueNode()) {
            throw itensInvalidos();
        }
        String valor = no.asText().trim();
        return valor.isBlank() ? null : valor;
    }

    private BigDecimal decimalImportado(JsonNode no) {
        if (no == null || no.isMissingNode() || no.isNull()) {
            return null;
        }
        if (!no.isValueNode()) {
            throw itensInvalidos();
        }
        try {
            return no.isNumber() ? no.decimalValue() : new BigDecimal(no.asText());
        } catch (NumberFormatException e) {
            throw itensInvalidos();
        }
    }

    private Integer inteiro(JsonNode no) {
        if (no == null || no.isMissingNode() || no.isNull()) {
            return null;
        }
        try {
            return no.isIntegralNumber() ? no.intValue() : new BigDecimal(no.asText()).intValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            return null;
        }
    }

    private String normalizarUnidade(String unidade) {
        String valor = unidade == null || unidade.isBlank() ? "UN" : unidade.trim();
        return valor.length() <= 30 ? valor : valor.substring(0, 30);
    }

    private String texto(JsonNode no) {
        if (no == null || no.isMissingNode() || no.isNull()) {
            return null;
        }
        if (!no.isValueNode()) {
            throw respostaInesperada();
        }
        String valor = no.asText();
        return valor.isBlank() ? null : valor;
    }

    private BigDecimal decimal(JsonNode no) {
        if (no == null || no.isMissingNode() || no.isNull()) {
            return null;
        }
        try {
            return no.isNumber() ? no.decimalValue() : new BigDecimal(no.asText());
        } catch (NumberFormatException e) {
            throw respostaInesperada();
        }
    }

    private String dataFormulario(String valor) {
        if (valor == null) {
            return null;
        }
        LocalDateTime data;
        try {
            data = ZonedDateTime.parse(valor, DateTimeFormatter.ISO_DATE_TIME)
                    .withZoneSameInstant(BRASILIA).toLocalDateTime();
        } catch (DateTimeParseException zonedException) {
            try {
                data = OffsetDateTime.parse(valor, DateTimeFormatter.ISO_DATE_TIME)
                        .atZoneSameInstant(BRASILIA).toLocalDateTime();
            } catch (DateTimeParseException offsetException) {
                try {
                    data = LocalDateTime.parse(valor, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException localException) {
                    throw respostaInesperada();
                }
            }
        }
        return DATA_FORMULARIO.format(data);
    }

    private PncpConsultaException linkInvalido() {
        return new PncpConsultaException(HttpStatus.BAD_REQUEST, "Informe um link válido do PNCP.");
    }

    private PncpConsultaException indisponivel(String motivo) {
        return new PncpConsultaException(HttpStatus.SERVICE_UNAVAILABLE,
                motivo + " Você ainda pode preencher a licitação manualmente.");
    }

    private PncpConsultaException respostaInesperada() {
        return new PncpConsultaException(HttpStatus.BAD_GATEWAY,
                "O PNCP retornou uma resposta inesperada. Você ainda pode preencher a licitação manualmente.");
    }

    private IllegalArgumentException itensInvalidos() {
        return new IllegalArgumentException(
                "Os itens recebidos do PNCP são inválidos. Busque os dados novamente antes de cadastrar.");
    }

    private record ReferenciaPncp(String cnpj, int ano, long sequencial, String linkNormalizado) {}

    private record CacheEntry<T>(T valor, Instant expiraEm) {}

    private record DadosEMetadados(DadosPncp dados, MetadadosPncp pncp) {}

    public record DadosPncp(String orgao, String numeroEdital, String uasg, String modalidade,
                            String objeto, String dataDisputa, BigDecimal valorEstimadoTotal,
                            String linkEdital) {}

    public record MetadadosPncp(String cnpj, int ano, long sequencial, String numeroControlePNCP) {}

    public record ItemPncp(Integer numeroItem, String descricao, BigDecimal quantidade,
                           String unidade, BigDecimal valorReferencia) {}

    public record ConsultaDadosPncpResponse(DadosPncp dados, MetadadosPncp pncp) {}

    public record ConsultaPncpResponse(DadosPncp dados, MetadadosPncp pncp, List<ItemPncp> itens) {}

    public static class PncpConsultaException extends RuntimeException {
        private final HttpStatus status;

        public PncpConsultaException(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }
}
