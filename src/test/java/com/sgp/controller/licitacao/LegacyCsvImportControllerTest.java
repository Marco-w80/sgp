package com.sgp.controller.licitacao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LegacyCsvImportControllerTest {
    @Autowired MockMvc mvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorAcessaFormularioComOsSeisArquivos() throws Exception {
        mvc.perform(get("/licitacoes/importacao-legado"))
                .andExpect(status().isOk())
                .andExpect(view().name("licitacoes/importacao-legado"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"licitacoes\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"fornecedorItens\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Validar e importar")));
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void usuarioComumNaoAcessaImportacao() throws Exception {
        mvc.perform(get("/licitacoes/importacao-legado")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void processaArquivosEExibeRelatorio() throws Exception {
        mvc.perform(multipart("/licitacoes/importacao-legado")
                        .file(csv("licitacoes", "licitacoes.csv", "id;orgao;numero_edital;uasg;modalidade;objeto;data_disputa;prazo_entrega;valor_estimado_total;precisa_amostra;link_edital;observacoes;current_stage;arquivada;created_by;created_at;updated_at\n"))
                        .file(csv("itens", "itens.csv", "id;licitacao_id;numero_item;descricao;quantidade;unidade;valor_referencia_edital;preco_maximo;percentual_desconto;estrategia_lance;resultado;preco_final;motivo_perda;created_at;updated_at;aprovado\n"))
                        .file(csv("itemCotacoes", "item_cotacoes.csv", "id;item_id;fornecedor;valor_unitario;data_cotacao;observacoes;created_by;created_at\n"))
                        .file(csv("stageHistory", "licitacao_stage_history.csv", "id;licitacao_id;from_stage;to_stage;changed_by;changed_at;observacoes;snapshot_json\n"))
                        .file(csv("fornecedores", "fornecedores.csv", "id;nome;contato;observacoes;created_by;created_at;updated_at;resumo\n"))
                        .file(csv("fornecedorItens", "fornecedor_itens.csv", "id;fornecedor_id;nome;marca;preco;created_at;updated_at\n"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("licitacoes/importacao-legado"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"relatorio-importacao-legado\"")));
    }

    private MockMultipartFile csv(String campo, String nome, String conteudo) {
        return new MockMultipartFile(campo, nome, "text/csv", conteudo.getBytes(StandardCharsets.UTF_8));
    }
}
