package com.dducwsjvbe.web_ui.config;

import com.dducwsjvbe.web_ui.filter.TokenRefreshFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final TokenRefreshFilter tokenRefreshFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register","/actuator/**").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,"/api/devices/temperature"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,"/api/devices/temperature"
                        ).permitAll()
                        .anyRequest().permitAll() // vì việc chặn đã do TokenRefreshFilter đảm nhiệm
                )
                .addFilterBefore(tokenRefreshFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
