package com.sgp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "site/home";
    }

    @GetMapping("/intranet")
    public String intranet() {
        return "intranet/dashboard"; // página a ser criada depois
    }
}
