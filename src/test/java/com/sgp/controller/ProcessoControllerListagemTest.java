package com.sgp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import com.sgp.model.StatusProcesso;
import com.sgp.service.ProcessoService;

@ExtendWith(MockitoExtension.class)
class ProcessoControllerListagemTest {

    @Mock
    private ProcessoService processoService;

    private ProcessoController controller;

    @BeforeEach
    void configurar() {
        controller = new ProcessoController();
        ReflectionTestUtils.setField(controller, "processoService", processoService);
    }

    @Test
    void paginaDeAtivosReuneAbertosEEmAndamento() {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.listarAtivos(null, null, model);

        assertThat(view).isEqualTo("processos/listar-processos");
        assertThat(model.get("tituloListagem")).isEqualTo("Processos ativos");
        assertThat(model.get("rotaListagem")).isEqualTo("/processos/ativos");
        assertThat(statusesConsultados()).containsExactly(StatusProcesso.ABERTO, StatusProcesso.EM_ANDAMENTO);
    }

    @Test
    void paginaDeSuspensosUsaStatusSuspenso() {
        ExtendedModelMap model = new ExtendedModelMap();
        controller.listarSuspensos(null, null, model);

        assertThat(model.get("rotaListagem")).isEqualTo("/processos/suspensos");
        assertThat(statusesConsultados()).containsExactly(StatusProcesso.SUSPENSO);
    }

    @Test
    void paginaDeObitosIncluiStatusAtualELegado() {
        ExtendedModelMap model = new ExtendedModelMap();
        controller.listarObitos(null, null, model);

        assertThat(model.get("rotaListagem")).isEqualTo("/processos/obitos");
        assertThat(statusesConsultados()).containsExactly(StatusProcesso.OBITO, StatusProcesso.CONCLUIDO);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<StatusProcesso> statusesConsultados() {
        ArgumentCaptor<List<StatusProcesso>> captor = ArgumentCaptor.forClass(List.class);
        verify(processoService).listarProcessosComFiltroAcompanhamentoPorStatus(
                nullable(Integer.class), nullable(Integer.class), captor.capture());
        return captor.getValue();
    }
}
