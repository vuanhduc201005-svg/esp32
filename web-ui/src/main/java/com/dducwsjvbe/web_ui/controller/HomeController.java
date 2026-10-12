package com.dducwsjvbe.web_ui.controller;

import com.dducwsjvbe.web_ui.dto.LoginRequestDto;
import com.dducwsjvbe.web_ui.dto.LoginResponse;
import com.dducwsjvbe.web_ui.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;

@Controller
@RequiredArgsConstructor
@Slf4j(topic = "Home-Controller")
public class HomeController {
    private final AuthService authService;
    @Value("${keycloak.url}")
    private String KeycloakUrl;


    @GetMapping()
    public String home(Model model) {
        model.addAttribute("KeycloakUrl", KeycloakUrl);
        return "login1"; // ứng với templates/index.html
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequestDto request
    ) {
        log.info("username={};password={}", request.getUsername(), request.getPassword());
        LoginResponse response = authService.login(request);
        log.info("Username : {}", response.getUser().getUsername());

        log.info("Roles : {}", response.getUser().getRoles());

        log.info("Access Token : {}", response.getToken().getAccessToken());

        log.info("Refresh Token : {}", response.getToken().getRefreshToken());
        ResponseCookie accessCookie = ResponseCookie.from(
                        "access_token",
                        response.getToken().getAccessToken())
                .httpOnly(true)
                .secure(false)          // true khi deploy HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(response.getToken().getExpiresIn()))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refresh_token",
                        response.getToken().getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(response.getToken().getRefreshExpiresIn()))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response.getUser())
                ;
    }
}
