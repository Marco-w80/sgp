package com.sgp.service;

import com.sgp.model.Usuario;
import com.sgp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // Aqui pega o perfil real do banco
        String role = usuario.getPerfil().name(); // ADMIN ou USUARIO

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha()) // se usar BCrypt, mantenha encode
                .roles(role) // agora usa o perfil do banco!
                .build();
    }
}
