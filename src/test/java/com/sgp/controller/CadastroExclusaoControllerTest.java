package com.sgp.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.sgp.model.Advogado;
import com.sgp.model.Doenca;
import com.sgp.model.GrupoProduto;
import com.sgp.model.Hospital;
import com.sgp.model.Local;
import com.sgp.model.Medico;
import com.sgp.model.Paciente;
import com.sgp.model.Processo;
import com.sgp.model.Produto;
import com.sgp.model.Sexo;
import com.sgp.model.StatusProcesso;
import com.sgp.repository.DoencaRepository;
import com.sgp.repository.HospitalRepository;
import com.sgp.repository.LocalRepository;
import com.sgp.repository.PessoaRepository;
import com.sgp.repository.ProcessoRepository;
import com.sgp.repository.ProdutoRepository;

@SpringBootTest
@Transactional
class CadastroExclusaoControllerTest {

    @Autowired private PessoaController pessoaController;
    @Autowired private ProdutoController produtoController;
    @Autowired private LocalController localController;
    @Autowired private HospitalController hospitalController;

    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private LocalRepository localRepository;
    @Autowired private HospitalRepository hospitalRepository;
    @Autowired private DoencaRepository doencaRepository;
    @Autowired private ProcessoRepository processoRepository;

    @Test
    void bloqueiaExclusaoEInformaVinculosComProcessos() {
        Cadastros cadastros = criarCadastrosVinculados();

        ExtendedModelMap modeloPessoas = new ExtendedModelMap();
        pessoaController.list(modeloPessoas);
        Map<Long, Long> vinculosPessoas = mapaVinculos(modeloPessoas);
        assertThat(vinculosPessoas)
                .containsEntry(cadastros.paciente().getId(), 1L)
                .containsEntry(cadastros.advogado().getId(), 1L)
                .containsEntry(cadastros.medico().getId(), 1L);

        ExtendedModelMap modeloProdutos = new ExtendedModelMap();
        produtoController.list(modeloProdutos);
        assertThat(mapaVinculos(modeloProdutos)).containsEntry(cadastros.produto().getId(), 1L);

        ExtendedModelMap modeloLocais = new ExtendedModelMap();
        localController.list(modeloLocais);
        assertThat(mapaVinculos(modeloLocais)).containsEntry(cadastros.local().getId(), 1L);

        ExtendedModelMap modeloHospitais = new ExtendedModelMap();
        hospitalController.listar(modeloHospitais);
        assertThat(mapaVinculos(modeloHospitais)).containsEntry(cadastros.hospital().getId(), 1L);

        assertBloqueado(pessoaController.excluir(cadastros.paciente().getId(), atributos()),
                "/pessoas/listar", cadastros.paciente().getId(), pessoaRepository.existsById(cadastros.paciente().getId()));
        assertBloqueado(pessoaController.excluir(cadastros.advogado().getId(), atributos()),
                "/pessoas/listar", cadastros.advogado().getId(), pessoaRepository.existsById(cadastros.advogado().getId()));
        assertBloqueado(pessoaController.excluir(cadastros.medico().getId(), atributos()),
                "/pessoas/listar", cadastros.medico().getId(), pessoaRepository.existsById(cadastros.medico().getId()));

        assertThat(produtoController.excluir(cadastros.produto().getId(), atributos()))
                .isEqualTo("redirect:/produtos/listar");
        assertThat(localController.excluir(cadastros.local().getId(), atributos()))
                .isEqualTo("redirect:/locais/listar");
        assertThat(hospitalController.excluir(cadastros.hospital().getId(), atributos()))
                .isEqualTo("redirect:/hospitais");

        assertThat(produtoRepository.existsById(cadastros.produto().getId())).isTrue();
        assertThat(localRepository.existsById(cadastros.local().getId())).isTrue();
        assertThat(hospitalRepository.existsById(cadastros.hospital().getId())).isTrue();
    }

    @Test
    void excluiCadastrosQueNaoPossuemVinculoComProcessos() {
        Paciente pessoa = pessoaRepository.save(new Paciente(
                "Pessoa livre", Sexo.OUTRO, LocalDate.of(1991, 1, 1), "70000000001", "LIVRE-P"));
        Produto produto = produtoRepository.save(produto("PROD-LIVRE", "Produto livre"));
        Local local = localRepository.save(local("Comarca livre"));
        Hospital hospital = hospitalRepository.save(hospital("Hospital livre"));

        assertThat(pessoaController.excluir(pessoa.getId(), atributos())).isEqualTo("redirect:/pessoas/listar");
        assertThat(produtoController.excluir(produto.getId(), atributos())).isEqualTo("redirect:/produtos/listar");
        assertThat(localController.excluir(local.getId(), atributos())).isEqualTo("redirect:/locais/listar");
        assertThat(hospitalController.excluir(hospital.getId(), atributos())).isEqualTo("redirect:/hospitais");

        assertThat(pessoaRepository.existsById(pessoa.getId())).isFalse();
        assertThat(produtoRepository.existsById(produto.getId())).isFalse();
        assertThat(localRepository.existsById(local.getId())).isFalse();
        assertThat(hospitalRepository.existsById(hospital.getId())).isFalse();
    }

    private Cadastros criarCadastrosVinculados() {
        Paciente paciente = pessoaRepository.save(new Paciente(
                "Paciente vinculado", Sexo.OUTRO, LocalDate.of(1990, 1, 1), "70000000002", "VINC-P"));
        Advogado advogado = pessoaRepository.save(new Advogado(
                "Advogado vinculado", Sexo.OUTRO, LocalDate.of(1980, 2, 2), "70000000003", "VINC-A", "OAB-VINC"));
        Medico medico = pessoaRepository.save(new Medico(
                "Médico vinculado", Sexo.OUTRO, LocalDate.of(1970, 3, 3), "70000000004", "VINC-M", "CRM-VINC"));
        Produto produto = produtoRepository.save(produto("PROD-VINC", "Produto vinculado"));
        Local local = localRepository.save(local("Comarca vinculada"));
        Hospital hospital = hospitalRepository.save(hospital("Hospital vinculado"));

        Doenca doenca = new Doenca();
        doenca.setNome("Doença vínculo exclusão");
        doenca = doencaRepository.save(doenca);

        Processo processo = new Processo("PROC-VINC-EXCLUSAO", paciente, LocalDate.now(), StatusProcesso.ABERTO);
        processo.setAdvogado(advogado);
        processo.setMedico(medico);
        processo.setHospital(hospital);
        processo.setLocal(local);
        processo.setDoenca(doenca);
        processo.addItem(produto, LocalDate.now(), 1);
        processoRepository.saveAndFlush(processo);

        return new Cadastros(paciente, advogado, medico, produto, local, hospital);
    }

    private Produto produto(String codigo, String nome) {
        return new Produto(codigo, nome, "Unidade", "UN", GrupoProduto.OUTROS, LocalDate.now(), null);
    }

    private Local local(String comarca) {
        return new Local(comarca, "Cível", "1", 1, "Centro", null);
    }

    private Hospital hospital(String nome) {
        Hospital hospital = new Hospital();
        hospital.setNome(nome);
        return hospital;
    }

    private RedirectAttributesModelMap atributos() {
        return new RedirectAttributesModelMap();
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Long> mapaVinculos(ExtendedModelMap modelo) {
        return (Map<Long, Long>) modelo.get("vinculosProcessos");
    }

    private void assertBloqueado(String redirecionamento, String rota, Long id, boolean aindaExiste) {
        assertThat(redirecionamento).isEqualTo("redirect:" + rota);
        assertThat(id).isNotNull();
        assertThat(aindaExiste).isTrue();
    }

    private record Cadastros(Paciente paciente, Advogado advogado, Medico medico,
                             Produto produto, Local local, Hospital hospital) {
    }
}
