package com.sgp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeradorDeSenha {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String senhaPura = "admin123";
        String senhaCriptografada = encoder.encode(senhaPura);

        System.out.println("Senha criptografada: " + senhaCriptografada);
    }
}
