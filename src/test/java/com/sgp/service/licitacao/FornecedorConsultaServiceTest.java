package com.sgp.service.licitacao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

class FornecedorConsultaServiceTest {
    @Test void validaCnpjNumericoEAlfanumerico() {
        assertThat(CnpjUtil.validarENormalizar("33.000.167/0001-01")).isEqualTo("33000167000101");
        assertThat(CnpjUtil.validarENormalizar("00.000.000/E08G-12")).isEqualTo("00000000E08G12");
        assertThatThrownBy(() -> CnpjUtil.validarENormalizar("33.000.167/0001-02"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void consultaEMapeiaDadosDoFornecedor() throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/33000167000101", troca -> {
            byte[] corpo = ("{\"cnpj\":\"33000167000101\",\"razao_social\":\"PETROLEO BRASILEIRO S A PETROBRAS\","+
                    "\"nome_fantasia\":\"PETROBRAS - EDISE\",\"situacao_cadastral\":\"Ativa\","+
                    "\"tipo_logradouro\":\"AVENIDA\",\"logradouro\":\"REPUBLICA DO CHILE\",\"numero\":\"65\","+
                    "\"bairro\":\"CENTRO\",\"municipio\":\"RIO DE JANEIRO\",\"uf\":\"RJ\",\"cep\":\"20031170\","+
                    "\"email\":\"contato@empresa.test\",\"telefones\":[{\"ddd\":\"21\",\"numero\":\"21660000\",\"is_fax\":false}]}")
                    .getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/json");troca.sendResponseHeaders(200, corpo.length);troca.getResponseBody().write(corpo);troca.close();
        });
        servidor.start();
        try {
            var service = new FornecedorConsultaService(new ObjectMapper(), HttpClient.newHttpClient(),
                    "http://localhost:"+servidor.getAddress().getPort(), Duration.ofSeconds(2));
            var dados = service.consultar("33.000.167/0001-01");
            assertThat(dados.cnpj()).isEqualTo("33.000.167/0001-01");
            assertThat(dados.nome()).isEqualTo("PETROBRAS - EDISE");
            assertThat(dados.contato()).contains("(21) 21660000", "contato@empresa.test");
            assertThat(dados.resumo()).contains("PETROLEO BRASILEIRO");
            assertThat(dados.observacoes()).contains("Situação cadastral: Ativa", "RIO DE JANEIRO/RJ");
        } finally { servidor.stop(0); }
    }
}
