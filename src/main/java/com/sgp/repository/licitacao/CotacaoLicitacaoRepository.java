package com.sgp.repository.licitacao;
import com.sgp.model.licitacao.CotacaoLicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface CotacaoLicitacaoRepository extends JpaRepository<CotacaoLicitacao,Long>{
    List<CotacaoLicitacao> findByItemLicitacaoIdOrderByItemNumeroItemAscDataCotacaoDesc(Long licitacaoId);
    Optional<CotacaoLicitacao> findByLegadoId(Long legadoId);
    @Modifying @Query("update CotacaoLicitacao c set c.fornecedor=null where c.fornecedor.id=:id")
    int desvincularFornecedor(@Param("id") Long fornecedorId);
}
