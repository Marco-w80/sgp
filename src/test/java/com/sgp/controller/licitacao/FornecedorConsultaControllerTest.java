package com.sgp.controller.licitacao;

import com.sgp.service.licitacao.FornecedorConsultaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class FornecedorConsultaControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean FornecedorConsultaService consulta;

    @Test @WithMockUser(roles="USUARIO") void retornaDadosParaPreencherFormulario() throws Exception {
        when(consulta.consultar(anyString())).thenReturn(new FornecedorConsultaService.DadosFornecedorCnpj(
                "33.000.167/0001-01","PETROBRAS","(21) 21660000","Razão social: PETROLEO BRASILEIRO",
                "Situação cadastral: Ativa","Ativa"));
        mvc.perform(post("/api/licitacoes/fornecedores/consultar-cnpj").with(csrf())
                .contentType("application/json").content("{\"cnpj\":\"33000167000101\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("PETROBRAS"))
                .andExpect(jsonPath("$.cnpj").value("33.000.167/0001-01"));
    }

    @Test @WithMockUser(roles="USUARIO") void exigeCsrf() throws Exception {
        mvc.perform(post("/api/licitacoes/fornecedores/consultar-cnpj")
                .contentType("application/json").content("{\"cnpj\":\"33000167000101\"}"))
                .andExpect(status().isForbidden());
    }
}
