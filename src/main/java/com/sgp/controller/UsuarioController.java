package com.sgp.controller;

import com.sgp.entity.Usuario;
import com.sgp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // <- ESSA LINHA É ESSENCIAL
import org.springframework.web.bind.annotation.*;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private PasswordEncoder encoder;

    @GetMapping("/register")
    public String formRegister(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/register";
    }

    @PostMapping("/register")
    public String save(@ModelAttribute Usuario usuario) {
        usuario.setSenha(encoder.encode(usuario.getSenha())); // <- Criptografia aqui
        usuarioRepo.save(usuario);
        return "redirect:/login?registered=true";
    }

}