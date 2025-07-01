package com.sgp.api;

import com.sgp.dto.CepResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/cep")
public class CepApiController {

    @GetMapping("/{cep}")
    public CepResponse buscarCep(@PathVariable String cep) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://viacep.com.br/ws/" + cep + "/json/";

        CepResponse response = restTemplate.getForObject(url, CepResponse.class);

        if (response == null || response.getCep() == null) {
            throw new RuntimeException("CEP inválido ou não encontrado");
        }

        return response;
    }
}
