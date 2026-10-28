package com.dducwsjvbe.fishfeeder.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

    // [THÊM MỚI] Tổng số chỗ của bãi xe (đổi số này theo ý bạn)
    private static final int TOTAL_SLOTS = 50;

    // [THÊM MỚI] Số chỗ trống hiện tại, dùng chung cho mọi request.
    // Dùng AtomicInteger để 2 người bấm cùng lúc không bị sai số.
    private final AtomicInteger availableSlots = new AtomicInteger(TOTAL_SLOTS);


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
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @PostMapping("/control-servo")
    public ResponseEntity<Map<String, Object>> controlServo(
            @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey,
            @RequestBody Map<String, String> request) {

        if (!VALID_API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String action = request.get("action");

// [THÊM MỚI] MỞ CỬA VÀO: trừ 1 chỗ trống
        if ("open_in".equalsIgnoreCase(action)) {
            int current;
            do {
                current = availableSlots.get();
                if (current <= 0) {
                    // Hết chỗ thì không mở cửa, không gửi lệnh cho ESP32
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                            "error", "Bãi xe đã hết chỗ trống",
                            "available_slots", current));
                }
            } while (!availableSlots.compareAndSet(current, current - 1));

            this.pendingAction = "open"; // ESP32 vẫn chỉ hiểu "open"
            return ResponseEntity.ok(Map.of(
                    "message", "Đã nhận lệnh: mở cửa vào",
                    "available_slots", availableSlots.get()));
        }

        // [THÊM MỚI] MỞ CỬA RA: cộng 1 chỗ trống
        if ("open_out".equalsIgnoreCase(action)) {
            int current;
            do {
                current = availableSlots.get();
                if (current >= TOTAL_SLOTS) {
                    // Bãi đang trống hoàn toàn thì không thể có xe ra
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                            "error", "Bãi xe đang trống hoàn toàn, không có xe để ra",
                            "available_slots", current));
                }
            } while (!availableSlots.compareAndSet(current, current + 1));

            this.pendingAction = "open";
            return ResponseEntity.ok(Map.of(
                    "message", "Đã nhận lệnh: mở cửa ra",
                    "available_slots", availableSlots.get()));
        }

        // GIỮ NGUYÊN: đóng cửa ngay (không ảnh hưởng số chỗ)
        if ("close".equalsIgnoreCase(action)) {
            this.pendingAction = "close";
            return ResponseEntity.ok(Map.of(
                    "message", "Đã nhận lệnh: close",
                    "available_slots", availableSlots.get())); // [THÊM MỚI] trả kèm số chỗ
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
        response.put("available_slots", availableSlots.get()); // [THÊM MỚI]
        response.put("total_slots", TOTAL_SLOTS);              // [THÊM MỚI]
        return ResponseEntity.ok(response);
    }
}