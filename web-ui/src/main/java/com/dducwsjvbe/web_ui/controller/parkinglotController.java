package com.dducwsjvbe.web_ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/parking-lot")
public class parkinglotController {
    @GetMapping("/control")
    public String admin(Model model) {
        return "index";
    }
}
