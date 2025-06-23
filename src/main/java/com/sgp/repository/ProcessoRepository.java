package com.sgp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sgp.model.Processo;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, Long> {
}