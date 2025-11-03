package com.sgp.repository;

import com.sgp.model.ProcessoExcluido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessoExcluidoRepository extends JpaRepository<ProcessoExcluido, Long> {
    // Nenhum método customizado é necessário por enquanto.
    // O .save() que vamos usar já vem do JpaRepository.
}