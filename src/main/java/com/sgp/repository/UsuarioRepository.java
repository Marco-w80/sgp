package com.sgp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgp.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
}
