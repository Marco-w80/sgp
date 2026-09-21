package com.sgp.repository.licitacao;
import com.sgp.model.licitacao.FornecedorLicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface FornecedorLicitacaoRepository extends JpaRepository<FornecedorLicitacao,Long>{
    Optional<FornecedorLicitacao> findByLegadoId(Long legadoId);
    Optional<FornecedorLicitacao> findByCnpj(String cnpj);
    List<FornecedorLicitacao> findAllByOrderByNomeAsc();
    @Query("select distinct f from FornecedorLicitacao f left join f.itens i where :q='' or lower(f.nome) like lower(concat('%',:q,'%')) or lower(f.cnpj) like lower(concat('%',:q,'%')) or lower(f.resumo) like lower(concat('%',:q,'%')) or lower(i.nome) like lower(concat('%',:q,'%')) or lower(i.marca) like lower(concat('%',:q,'%')) order by f.nome")
    List<FornecedorLicitacao> pesquisar(@Param("q") String q);
}
