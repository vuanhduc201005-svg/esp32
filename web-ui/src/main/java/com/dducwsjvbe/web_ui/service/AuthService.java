package com.dducwsjvbe.web_ui.service;

import com.dducwsjvbe.web_ui.dto.LoginRequestDto;
import com.dducwsjvbe.web_ui.dto.LoginResponse;
import com.dducwsjvbe.web_ui.dto.TokenExchangeResponse;

public interface AuthService {
    LoginResponse login(LoginRequestDto request);
    TokenExchangeResponse refreshToken(String refreshToken);
}
