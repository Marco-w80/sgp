package com.sgp.repository.licitacao;

import com.sgp.model.licitacao.Licitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LicitacaoRepository extends JpaRepository<Licitacao,Long> {
    List<Licitacao> findByArquivadaOrderByDataDisputaAsc(boolean arquivada);
    Optional<Licitacao> findByLegadoId(Long legadoId);
    @Query("select distinct l from Licitacao l left join l.itens i where l.arquivada=:arquivada and " +
           "(:busca='' or lower(l.orgao) like lower(concat('%',:busca,'%')) or lower(l.numeroEdital) like lower(concat('%',:busca,'%')) " +
           "or lower(l.objeto) like lower(concat('%',:busca,'%')) or cast(i.numeroItem as string) like concat('%',:busca,'%') " +
           "or lower(i.descricao) like lower(concat('%',:busca,'%'))) order by l.dataDisputa asc")
    List<Licitacao> pesquisar(@Param("busca") String busca,@Param("arquivada") boolean arquivada);
}
