package com.sgp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sgp.model.Local;

@Repository
public interface LocalRepository extends JpaRepository<Local, Long> {
    
}