package com.dducwsjvbe.web_ui.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
@Component
public class JwtUtils {
    public boolean isExpiringSoon(String jwt, long thresholdSeconds) {
        try {
            String[] parts = jwt.split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(payloadJson);
            long exp = node.get("exp").asLong();
            long now = Instant.now().getEpochSecond();
            return (exp - now) < thresholdSeconds;
        } catch (Exception e) {
            return true; // parse lỗi -> coi như hết hạn, ép refresh cho an toàn
        }
    }
}
/*
nếu fail tức token còn hạn
nếu true thì 1 là hết hạn or token không đáng tin cậy do có thể là ko có exo chả hạn
 */