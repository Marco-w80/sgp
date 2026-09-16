package com.sgp.controller.licitacao;

import com.sgp.service.licitacao.PncpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PncpControllerTest {
    private static final String ENDPOINT = "/api/licitacoes/pncp/consultar";
    private static final String ENDPOINT_DADOS = ENDPOINT + "/dados";
    private static final String ENDPOINT_ITENS = ENDPOINT + "/itens";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PncpService pncp;

    @Test
    void exigeAutenticacao() throws Exception {
        mvc.perform(post(ENDPOINT).with(csrf())
                        .contentType("application/json")
                        .content("{\"link\":\"https://pncp.gov.br/app/editais/46374500000194/2026/7477\"}"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void exigeCsrf() throws Exception {
        mvc.perform(post(ENDPOINT)
                        .contentType("application/json")
                        .content("{\"link\":\"https://pncp.gov.br/app/editais/46374500000194/2026/7477\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void retornaDadosMapeados() throws Exception {
        var dados = new PncpService.DadosPncp("Órgão", "30", "090110", "Pregão",
                "Objeto", "2026-09-24T09:00", new BigDecimal("10.50"),
                "https://pncp.gov.br/app/editais/46374500000194/2026/7477");
        var metadados = new PncpService.MetadadosPncp("46374500000194", 2026, 7477,
                "46374500000194-1-007477/2026");
        var itens = List.of(new PncpService.ItemPncp(1, "Medicamento", new BigDecimal("10"),
                "Comprimido", new BigDecimal("0.25")));
        when(pncp.buscarContratacaoPncp(anyString()))
                .thenReturn(new PncpService.ConsultaPncpResponse(dados, metadados, itens));

        mvc.perform(post(ENDPOINT).with(csrf())
                        .contentType("application/json")
                        .content("{\"link\":\"https://pncp.gov.br/app/editais/46374500000194/2026/7477\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.orgao").value("Órgão"))
                .andExpect(jsonPath("$.dados.dataDisputa").value("2026-09-24T09:00"))
                .andExpect(jsonPath("$.pncp.numeroControlePNCP").value("46374500000194-1-007477/2026"))
                .andExpect(jsonPath("$.itens[0].numeroItem").value(1))
                .andExpect(jsonPath("$.itens[0].descricao").value("Medicamento"));
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void consultaDadosGeraisSeparadamente() throws Exception {
        var dados = new PncpService.DadosPncp("Órgão", "30", "090110", "Pregão",
                "Objeto", "2026-09-24T09:00", new BigDecimal("10.50"),
                "https://pncp.gov.br/app/editais/46374500000194/2026/7477");
        var metadados = new PncpService.MetadadosPncp("46374500000194", 2026, 7477,
                "46374500000194-1-007477/2026");
        when(pncp.buscarDadosContratacaoPncp(anyString()))
                .thenReturn(new PncpService.ConsultaDadosPncpResponse(dados, metadados));

        mvc.perform(post(ENDPOINT_DADOS).with(csrf())
                        .contentType("application/json")
                        .content("{\"link\":\"https://pncp.gov.br/app/editais/46374500000194/2026/7477\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.orgao").value("Órgão"))
                .andExpect(jsonPath("$.pncp.sequencial").value(7477));
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void consultaItensSeparadamente() throws Exception {
        when(pncp.buscarItensContratacaoPncp(anyString())).thenReturn(List.of(
                new PncpService.ItemPncp(1, "Medicamento", new BigDecimal("10"),
                        "Comprimido", new BigDecimal("0.25"))));

        mvc.perform(post(ENDPOINT_ITENS).with(csrf())
                        .contentType("application/json")
                        .content("{\"link\":\"https://pncp.gov.br/app/editais/46374500000194/2026/7477\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].numeroItem").value(1))
                .andExpect(jsonPath("$.itens[0].descricao").value("Medicamento"));
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void retornaErroAmigavelSemStackTrace() throws Exception {
        when(pncp.buscarContratacaoPncp(anyString())).thenThrow(
                new PncpService.PncpConsultaException(HttpStatus.NOT_FOUND,
                        "Não foi possível localizar essa contratação no PNCP."));

        mvc.perform(post(ENDPOINT).with(csrf())
                        .contentType("application/json")
                        .content("{\"link\":\"https://pncp.gov.br/app/editais/46374500000194/2026/999999999\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não foi possível localizar essa contratação no PNCP."))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
