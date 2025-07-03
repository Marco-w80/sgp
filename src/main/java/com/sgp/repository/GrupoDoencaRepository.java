package com.sgp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sgp.model.GrupoDoenca;

public interface GrupoDoencaRepository extends JpaRepository<GrupoDoenca, Long> {
    
}
