package com.sgp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.sgp.model.Doenca;
import com.sgp.model.GrupoDoenca;
import com.sgp.repository.DoencaRepository;
import com.sgp.repository.GrupoDoencaRepository;

@ExtendWith(MockitoExtension.class)
class DoencaControllerTest {

    @Mock
    private DoencaRepository doencaRepository;

    @Mock
    private GrupoDoencaRepository grupoRepository;

    private DoencaController controller;

    @BeforeEach
    void configurar() {
        controller = new DoencaController(doencaRepository, grupoRepository);
    }

    @Test
    void deveCriarCategoriaReservadaNoPrimeiroCadastroDeDoencaGeral() {
        when(grupoRepository.findByNomeIgnoreCase(DoencaController.GRUPO_OUTRAS_DOENCAS))
                .thenReturn(Optional.empty());
        when(grupoRepository.save(any(GrupoDoenca.class))).thenAnswer(invocacao -> {
            GrupoDoenca grupo = invocacao.getArgument(0);
            grupo.setId(90L);
            return grupo;
        });
        when(doencaRepository.existsByNomeIgnoreCaseAndGrupoId("Esclerose múltipla", 90L))
                .thenReturn(false);

        String destino = controller.cadastrarGeral("  Esclerose múltipla  ", new RedirectAttributesModelMap());

        assertEquals("redirect:/doencas/cadastrar", destino);
        ArgumentCaptor<Doenca> captor = ArgumentCaptor.forClass(Doenca.class);
        verify(doencaRepository).save(captor.capture());
        assertEquals("Esclerose múltipla", captor.getValue().getNome());
        assertEquals(DoencaController.GRUPO_OUTRAS_DOENCAS, captor.getValue().getGrupo().getNome());
    }

    @Test
    void deveReutilizarCategoriaReservadaExistente() {
        GrupoDoenca grupo = grupo(90L, DoencaController.GRUPO_OUTRAS_DOENCAS);
        when(grupoRepository.findByNomeIgnoreCase(DoencaController.GRUPO_OUTRAS_DOENCAS))
                .thenReturn(Optional.of(grupo));
        when(doencaRepository.existsByNomeIgnoreCaseAndGrupoId("Lúpus", 90L)).thenReturn(false);

        controller.cadastrarGeral("Lúpus", new RedirectAttributesModelMap());

        verify(grupoRepository, never()).save(any(GrupoDoenca.class));
        ArgumentCaptor<Doenca> captor = ArgumentCaptor.forClass(Doenca.class);
        verify(doencaRepository).save(captor.capture());
        assertSame(grupo, captor.getValue().getGrupo());
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveSepararDoencasGeraisDasLocalizacoesTumoraisNaTela() {
        GrupoDoenca cabeca = grupo(1L, "Cabeça e pescoço");
        GrupoDoenca outras = grupo(90L, DoencaController.GRUPO_OUTRAS_DOENCAS);
        Doenca lupus = doenca(8L, "Lúpus", outras);
        when(grupoRepository.findAll()).thenReturn(List.of(outras, cabeca));
        when(grupoRepository.findByNomeIgnoreCase(DoencaController.GRUPO_OUTRAS_DOENCAS))
                .thenReturn(Optional.of(outras));
        when(doencaRepository.findByGrupoId(90L)).thenReturn(List.of(lupus));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.mostrarCadastro(null, model);

        assertEquals("doencas/cadastrar", view);
        assertEquals(List.of(cabeca), (List<GrupoDoenca>) model.get("grupos"));
        assertEquals(List.of(lupus), (List<Doenca>) model.get("outrasDoencas"));
    }

    private GrupoDoenca grupo(Long id, String nome) {
        GrupoDoenca grupo = new GrupoDoenca();
        grupo.setId(id);
        grupo.setNome(nome);
        return grupo;
    }

    private Doenca doenca(Long id, String nome, GrupoDoenca grupo) {
        Doenca doenca = new Doenca();
        doenca.setId(id);
        doenca.setNome(nome);
        doenca.setGrupo(grupo);
        return doenca;
    }
}
