package com.sgp.api;

import com.sgp.dto.CepResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/cep")
public class CepApiController {

    private static final Logger logger = LoggerFactory.getLogger(CepApiController.class);

    @GetMapping("/{cep}")
    public ResponseEntity<CepResponse> buscarCep(@PathVariable String cep) {
        logger.info("=============================================");
        logger.info("Recebida requisição para o CEP: {}", cep);

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://brasilapi.com.br/api/cep/v1/" + cep;
        logger.info("Consultando URL externa: {}", url);

        try {
            // Passo 1: Pega a resposta como um Map genérico.
            ResponseEntity<Map> responseDaApiExterna = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> corpoDaResposta = responseDaApiExterna.getBody();
            logger.info("Resposta recebida da BrasilAPI: {}", corpoDaResposta);

            if (corpoDaResposta == null) {
                logger.warn("Corpo da resposta da API externa veio nulo.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Passo 2: Mapeia manualmente os dados para o CepResponse.
            CepResponse responseParaFrontend = new CepResponse();
            responseParaFrontend.setCep((String) corpoDaResposta.get("cep"));
            responseParaFrontend.setLogradouro((String) corpoDaResposta.get("street"));
            responseParaFrontend.setBairro((String) corpoDaResposta.get("neighborhood"));
            responseParaFrontend.setLocalidade((String) corpoDaResposta.get("city"));
            responseParaFrontend.setUf((String) corpoDaResposta.get("state"));

            logger.info("Objeto mapeado para o front-end: {}", responseParaFrontend);

            // Passo 3: Retorna nosso objeto preenchido.
            return ResponseEntity.ok(responseParaFrontend);

        } catch (HttpClientErrorException.NotFound e) {
            logger.error("ERRO: A API externa retornou 404 (Não Encontrado) para o CEP: {}", cep);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("ERRO CRÍTICO ao tentar buscar o CEP: {}", cep, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}