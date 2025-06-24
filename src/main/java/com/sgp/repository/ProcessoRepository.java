package com.sgp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sgp.dto.MonthCountDto;
import com.sgp.dto.StatusCountDto;
import com.sgp.model.Processo;

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
        
}