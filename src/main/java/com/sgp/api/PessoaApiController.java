package com.sgp.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sgp.model.Advogado;
import com.sgp.model.Medico;
import com.sgp.model.Pessoa;
import com.sgp.repository.PessoaRepository;


    @RestController
    @RequestMapping("/api/pessoas")
    public class PessoaApiController {

        @Autowired
        private PessoaRepository pessoaRepository;

        @GetMapping("/{id}")
        public ResponseEntity<?> getPessoa(@PathVariable Long id) {
            Pessoa p = pessoaRepository.findById(id).orElse(null);
            if (p == null) return ResponseEntity.notFound().build();

            Map<String, String> dados = new HashMap<>();
            dados.put("nome", p.getNome());
            dados.put("cpf", p.getCpf());
            if (p instanceof Advogado adv) dados.put("oab", adv.getOab());
            if (p instanceof Medico med) dados.put("crm", med.getCrm());

            return ResponseEntity.ok(dados);
        }
    }

    
