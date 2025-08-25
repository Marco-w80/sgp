package com.sgp.repository;

import com.sgp.model.ProcessoProduto;
import com.sgp.projections.Projections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessoProdutoRepository extends JpaRepository<ProcessoProduto, Long> {

    // Consumo total por produto (agrupa por ID do produto)
    @Query("""
           select cast(prod.id as string) as produto,
                  sum(pp.quantidade) as quantidadeTotal
           from ProcessoProduto pp
           join pp.produto prod
           group by prod.id
           order by quantidadeTotal desc
           """)
    List<Projections.ProdutoConsumoProjection> consumoTotalPorProduto();

    // Consumo mensal por produto (YYYY-MM), agrupado por ID
    @Query("""
           select cast(prod.id as string) as produto,
                  function('date_format', pp.dataEnvio, '%Y-%m') as anoMes,
                  sum(pp.quantidade) as quantidade
           from ProcessoProduto pp
           join pp.produto prod
           group by prod.id, function('date_format', pp.dataEnvio, '%Y-%m')
           order by prod.id, function('date_format', pp.dataEnvio, '%Y-%m')
           """)
    List<Projections.ProdutoConsumoMensalProjection> consumoMensalPorProduto();

}
