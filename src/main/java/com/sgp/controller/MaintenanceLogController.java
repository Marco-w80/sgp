package com.sgp.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sgp.dto.MaintenanceLogForm;
import com.sgp.model.MaintenanceLog;
import com.sgp.model.MaintenanceType;
import com.sgp.service.MaintenanceLogService;

@Controller
@RequestMapping("/maintenance")
public class MaintenanceLogController {

    private final MaintenanceLogService service;

    public MaintenanceLogController(MaintenanceLogService service) {
        this.service = service;
    }

    @GetMapping
    public String showPage(Model model) {
    // dados já existentes
    model.addAttribute("entries", service.findAll());
    model.addAttribute("totalMinutes", service.getTotalMinutes());
    // novos atributos
    int monthlyMin = service.getMonthlyMinutes();
    double monthlyHours = monthlyMin / 60.0;
    int percent = (int) Math.min((monthlyHours/20.0)*100, 100);
    model.addAttribute("monthlyHours", monthlyHours);
    model.addAttribute("monthlyPercent", percent);
    // formulário, tipos, usuários…
    model.addAttribute("form", new MaintenanceLogForm());
    model.addAttribute("types", MaintenanceType.values());
    model.addAttribute("users", List.of("Samuel", "Marco"));
    return "manutencao/maintenance";
    }


    @PostMapping("/save")
    public String saveEntry(
            @Valid @ModelAttribute("form") MaintenanceLogForm form,
            BindingResult br,
            RedirectAttributes ra) {

        if (br.hasErrors()) {
            ra.addFlashAttribute(
                "org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            return "redirect:/maintenance";
        }

        MaintenanceLog log = new MaintenanceLog();
        BeanUtils.copyProperties(form, log);
        service.save(log);

        return "redirect:/maintenance";
    }
}
