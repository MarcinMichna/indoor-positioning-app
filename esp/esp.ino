#include <WiFi.h>
#include <WiFiClient.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEScan.h>
#include <BLEServer.h>
#include <BLEAdvertisedDevice.h>
#include <HTTPClient.h>

int bleScanTime = 2; // in seconds


const char *espName = "ESP_4";

// pattern: ESP_<number>_WIFI
const char *espNameWifi = "ESP_4_WIFI";

// pattern: ESP_<number>_BT
const char *espNameBt = "ESP_4_BT";

String serverName = "http://michnam.pl:5000/add";


// Access Point
const char *apPassword= "polska123";

// Wifi connection
const char *wifiSsid = "Marcin_Krul";
const char *wifiPassword= "M@rsik353";

// Bluetooth
BLEScan* bleScanner;


// REST

String json = "";

class BleScanResult: public BLEAdvertisedDeviceCallbacks {
    void onResult(BLEAdvertisedDevice advertisedDevice) {

      String bleName = advertisedDevice.getName().c_str();
      String addr = advertisedDevice.getAddress().toString().c_str();
      int rssi = advertisedDevice.getRSSI();

      json += String("{ ");
      json += String("\"name\": ") + String("\"") + String(bleName) + String("\", ");
      json += String("\"addr\": ") + String("\"") + String(addr) + String("\", ");
      json += String("\"rssi\": ") + String(rssi) + String(", ");
      json += String("\"esp\": ") + String("\"") + String(espName) + String("\" ");
      json += String(" },");
    }
};



void setup() {
  Serial.begin(115200);
  apSetup();
  stationSetup();
  bluetoothServerSetup();
  bluetoothClientSetup();
}

void loop() {
  delay(100);
  wifiScan();
  bleScan();
  sendHttpRequest();
}

void apSetup() {
  Serial.println("Setting up Wifi Access Point");
  WiFi.softAP(espNameWifi, apPassword);
  IPAddress apIP = WiFi.softAPIP();
  Serial.print("Wifi AP ssid: ");
  Serial.print(espNameWifi);
  Serial.print(", IP: ");
  Serial.println(apIP);
}

void stationSetup() {
  WiFi.begin(wifiSsid,wifiPassword,7,0,5);

  while(WiFi.status() != WL_CONNECTED){
    delay(100);
  }
  Serial.print("Wifi client IP: ");
  Serial.println(WiFi.localIP()); 
}

void bluetoothClientSetup() {
  bleScanner = BLEDevice::getScan(); //create new scan
  bleScanner -> setAdvertisedDeviceCallbacks(new BleScanResult());
  bleScanner -> setActiveScan(true); //active scan uses more power, but get results faster
  bleScanner -> setInterval(100);
  bleScanner -> setWindow(99);  // less or equal setInterval value
  Serial.println("Ble scan setup successful\n.");
}

void bluetoothServerSetup() {
  BLEDevice::init(espNameBt);
  BLEServer *pServer = BLEDevice::createServer();
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->setScanResponse(true);
  BLEDevice::startAdvertising();
  Serial.println("Ble server setup successful\n.");
}

void wifiScan() {
  int n = WiFi.scanNetworks();
  Serial.print("Wifi devices found: ");
  Serial.println(n);
  json = "{ \"wifi\": [ ";
  if (n != 0) {
      for (int i = 0; i < n; ++i) {
          json += String("{ ");
          json += String("\"ssid\": ") + String("\"") + String(WiFi.SSID(i)) + String("\", ");
          json += String("\"rssi\": ") + String(WiFi.RSSI(i)) + String(", ");
          json += String("\"esp\": ") + String("\"") + String(espName) + String("\"");
          json += String("},");
          }
  }
  json = json.substring(0, json.length()-1);
  json += String("], ");
}

void bleScan() {
  json += "\"ble\": [ ";
  BLEScanResults foundDevices = bleScanner->start(bleScanTime, false);
  Serial.print("BLE devices found: ");
  Serial.println(foundDevices.getCount());
  bleScanner->clearResults(); 
  json = json.substring(0, json.length()-1);
  json += String("] } ");
}

void sendHttpRequest() {
  HTTPClient http;
  http.begin(serverName);
  http.addHeader("Content-Type", "application/json");

  Serial.print("Sending json: ");
  Serial.println(json);

  int httpResponseCode = http.POST(json);
  Serial.print("HTTP Response code: ");
  Serial.println(httpResponseCode);
  http.end();
  
  Serial.println(".");
}
