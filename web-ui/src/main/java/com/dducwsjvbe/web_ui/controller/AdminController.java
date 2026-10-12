package com.dducwsjvbe.web_ui.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @Value("${keycloak.url}")
    private String KeycloakUrl;

    @Value("${Monitoring.url}")
    private String MonitoringUrl;


    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("KeycloakUrl", KeycloakUrl);
        model.addAttribute("MonitoringUrl", MonitoringUrl);

        return "admin";
    }
}
