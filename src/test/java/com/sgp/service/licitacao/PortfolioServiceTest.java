package com.sgp.service.licitacao;

import com.sgp.repository.licitacao.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @Transactional
class PortfolioServiceTest {
 @Autowired PortfolioService service;@Autowired FornecedorItemRepository itens;
 @Test void crudPesquisaEImportacao(){var p=service.salvarEstoque(null,"Seringa","Marca A","Fábrica",BigDecimal.TEN,BigDecimal.ONE);assertThat(service.estoque("Marca A")).extracting("id").contains(p.getId());service.salvarEstoque(p.getId(),"Seringa 2",null,null,null,null);assertThat(service.estoque("Seringa 2")).hasSize(1);service.excluirEstoque(p.getId());assertThat(service.estoque("Seringa 2")).isEmpty();var f=service.salvarFornecedor(null,"Fornecedor Alfa","contato","hospitalar",null);var i=service.salvarItem(f.getId(),null,"Luva","Marca B",BigDecimal.TEN);service.salvarItem(f.getId(),i.getId(),"Luva editada","Marca B",BigDecimal.ONE);int total=service.importarItens(f.getId(),List.of(new ImportacaoTabularService.LinhaFornecedor("Máscara","M",BigDecimal.ONE)));assertThat(total).isEqualTo(1);assertThat(service.fornecedores("Máscara")).hasSize(1);service.excluirItem(f.getId(),i.getId());service.excluirFornecedor(f.getId());assertThat(service.fornecedores("Fornecedor Alfa")).isEmpty();}
}
