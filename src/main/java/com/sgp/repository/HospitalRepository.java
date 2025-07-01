package com.sgp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgp.model.Hospital;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    List<Hospital> findByNomeContainingIgnoreCase(String nome);
}
