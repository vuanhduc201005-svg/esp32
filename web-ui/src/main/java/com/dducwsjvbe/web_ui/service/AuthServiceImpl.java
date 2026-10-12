package com.dducwsjvbe.web_ui.service;

import com.dducwsjvbe.common_service.advice.custom.RefreshTokenExpiredException;
import com.dducwsjvbe.web_ui.dto.LoginRequestDto;
import com.dducwsjvbe.web_ui.dto.LoginResponse;
import com.dducwsjvbe.web_ui.dto.TokenExchangeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RestClient restClient;

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${GatewayPort.url}")
    private String gatewayPort;
    @Override
    public LoginResponse login(LoginRequestDto request) {

        return restClient.post()
                .uri(gatewayPort+"/api/v1/auths/login")
                .body(request)
                .retrieve()
                .body(LoginResponse.class);

    }
    @Override
    public TokenExchangeResponse refreshToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);

        try {
            return restClient.post()
                    .uri(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenExchangeResponse.class);
        } catch (HttpClientErrorException e) {
            throw new RefreshTokenExpiredException("Refresh token invalid or expired");
        }
    }
}
