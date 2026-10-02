package com.dducwsjvbe.fishfeeder.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*")
public class DeviceController {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String VALID_API_KEY = "fish-secret-123";

    // Biến lưu trạng thái trong bộ nhớ
    private String pendingAction = "none"; // Lệnh chờ gửi tới ESP32: "open", "close", "none"
    private Double lastTemperature = null;
    private Boolean isDoorOpen = false;

    /**
     * 1. ESP32 POST lên đây mỗi 5s để gửi nhiệt độ & nhận lệnh điều khiển Servo
     */
    @PostMapping("/temperature")
    public ResponseEntity<Map<String, Object>> handleEsp32Ping(
            @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey,
            @RequestBody Map<String, Object> payload) {

        if (!VALID_API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Cập nhật dữ liệu gửi từ ESP32
        if (payload.containsKey("temperature") && payload.get("temperature") != null) {
            this.lastTemperature = Double.parseDouble(payload.get("temperature").toString());
        }
        if (payload.containsKey("is_open")) {
            this.isDoorOpen = (Boolean) payload.get("is_open");
        }

        // Trả về lệnh điều khiển cho ESP32
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("action", pendingAction);

        // Reset lại lệnh chờ sau khi đã phát đi
        this.pendingAction = "none";

        return ResponseEntity.ok(response);
    }

    /**
     * 2. Web POST vào đây khi người dùng nhấn "MỞ CỬA" hoặc "ĐÓNG NGAY"
     */
    @PostMapping("/control-servo")
    public ResponseEntity<Map<String, Object>> controlServo(
            @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey,
            @RequestBody Map<String, String> request) {

        if (!VALID_API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String action = request.get("action");
        if ("open".equalsIgnoreCase(action) || "close".equalsIgnoreCase(action)) {
            this.pendingAction = action.toLowerCase();
            return ResponseEntity.ok(Map.of("message", "Đã nhận lệnh: " + this.pendingAction));
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Action không hợp lệ"));
    }

    /**
     * 3. Web GET vào đây mỗi 5s để hiển thị nhiệt độ và trạng thái cửa
     */
    @GetMapping("/temperature")
    public ResponseEntity<Map<String, Object>> getTemperature(
            @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey) {

        if (!VALID_API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("temperature", lastTemperature);
        response.put("is_open", isDoorOpen);
        return ResponseEntity.ok(response);
    }
}