package com.dducwsjvbe.user_service.config;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

public class JwtRoleUtils {
    private JwtRoleUtils() {}

    @SuppressWarnings("unchecked")
    public static List<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return List.of();
        }
        return (List<String>) realmAccess.getOrDefault("roles", List.of());
    }
}
