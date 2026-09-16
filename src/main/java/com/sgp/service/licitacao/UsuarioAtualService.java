package com.sgp.service.licitacao;

import com.sgp.model.Usuario;
import com.sgp.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAtualService {
    private final UsuarioRepository usuarios;
    public UsuarioAtualService(UsuarioRepository usuarios){this.usuarios=usuarios;}
    public Usuario obter(){
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null||!auth.isAuthenticated()||"anonymousUser".equals(auth.getName())) return null;
        return usuarios.findByEmail(auth.getName()).orElse(null);
    }
}
