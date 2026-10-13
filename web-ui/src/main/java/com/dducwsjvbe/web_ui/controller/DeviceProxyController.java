package com.dducwsjvbe.web_ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Proxy: Browser -> web-ui (cookie) -> API Gateway (Bearer) -> fishfeeder.
 *
 * - Bearer token được RestClient interceptor (RestClientConfig) tự gắn từ TokenContext,
 *   TokenContext do TokenRefreshFilter set từ cookie access_token (có refresh nếu sắp hết hạn).
 * - X-API-KEY được thêm ở server nên không lộ ra HTML/JS.
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j(topic = "Device-Proxy-Controller")
public class DeviceProxyController {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final RestClient restClient;

    @Value("${GatewayPort.url}")
    private String gatewayUrl;

    @Value("${fishfeeder.api-key}")
    private String apiKey;

    /** Chỉ lấy status + body, KHÔNG chép header của gateway (tránh trùng Transfer-Encoding: chunked -> 502). */
    private ResponseEntity<String> clean(ResponseEntity<String> res) {
        return ResponseEntity.status(res.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(res.getBody());
    }

    @PostMapping("/control-servo")
    public ResponseEntity<String> controlServo(@RequestBody Map<String, String> body) {
        try {
            ResponseEntity<String> res = restClient.post()
                    .uri(gatewayUrl + "/api/devices/control-servo")
                    .header(API_KEY_HEADER, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);
            return clean(res);
        } catch (HttpStatusCodeException e) {
            // giữ nguyên status từ gateway (401/403/400/429...) để JS xử lý
            log.warn("control-servo failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("Cannot reach gateway", e);
            return ResponseEntity.status(502).body("{\"error\":\"Không kết nối được API Gateway\"}");
        }
    }

    @GetMapping("/temperature")
    public ResponseEntity<String> temperature() {
        try {
            ResponseEntity<String> res = restClient.method(HttpMethod.GET)
                    .uri(gatewayUrl + "/api/devices/temperature")
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .toEntity(String.class);
            return clean(res);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("Cannot reach gateway", e);
            return ResponseEntity.status(502).body("{\"error\":\"Không kết nối được API Gateway\"}");
        }
    }
}