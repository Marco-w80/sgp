package com.sgp.controller;

import com.sgp.model.AlertaResumoConfig;
import com.sgp.service.ProcessoResumoPendenteEmailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/intranet/alertas/config")
public class AlertaResumoConfigController {

    private final ProcessoResumoPendenteEmailService processoResumoPendenteEmailService;

    public AlertaResumoConfigController(ProcessoResumoPendenteEmailService processoResumoPendenteEmailService) {
        this.processoResumoPendenteEmailService = processoResumoPendenteEmailService;
    }

    @GetMapping
    public String paginaConfiguracao(Model model) {
        model.addAttribute("config", processoResumoPendenteEmailService.obterOuCriarConfiguracao());
        return "alertas/config-resumo-diario";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam("emailsDestino") String emailsDestino,
                         @RequestParam("diasSemAcesso") Integer diasSemAcesso,
                         @RequestParam("horarioEnvio") String horarioEnvio,
                         @RequestParam(value = "ativo", required = false) Boolean ativo,
                         @RequestParam(value = "enviarSemResultados", required = false) Boolean enviarSemResultados,
                         RedirectAttributes redirectAttributes) {
        AlertaResumoConfig config = processoResumoPendenteEmailService.obterOuCriarConfiguracao();
        config.setEmailsDestino(emailsDestino != null ? emailsDestino.trim() : "");
        config.setDiasSemAcesso(diasSemAcesso != null && diasSemAcesso >= 0 ? diasSemAcesso : 10);
        config.setHorarioEnvio(validarHorario(horarioEnvio));
        config.setAtivo(Boolean.TRUE.equals(ativo));
        config.setEnviarSemResultados(Boolean.TRUE.equals(enviarSemResultados));
        processoResumoPendenteEmailService.salvarConfiguracao(config);

        redirectAttributes.addFlashAttribute("sucesso", "Configuracao salva com sucesso.");
        return "redirect:/intranet/alertas/config";
    }

    @PostMapping("/enviar-teste")
    public String enviarTeste(RedirectAttributes redirectAttributes) {
        int total = processoResumoPendenteEmailService.enviarResumoTesteManual();
        redirectAttributes.addFlashAttribute("sucesso",
                "Envio manual executado. Processos incluidos no e-mail: " + total + ".");
        return "redirect:/intranet/alertas/config";
    }

    private String validarHorario(String horario) {
        if (horario == null || !horario.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            return "09:00";
        }
        return horario;
    }
}
