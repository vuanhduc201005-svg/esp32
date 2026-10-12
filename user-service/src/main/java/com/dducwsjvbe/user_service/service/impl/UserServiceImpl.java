package com.dducwsjvbe.user_service.service.impl;

import com.dducwsjvbe.user_service.config.JwtRoleUtils;
import com.dducwsjvbe.user_service.dto.identity.*;
import com.dducwsjvbe.user_service.dto.request.LoginRequestDto;
import com.dducwsjvbe.user_service.dto.response.LoginResponse;
import com.dducwsjvbe.user_service.repository.IdentityClient;
import com.dducwsjvbe.user_service.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j(topic = "User-Service/User-Service-Impl")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final IdentityClient identityClient;
    private final JwtDecoder jwtDecoder;

    @Value("${idp.client-id}")
    @NonFinal
    private String clientId;

    @Value("${idp.client-secret}")
    @NonFinal
    private String clientSecret;



    @Override
    public LoginResponse login(LoginRequestDto request) {
        log.info("Login Request: {}", request.getUsername());
        TokenExchangeResponse token = identityClient.exchangeUserToken(UserTokenExchangeParam.builder()
                .grant_type("password")
                .client_secret(clientSecret)
                .client_id(clientId)
                .scope("openid")
                .username(request.getUsername())
                .password(request.getPassword())
                .build());
        Jwt jwt = jwtDecoder.decode(token.getAccessToken());

        UserInfo userInfo = UserInfo.builder()
                .username(jwt.getClaimAsString("preferred_username"))
                .roles(JwtRoleUtils.extractRoles(jwt))
                .build();
        return LoginResponse.builder()
                .token(token)
                .user(userInfo)
                .build();
    }

}
