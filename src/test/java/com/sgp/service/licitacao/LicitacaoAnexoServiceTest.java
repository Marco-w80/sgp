package com.sgp.service.licitacao;

import com.sgp.model.licitacao.Licitacao;
import com.sgp.repository.licitacao.LicitacaoAnexoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @Transactional
class LicitacaoAnexoServiceTest {
 @Autowired LicitacaoAnexoService anexos;@Autowired LicitacaoService licitacoes;@Autowired LicitacaoAnexoRepository repositorio;
 private Licitacao nova(){Licitacao l=new Licitacao();l.setOrgao("Órgão");l.setNumeroEdital("A-1");l.setObjeto("Objeto");l.setDataDisputa(LocalDateTime.now().plusDays(1));return licitacoes.criar(l);}
 @Test void uploadDownloadExclusaoEFormatoInvalido()throws Exception{Licitacao l=nova();var a=anexos.salvar(l.getId(),"Proposta comercial",null,null,new MockMultipartFile("arquivo","teste.png","image/png",new byte[]{1,2,3}));assertThat(a.getNomeExibicao()).isEqualTo("Proposta comercial");assertThat(anexos.download(a.getId()).recurso().exists()).isTrue();anexos.excluir(l.getId(),a.getId());assertThat(repositorio.findById(a.getId())).isEmpty();assertThatThrownBy(()->anexos.salvar(l.getId(),"Arquivo suspeito",null,null,new MockMultipartFile("arquivo","malware.exe","application/octet-stream",new byte[]{1}))).isInstanceOf(IllegalArgumentException.class);}
 @Test void exigeNomeDoAnexo(){Licitacao l=nova();assertThatThrownBy(()->anexos.salvar(l.getId(),"   ",null,null,new MockMultipartFile("arquivo","teste.pdf","application/pdf",new byte[]{1}))).isInstanceOf(IllegalArgumentException.class).hasMessage("Informe o nome do anexo.");}
 @Test void bloqueiaArquivoMaiorQueVinteMb(){Licitacao l=nova();byte[] grande=new byte[(int)LicitacaoAnexoService.LIMITE+1];assertThatThrownBy(()->anexos.salvar(l.getId(),"Arquivo grande",null,null,new MockMultipartFile("arquivo","grande.pdf","application/pdf",grande))).isInstanceOf(IllegalArgumentException.class);}
}
