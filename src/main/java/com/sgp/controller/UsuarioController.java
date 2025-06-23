package com.sgp.controller;

import com.sgp.model.Usuario;
import com.sgp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private PasswordEncoder encoder;

    // FORMULÁRIO DE CADASTRO (novo)
    @GetMapping("/cadastrar")
    public String formCreate(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/cadastrar-usuario";
    }

    @PostMapping("/cadastrar")
    public String create(@ModelAttribute Usuario usuario) {
        usuario.setSenha(encoder.encode(usuario.getSenha()));
        usuarioRepo.save(usuario);
        return "redirect:/usuarios?created=true";
    }

    // LISTAR
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("usuarios", usuarioRepo.findAll());
        return "usuarios/listar-usuarios";
    }

    // EDIÇÃO (já existente, mas ajustado pra ficar sob /usuarios)
    @GetMapping("/editar/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioRepo.findById(id).orElseThrow());
        return "usuarios/editar-usuario";
    }

    @PostMapping("/editar/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Usuario form) {
        Usuario u = usuarioRepo.findById(id).orElseThrow();
        u.setNome(form.getNome());
        u.setEmail(form.getEmail());
        if (!form.getSenha().isBlank()) {
            u.setSenha(encoder.encode(form.getSenha()));
        }
        usuarioRepo.save(u);
        return "redirect:/usuarios?updated=true";
    }
}
