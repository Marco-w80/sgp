package com.sgp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sgp.dto.MonthCountDto;
import com.sgp.dto.StatusCountDto;
import com.sgp.model.Processo;
import com.sgp.model.StatusProcesso;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, Long> {


    @Query("SELECT new com.sgp.dto.StatusCountDto(p.status, COUNT(p)) "
         + "FROM Processo p GROUP BY p.status")
    List<StatusCountDto> countByStatus();

    
    @Query("SELECT new com.sgp.dto.MonthCountDto(" +
            "YEAR(p.dataInicio), MONTH(p.dataInicio), COUNT(p)) " +
            "FROM Processo p " +
            "GROUP BY YEAR(p.dataInicio), MONTH(p.dataInicio) " +
            "ORDER BY YEAR(p.dataInicio), MONTH(p.dataInicio)")
        List<MonthCountDto> countByMonth();


         @Query("""
      SELECT p FROM Processo p
      WHERE (:de IS NULL OR p.dataInicio >= :de)
        AND (:ate IS NULL OR p.dataInicio <= :ate)
        AND (:status IS NULL OR p.status = :status)
        AND (:localId IS NULL OR p.local.id = :localId)
        AND (:cpfAnexado IS NULL OR p.cpfAnexado = :cpfAnexado)
        AND (:compResidenciaAnexado IS NULL OR p.compResidenciaAnexado = :compResidenciaAnexado)
        AND (:compRendaAnexado IS NULL OR p.compRendaAnexado = :compRendaAnexado)
        AND (:procuracaoAnexado IS NULL OR p.procuracaoAnexado = :procuracaoAnexado)
        AND (:declaracaoInsuficienciaAnexado IS NULL OR p.declaracaoInsuficienciaAnexado = :declaracaoInsuficienciaAnexado)
        AND (:paciente IS NULL OR LOWER(p.paciente.nome) LIKE LOWER(CONCAT('%', :paciente, '%')) OR p.paciente.cpf LIKE CONCAT('%', :paciente, '%'))
      """)
    List<Processo> findByFiltros(
            @Param("de") LocalDate de,
            @Param("ate") LocalDate ate,
            @Param("status") StatusProcesso status,
            @Param("paciente") String paciente,
            @Param("localId") Long localId,
            @Param("cpfAnexado") Boolean cpfAnexado,
            @Param("compResidenciaAnexado") Boolean compResidenciaAnexado,
            @Param("compRendaAnexado") Boolean compRendaAnexado,
            @Param("procuracaoAnexado") Boolean procuracaoAnexado,
            @Param("declaracaoInsuficienciaAnexado") Boolean declaracaoInsuficienciaAnexado
    );
}