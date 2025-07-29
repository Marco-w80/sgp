package com.sgp.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sgp.dto.ProcessoPendenteDTO;
import com.sgp.model.StatusProcesso;
import com.sgp.service.ProcessoService;

@Controller
public class HomeController {

    @Autowired
    private ProcessoService processoService;

    @GetMapping("/")
    public String home() {
        // return "site/home";
        return "login";
    }

    @GetMapping("/intranet")
    public String intranet(
            @RequestParam(name = "dias", required = false, defaultValue = "0") Integer diasMinimos,
            Model model) {

        List<ProcessoPendenteDTO> pendencias =
                processoService.listarProcessosComDocumentacaoPendenteComMinDias(diasMinimos);

        Map<StatusProcesso, Long> statusCounts = processoService.contarPorStatus();

        model.addAttribute("pendencias", pendencias);
        model.addAttribute("totalPendentes", pendencias.size());
        model.addAttribute("diasMinimos", diasMinimos);
        model.addAttribute("statusCounts", statusCounts);

        // opcional: classes CSS por status
        Map<StatusProcesso, String> css = Map.of(
            StatusProcesso.ABERTO, "border-left-info",
            StatusProcesso.EM_ANDAMENTO, "border-left-primary",
            StatusProcesso.CONCLUIDO, "border-left-success",
            StatusProcesso.SUSPENSO, "border-left-danger"
        );
        model.addAttribute("statusCss", css);

        return "intranet/dashboard";
    }





}

