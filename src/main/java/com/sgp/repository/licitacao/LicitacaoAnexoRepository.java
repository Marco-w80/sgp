package com.sgp.repository.licitacao;
import com.sgp.model.licitacao.LicitacaoAnexo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface LicitacaoAnexoRepository extends JpaRepository<LicitacaoAnexo,Long>{
    List<LicitacaoAnexo> findByLicitacaoId(Long licitacaoId);
    List<LicitacaoAnexo> findByItemId(Long itemId);
    Optional<LicitacaoAnexo> findByLegadoId(Long legadoId);
}
