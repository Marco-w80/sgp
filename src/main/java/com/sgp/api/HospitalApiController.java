package com.sgp.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sgp.model.Hospital;
import com.sgp.repository.HospitalRepository;


    @RestController
    @RequestMapping("/api/hospitais")
    public class HospitalApiController {

        @Autowired
        private HospitalRepository hospitalRepository;

        @GetMapping("/buscar")
        public List<Hospital> buscar(@RequestParam String nome) {
            return hospitalRepository.findByNomeContainingIgnoreCase(nome);
        }
}

    
