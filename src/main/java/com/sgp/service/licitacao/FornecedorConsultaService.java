package com.sgp.service.licitacao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class FornecedorConsultaService {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private final ObjectMapper json;
    private final HttpClient http;
    private final String apiBase;
    private final Duration timeout;

    @Autowired
    public FornecedorConsultaService(ObjectMapper json,
            @Value("${app.licitacoes.cnpj-api-url:https://api.opencnpj.org/}") String apiBase) {
        this(json, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER).build(), apiBase, TIMEOUT);
    }

    FornecedorConsultaService(ObjectMapper json, HttpClient http, String apiBase, Duration timeout) {
        this.json = json;
        this.http = http;
        this.apiBase = apiBase.endsWith("/") ? apiBase : apiBase + "/";
        this.timeout = timeout;
    }

    public DadosFornecedorCnpj consultar(String valor) {
        String cnpj = CnpjUtil.validarENormalizar(valor);
        if (cnpj == null) throw new IllegalArgumentException("Informe o CNPJ que deseja consultar.");
        try {
            HttpRequest requisicao = HttpRequest.newBuilder(URI.create(apiBase + cnpj))
                    .timeout(timeout).header("Accept", "application/json")
                    .header("User-Agent", "SGP-Licitacoes/1.0").GET().build();
            HttpResponse<String> resposta = http.send(requisicao, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() == 404) throw new ConsultaCnpjException(HttpStatus.NOT_FOUND,
                    "CNPJ não encontrado. Você ainda pode cadastrar o fornecedor manualmente.");
            if (resposta.statusCode() < 200 || resposta.statusCode() >= 300)
                throw indisponivel();
            return mapear(cnpj, json.readTree(resposta.body()));
        } catch (HttpTimeoutException e) {
            throw new ConsultaCnpjException(HttpStatus.GATEWAY_TIMEOUT,
                    "A consulta demorou além do esperado. Você ainda pode preencher o cadastro manualmente.");
        } catch (ConsultaCnpjException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw indisponivel();
        } catch (IOException | IllegalArgumentException e) {
            throw indisponivel();
        }
    }

    private DadosFornecedorCnpj mapear(String cnpjConsultado, JsonNode raiz) {
        String razao = texto(raiz, "razao_social");
        String fantasia = texto(raiz, "nome_fantasia");
        String nome = !fantasia.isBlank() ? fantasia : razao;
        if (nome.isBlank()) throw indisponivel();
        String contato = contato(raiz);
        String situacao = texto(raiz, "situacao_cadastral");
        String endereco = endereco(raiz);
        List<String> observacoes = new ArrayList<>();
        if (!situacao.isBlank()) observacoes.add("Situação cadastral: " + situacao);
        if (!endereco.isBlank()) observacoes.add("Endereço: " + endereco);
        String resumo = !razao.isBlank() && !razao.equalsIgnoreCase(nome) ? "Razão social: " + razao : "";
        return new DadosFornecedorCnpj(CnpjUtil.formatar(cnpjConsultado), nome, contato, resumo,
                String.join(" | ", observacoes), situacao);
    }

    private String contato(JsonNode raiz) {
        List<String> partes = new ArrayList<>();
        JsonNode telefones = raiz.path("telefones");
        if (telefones.isArray()) for (JsonNode telefone : telefones) {
            if (telefone.path("is_fax").asBoolean(false)) continue;
            String ddd = texto(telefone, "ddd"), numero = texto(telefone, "numero");
            if (!numero.isBlank()) { partes.add((ddd.isBlank() ? "" : "("+ddd+") ") + numero); break; }
        }
        String email = texto(raiz, "email");
        if (!email.isBlank()) partes.add(email);
        return String.join(" | ", partes);
    }

    private String endereco(JsonNode raiz) {
        List<String> partes = new ArrayList<>();
        String tipo = texto(raiz, "tipo_logradouro"), logradouro = texto(raiz, "logradouro");
        if (!logradouro.isBlank()) partes.add((tipo.isBlank() ? "" : tipo + " ") + logradouro);
        adicionar(partes, texto(raiz, "numero")); adicionar(partes, texto(raiz, "complemento"));
        adicionar(partes, texto(raiz, "bairro"));
        String municipio = texto(raiz, "municipio"), uf = texto(raiz, "uf");
        if (!municipio.isBlank()) partes.add(municipio + (uf.isBlank() ? "" : "/" + uf));
        String cep = texto(raiz, "cep"); if (!cep.isBlank()) partes.add("CEP " + cep);
        return String.join(", ", partes);
    }

    private void adicionar(List<String> partes, String valor) { if (!valor.isBlank()) partes.add(valor); }
    private String texto(JsonNode no, String campo) { return no.path(campo).isTextual() ? no.path(campo).asText().trim() : ""; }
    private ConsultaCnpjException indisponivel() { return new ConsultaCnpjException(HttpStatus.SERVICE_UNAVAILABLE,
            "A consulta pública de CNPJ está indisponível. Preencha o cadastro manualmente."); }

    public record DadosFornecedorCnpj(String cnpj, String nome, String contato, String resumo,
                                      String observacoes, String situacaoCadastral) {}

    public static class ConsultaCnpjException extends RuntimeException {
        private final HttpStatus status;
        public ConsultaCnpjException(HttpStatus status, String mensagem) { super(mensagem); this.status = status; }
        public HttpStatus getStatus() { return status; }
    }
}
