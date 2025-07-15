package com.sgp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sgp.model.MaintenanceLog;

public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {

    @Query("SELECT COALESCE(SUM(m.durationMinutes),0) FROM MaintenanceLog m")
    Integer sumTotalMinutes();

    List<MaintenanceLog> findAllByOrderByDateDesc();


    @Query("""
      SELECT COALESCE(SUM(m.durationMinutes),0)
      FROM MaintenanceLog m
      WHERE m.date BETWEEN :start AND :end
    """)
    Integer sumMinutesBetween(
      @Param("start") LocalDate start,
      @Param("end")   LocalDate end);

}
