package com.sgp.controller.licitacao;

import com.sgp.service.licitacao.LegacyCsvImportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/licitacoes/importacao-legado")
@PreAuthorize("hasRole('ADMIN')")
public class LegacyCsvImportController {
    private final LegacyCsvImportService service;

    public LegacyCsvImportController(LegacyCsvImportService service) {
        this.service = service;
    }

    @GetMapping
    public String pagina() {
        return "licitacoes/importacao-legado";
    }

    @PostMapping
    public String importar(@RequestParam MultipartFile licitacoes,
                           @RequestParam MultipartFile itens,
                           @RequestParam("itemCotacoes") MultipartFile cotacoes,
                           @RequestParam("stageHistory") MultipartFile historicos,
                           @RequestParam MultipartFile fornecedores,
                           @RequestParam("fornecedorItens") MultipartFile fornecedorItens,
                           Model model) throws IOException {
        LegacyCsvImportService.Arquivos arquivos = new LegacyCsvImportService.Arquivos(
                licitacoes.getBytes(), itens.getBytes(), cotacoes.getBytes(), historicos.getBytes(),
                fornecedores.getBytes(), fornecedorItens.getBytes());
        model.addAttribute("relatorio", service.importar(arquivos));
        return "licitacoes/importacao-legado";
    }
}
