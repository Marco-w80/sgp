package com.sgp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sgp.model.GrupoDoenca;

public interface GrupoDoencaRepository extends JpaRepository<GrupoDoenca, Long> {
    Optional<GrupoDoenca> findByNomeIgnoreCase(String nome);
}
