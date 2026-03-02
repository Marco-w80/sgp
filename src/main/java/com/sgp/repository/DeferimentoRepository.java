package com.sgp.repository;

import com.sgp.model.Deferimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeferimentoRepository extends JpaRepository<Deferimento, Long> {

    @Query("select coalesce(max(d.numeroDeferimento), 0) from Deferimento d where d.processo.id = :processoId")
    Integer findMaxNumeroByProcessoId(@Param("processoId") Long processoId);
}
