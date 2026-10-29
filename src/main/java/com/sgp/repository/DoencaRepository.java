package com.sgp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgp.model.Doenca;

public interface DoencaRepository extends JpaRepository<Doenca, Long> {

    List<Doenca> findByGrupoId(Long grupoId);
    boolean existsByNomeIgnoreCaseAndGrupoId(String nome, Long grupoId);
    boolean existsByNomeIgnoreCaseAndGrupoIdAndIdNot(String nome, Long grupoId, Long id);
}
