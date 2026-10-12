package com.dducwsjvbe.web_ui.controller;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffController {
    @GetMapping("/staff")
    public String admin() {
        return "staff";
    }
}
