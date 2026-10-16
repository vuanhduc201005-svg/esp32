#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <HTTPClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <ArduinoJson.h>
#include <ESP32Servo.h>
#include <SPI.h>      // [RFID] THEM MOI
#include <MFRC522.h>  // [RFID] THEM MOI (thu vien "MFRC522" cua GithubCommunity)

// ==================== CẤU HÌNH ====================
const char* WIFI_SSID = "Galaxy A15 78E1";
const char* WIFI_PASS = "88888888";

const char* SERVER_URL = "https://interstate-forward-surname-homes.trycloudflare.com/api/devices/temperature";
const char* API_KEY = "fish-secret-123";

// ==================== GPIO PIN ====================
const int ONE_WIRE_PIN = 4;
const int SERVO_LEFT_PIN  = 18;
const int SERVO_RIGHT_PIN = 19;

// ==================== [RFID] CHAN RC522 - THEM MOI ====================
// Khong dung SPI mac dinh (18/19/23) vi 18, 19 dang gan cho servo -> dung SPI tuy chinh
const int RFID_SCK_PIN  = 14;
const int RFID_MISO_PIN = 25;
const int RFID_MOSI_PIN = 13;
const int RFID_SS_PIN   = 5;   // SDA / SS
const int RFID_RST_PIN  = 27;

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

// ==================== [RFID] BIEN RFID - THEM MOI ====================
MFRC522 mfrc522(RFID_SS_PIN, RFID_RST_PIN);
unsigned long lastCardTime = 0;
const unsigned long CARD_COOLDOWN = 1500; // chong quet lap lien tuc

// Danh sach the duoc phep (UID 4 byte). Quet the 1 lan, xem UID tren Serial Monitor roi dien vao day.
const byte AUTHORIZED_UIDS[][4] = {
    {0x11, 0xDA, 0x42, 0x02},
    {0xC6, 0x8F, 0xB2, 0xE3},
    {0xC0, 0x9C, 0xB6, 0x5C}
};
const int AUTHORIZED_COUNT = sizeof(AUTHORIZED_UIDS) / sizeof(AUTHORIZED_UIDS[0]);

// ==================== HÀM ĐIỀU KHIỂN SERVO FIX LỖI ====================
void openDoors() {
    Serial.println("-> [SERVO] Dang MO cua (90 do)...");
    if (!servoLeft.attached())  servoLeft.attach(SERVO_LEFT_PIN, 500, 2400);
    if (!servoRight.attached()) servoRight.attach(SERVO_RIGHT_PIN, 500, 2400);
    // Gửi góc 90 độ
    servoLeft.write(90);
    servoRight.write(90);

    isOpen = true;
    openStartTime = millis(); // Reset lại đếm giờ 10s
}

void closeDoors() {
    Serial.println("-> [SERVO] Dang DONG cua (0 do)...");
    if (!servoLeft.attached())  servoLeft.attach(SERVO_LEFT_PIN, 500, 2400);
    if (!servoRight.attached()) servoRight.attach(SERVO_RIGHT_PIN, 500, 2400);
    // Ép Servo quay về góc 0 độ
    servoLeft.write(0);
    servoRight.write(0);

    isOpen = false;
}

// ==================== HÀM KẾT NỐI WIFI ĐÃ FIX ====================
void connectWiFi() {
    Serial.println("\n--- DANG KET NOI WIFI ---");
    WiFi.disconnect(true); // Xoá session Wi-Fi cũ bị kẹt
    delay(500);

    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASS);

    int timeout = 0;
    // Cho phép chờ tối đa 30 lần x 500ms = 15 giây
    while (WiFi.status() != WL_CONNECTED && timeout < 30) {
        delay(500);
        Serial.print(".");
        timeout++;
    }

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\n[OK] WiFi Connected! IP: " + WiFi.localIP().toString());
    } else {
        Serial.println("\n[LOI] Khong the ket noi Wi-Fi!");
        Serial.print("Ma trang thai WiFi Status: ");
        Serial.println(WiFi.status());
        // Ma loi: 1 = Khong tim thay SSID, 4 = Sai MPT/Loi ket noi, 6 = Mat ket noi
    }
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

// ==================== [RFID] HAM RFID - THEM MOI ====================
bool isAuthorizedCard() {
    if (mfrc522.uid.size != 4) return false;
    for (int i = 0; i < AUTHORIZED_COUNT; i++) {
        bool match = true;
        for (byte j = 0; j < 4; j++) {
            if (mfrc522.uid.uidByte[j] != AUTHORIZED_UIDS[i][j]) {
                match = false;
                break;
            }
        }
        if (match) return true;
    }
    return false;
}

void checkRFID() {
    if (millis() - lastCardTime < CARD_COOLDOWN) return;
    if (!mfrc522.PICC_IsNewCardPresent()) return;
    if (!mfrc522.PICC_ReadCardSerial()) return;

    lastCardTime = millis();

    Serial.print("-> [RFID] UID: ");
    for (byte i = 0; i < mfrc522.uid.size; i++) {
        if (mfrc522.uid.uidByte[i] < 0x10) Serial.print("0");
        Serial.print(mfrc522.uid.uidByte[i], HEX);
        Serial.print(" ");
    }
    Serial.println();

    if (isAuthorizedCard()) {
        Serial.println("-> [RFID] The hop le -> MO cua");
        openDoors(); // tu dong dong lai sau 10s nhu cu
    } else {
        Serial.println("-> [RFID] The KHONG hop le!");
    }

    mfrc522.PICC_HaltA();
    mfrc522.PCD_StopCrypto1();
}

void setup() {
    Serial.begin(115200);
    sensors.begin();

    // CẤU HÌNH XUNG PWM CHUẨN CHO ESP32 SERVO (500us - 2400us)
    servoLeft.setPeriodHertz(50);
    servoRight.setPeriodHertz(50);

    servoLeft.attach(SERVO_LEFT_PIN, 500, 2400);
    servoRight.attach(SERVO_RIGHT_PIN, 500, 2400);

    // Mặc định đóng cửa khi vừa bật nguồn
    closeDoors();

    connectWiFi();

    // [RFID] THEM MOI: khoi tao RC522
    SPI.begin(RFID_SCK_PIN, RFID_MISO_PIN, RFID_MOSI_PIN, RFID_SS_PIN);
    mfrc522.PCD_Init();
    mfrc522.PCD_DumpVersionToSerial();   // <-- thêm dòng này
    Serial.println("[RFID] RC522 san sang, hay quet the...");
}

void loop() {
    if (WiFi.status() != WL_CONNECTED) {
        // Nếu mất Wi-Fi, thử kết nối lại sau mỗi chu kỳ
        static unsigned long lastReconnect = 0;
        if (millis() - lastReconnect >= 10000) {
            lastReconnect = millis();
            connectWiFi();
        }
    } else {
        // 2. GỬI PING VỀ SERVER (3 GIÂY / LẦN) KHI CÓ WIFI
        if (millis() - lastSend >= SEND_INTERVAL) {
            lastSend = millis();
            sendTemperatureAndCheckCommand();
        }
    }

    // 1. TỰ ĐỘNG ĐÓNG CỬA SAU 10 GIÂY (Chạy độc lập không phụ thuộc WiFi)
    if (isOpen && (millis() - openStartTime >= AUTO_CLOSE_TIMEOUT)) {
        Serial.println("-> [TIMEOUT 10s] Tu dong kich hoat DONG CUA!");
        closeDoors();
    }

    // [RFID] THEM MOI: quet the (chay doc lap, khong can WiFi / Web / Java)
    checkRFID();
}