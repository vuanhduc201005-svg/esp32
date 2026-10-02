#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <HTTPClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <ArduinoJson.h>
#include <ESP32Servo.h>

// ==================== CẤU HÌNH ====================
const char* WIFI_SSID = "Galaxy A15 78E1";
const char* WIFI_PASS = "88888888";

const char* SERVER_URL = "https://candles-findlaw-spies-reaction.trycloudflare.com/api/devices/temperature";
const char* API_KEY = "fish-secret-123";

// ==================== GPIO PIN ====================
const int ONE_WIRE_PIN = 4;
const int SERVO_LEFT_PIN  = 18;
const int SERVO_RIGHT_PIN = 19;

// ==================== OBJECTS & VARS ====================
OneWire oneWire(ONE_WIRE_PIN);
DallasTemperature sensors(&oneWire);

Servo servoLeft;
Servo servoRight;

const unsigned long SEND_INTERVAL = 3000; // Giảm xuống 3s để phản hồi lệnh Web nhanh hơn
unsigned long lastSend = 0;

// Trạng thái Servo & Tự động đóng 10s
bool isOpen = false;
unsigned long openStartTime = 0;
const unsigned long AUTO_CLOSE_TIMEOUT = 10000; // 10 giây

// ==================== HÀM ĐIỀU KHIỂN SERVO FIX LỖI ====================
void openDoors() {
    Serial.println("-> [SERVO] Dang MO cua (90 do)...");

    // Gửi góc 90 độ
    servoLeft.write(90);
    servoRight.write(90);

    isOpen = true;
    openStartTime = millis(); // Reset lại đếm giờ 10s
}

void closeDoors() {
    Serial.println("-> [SERVO] Dang DONG cua (0 do)...");

    // Ép Servo quay về góc 0 độ
    servoLeft.write(0);
    servoRight.write(0);

    isOpen = false;
}

void connectWiFi() {
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASS);
    Serial.print("Connecting WiFi");
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println("\nWiFi Connected! IP: " + WiFi.localIP().toString());
}

bool readTemperature(float &out) {
    sensors.requestTemperatures();
    float t = sensors.getTempCByIndex(0);
    if (t == DEVICE_DISCONNECTED_C || t == 85.0) return false;
    out = t;
    return true;
}

void sendTemperatureAndCheckCommand() {
    float temperature;
    bool sensorOK = readTemperature(temperature);

    JsonDocument doc;
    if (sensorOK) {
        doc["temperature"] = round(temperature * 10) / 10.0;
        doc["status"] = "ok";
    } else {
        doc["temperature"] = nullptr;
        doc["status"] = "sensor_error";
    }
    doc["uptime"] = millis() / 1000;
    doc["is_open"] = isOpen;

    String body;
    serializeJson(doc, body);

    WiFiClientSecure *client = new WiFiClientSecure();
    if (!client) return;
    client->setInsecure();

    HTTPClient http;
    http.setTimeout(8000);

    if (http.begin(*client, SERVER_URL)) {
        http.addHeader("Content-Type", "application/json");
        http.addHeader("X-API-KEY", API_KEY);
        http.addHeader("Connection", "close");

        int code = http.POST(body);
        if (code > 0) {
            String response = http.getString();
            Serial.printf("HTTP %d | Res: %s\n", code, response.c_str());

            JsonDocument resDoc;
            DeserializationError err = deserializeJson(resDoc, response);
            if (!err) {
                String action = resDoc["action"] | "none";

                // Nhận lệnh từ Backend
                if (action == "open") {
                    openDoors();
                } else if (action == "close") {
                    closeDoors();
                }
            }
        }
        http.end();
    }
    client->stop();
    delete client;
}

void setup() {
    Serial.begin(115200);
    sensors.begin();

    // CẤU HÌNH XUNG PWM CHUẨN CHO ESP32 SERVO (500us - 2400us)
    // Giúp Servo nhận đúng dải góc 0 - 180 độ không bị trượt/kẹt
    servoLeft.setPeriodHertz(50);
    servoRight.setPeriodHertz(50);

    servoLeft.attach(SERVO_LEFT_PIN, 500, 2400);
    servoRight.attach(SERVO_RIGHT_PIN, 500, 2400);

    // Mặc định đóng cửa khi vừa bật nguồn
    closeDoors();

    connectWiFi();
}

void loop() {
    if (WiFi.status() != WL_CONNECTED) {
        connectWiFi();
    }

    // 1. TỰ ĐỘNG ĐÓNG CỬA SAU 10 GIÂY
    if (isOpen && (millis() - openStartTime >= AUTO_CLOSE_TIMEOUT)) {
        Serial.println("-> [TIMEOUT 10s] Tu dong kich hoat DONG CUA!");
        closeDoors();
    }

    // 2. GỬI PING VỀ SERVER (3 GIÂY / LẦN)
    if (millis() - lastSend >= SEND_INTERVAL) {
        lastSend = millis();
        sendTemperatureAndCheckCommand();
    }
}