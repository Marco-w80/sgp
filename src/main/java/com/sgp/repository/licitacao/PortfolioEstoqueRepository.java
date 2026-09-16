package com.sgp.repository.licitacao;
import com.sgp.model.licitacao.PortfolioEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface PortfolioEstoqueRepository extends JpaRepository<PortfolioEstoque,Long>{
    Optional<PortfolioEstoque> findByLegadoId(Long legadoId);
    @Query("select p from PortfolioEstoque p where :q='' or lower(p.nome) like lower(concat('%',:q,'%')) or lower(p.marca) like lower(concat('%',:q,'%')) or lower(p.fabricante) like lower(concat('%',:q,'%')) order by p.nome")
    List<PortfolioEstoque> pesquisar(@Param("q") String q);
}
