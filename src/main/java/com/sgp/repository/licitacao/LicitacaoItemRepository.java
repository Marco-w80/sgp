package com.sgp.repository.licitacao;
import com.sgp.model.licitacao.LicitacaoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface LicitacaoItemRepository extends JpaRepository<LicitacaoItem,Long>{
    List<LicitacaoItem> findByLicitacaoIdOrderByNumeroItem(Long licitacaoId);
    boolean existsByLicitacaoIdAndSelecionadoCotacaoTrueAndDecisao(Long licitacaoId, com.sgp.model.licitacao.DecisaoItem decisao);
    Optional<LicitacaoItem> findByLegadoId(Long legadoId);
}
