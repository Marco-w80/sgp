package com.sgp.repository;

import com.sgp.model.ProcessoLog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessoLogRepository extends JpaRepository<ProcessoLog, Long> {

        List<ProcessoLog> findByProcessoId(Long processoId);

        Optional<ProcessoLog> findTopByProcessoIdAndTipoEventoOrderByDataHoraDesc(Long processoId, String tipoEvento);


}
