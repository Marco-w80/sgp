package com.sgp.repository.licitacao;
import com.sgp.model.licitacao.LicitacaoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface LicitacaoHistoricoRepository extends JpaRepository<LicitacaoHistorico,Long>{Optional<LicitacaoHistorico> findByLegadoId(Long legadoId);List<LicitacaoHistorico> findByLicitacaoId(Long licitacaoId);}
