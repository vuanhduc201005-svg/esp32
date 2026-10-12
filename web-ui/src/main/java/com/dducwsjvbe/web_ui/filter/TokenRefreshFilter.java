package com.dducwsjvbe.web_ui.filter;

import com.dducwsjvbe.common_service.advice.custom.RefreshTokenExpiredException;
import com.dducwsjvbe.web_ui.dto.TokenExchangeResponse;
import com.dducwsjvbe.web_ui.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "Token-Refresh-Filter")
public class TokenRefreshFilter extends OncePerRequestFilter {
    private final AuthService authService;
    private final JwtUtils jwtUtils;

    // đảm bảo chỉ 1 luồng refresh cho cùng 1 refresh_token tại 1 thời điểm
    private final ConcurrentHashMap<String, Object> refreshLocks = new ConcurrentHashMap<>();

    private static final String ACCESS_COOKIE = "access_token";
    private static final String REFRESH_COOKIE = "refresh_token";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // các path public (login, register...) thì bỏ qua
        if (isWhiteListed(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = extractCookie(request, ACCESS_COOKIE);
        String refreshToken = extractCookie(request, REFRESH_COOKIE);

        try {
            if (accessToken == null) {
                if (refreshToken == null) {
                    unauthorized(response, "Chưa đăng nhập");
                    return;
                }
                // không còn access token nhưng còn refresh token -> refresh luôn
                accessToken = doRefresh(refreshToken, response);
            } else if (jwtUtils.isExpiringSoon(accessToken, 30)) {
                // sắp hết hạn (còn dưới 30s) -> chủ động refresh trước khi dùng
                if (refreshToken == null) {
                    unauthorized(response, "Phiên đăng nhập hết hạn");
                    return;
                }
                accessToken = doRefresh(refreshToken, response);
            }

            TokenContext.setAccessToken(accessToken);
            filterChain.doFilter(request, response);

        } catch (RefreshTokenExpiredException e) {
            clearCookies(response);
            unauthorized(response, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại");
        } finally {
            TokenContext.clear(); // luôn dọn ThreadLocal sau mỗi request
        }
    }

    private String doRefresh(String refreshToken, HttpServletResponse response) {
        // đồng bộ theo refreshToken để tránh nhiều request cùng lúc refresh trùng nhau
        Object lock = refreshLocks.computeIfAbsent(refreshToken, k -> new Object());
        synchronized (lock) {
            try {
                log.info("Refreshing access token...");
                TokenExchangeResponse newToken = authService.refreshToken(refreshToken);
                setCookies(response, newToken);
                return newToken.getAccessToken();
            } finally {
                refreshLocks.remove(refreshToken);
            }
        }
    }

    private void setCookies(HttpServletResponse response, TokenExchangeResponse token) {
        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_COOKIE, token.getAccessToken())
                .httpOnly(true).secure(false).sameSite("Lax").path("/")
                .maxAge(Duration.ofSeconds(token.getExpiresIn()))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE, token.getRefreshToken())
                .httpOnly(true).secure(false).sameSite("Lax").path("/")
                .maxAge(Duration.ofSeconds(token.getRefreshExpiresIn()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private void clearCookies(HttpServletResponse response) {
        ResponseCookie clearAccess = ResponseCookie.from(ACCESS_COOKIE, "").path("/").maxAge(0).build();
        ResponseCookie clearRefresh = ResponseCookie.from(REFRESH_COOKIE, "").path("/").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private boolean isWhiteListed(String uri) {
        return uri.startsWith("/login") || uri.equals("/") || uri.startsWith("/register");
    }
}
/*
trc khi gọi api thì check token trc và nếu còn hạn ms call api còn nếu hết hạn pk refresh ms call api nên ko cần goọi lại sau refresh
 */