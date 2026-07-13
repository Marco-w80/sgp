package com.sgp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;

import jakarta.servlet.RequestDispatcher;

class CustomErrorControllerTest {

    private final CustomErrorController controller = new CustomErrorController();

    @Test
    void deveGerarDetalhesSegurosParaErroDePersistencia() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 500);
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/processos/editar/172");
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION,
                new DataIntegrityViolationException("detalhe SQL que não pode ser exibido"));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.exibirErro(request, model);

        assertEquals("error/erro", view);
        assertEquals(500, model.get("status"));
        assertTrue(model.get("causaProvavel").toString().contains("conflito"));
        assertTrue(model.get("detalhesParaSuporte").toString().contains("/processos/editar/172"));
        assertTrue(model.get("codigoSuporte").toString().startsWith("SGP-"));
        assertFalse(model.get("detalhesParaSuporte").toString().contains("detalhe SQL"));
    }

    @Test
    void deveExplicarPaginaNaoEncontrada() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/rota-inexistente");
        ExtendedModelMap model = new ExtendedModelMap();

        controller.exibirErro(request, model);

        assertEquals("Página não encontrada", model.get("titulo"));
        assertTrue(model.get("causaProvavel").toString().contains("não existe"));
    }
}
