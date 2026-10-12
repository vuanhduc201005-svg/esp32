package com.dducwsjvbe.user_service.controller;

import com.dducwsjvbe.user_service.dto.request.LoginRequestDto;
import com.dducwsjvbe.user_service.dto.response.LoginResponse;
import com.dducwsjvbe.user_service.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auths")
@RequiredArgsConstructor
@Slf4j(topic = "User-Service/Auth-Controller")
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequestDto request) {
        log.info("Login Request: {}", request.getUsername());
        return ResponseEntity.ok(userService.login(request));
    }

}
