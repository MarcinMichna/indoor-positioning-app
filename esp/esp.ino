#include <WiFi.h>
#include <WiFiClient.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEScan.h>
#include <BLEServer.h>
#include <BLEAdvertisedDevice.h>
#include <HTTPClient.h>

// Bluetooth server
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-000000000001"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

// Access Point
const char *apSsid = "ESP_1";
const char *apPassword= "polska123";

// Wifi connection
const char *wifiSsid = "Marcin_Krul";
const char *wifiPassword= "M@rsik353";

// Bluetooth
int bleScanTime = 5; // in seconds
BLEScan* bleScanner;


// REST
String serverName = "michnam.pl:5000/add";
String json = "";

class BleScanResult: public BLEAdvertisedDeviceCallbacks {
    void onResult(BLEAdvertisedDevice advertisedDevice) {
      
      String addr = advertisedDevice.getAddress().toString().c_str();
      int rssi = advertisedDevice.getRSSI();

      json += String("{ ");
      json += String("'addr': ") + String("'") + String(addr) + String("', ");
      json += String("'rssi': ") + String(rssi) + String(", ");
      json += String("'esp': ") + String("'") + String(apSsid) + String("' ");
      json += String(" }, ");
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
  delay(1000);
  wifiScan();
  bleScan();
  testRequest();
}

void apSetup() {
  Serial.println(".");
  Serial.println("Setting up Wifi Access Point");
  WiFi.softAP(apSsid, apPassword);
  IPAddress apIP = WiFi.softAPIP();
  Serial.print("Wifi AP ssid: ");
  Serial.print(apSsid);
  Serial.print(", IP: ");
  Serial.println(apIP);
  Serial.println(".");
}

void stationSetup() {
  WiFi.begin(wifiSsid,wifiPassword,7,0,5);

  while(WiFi.status() != WL_CONNECTED){
    delay(100);
  }
  Serial.print("Wifi client IP: ");
  Serial.println(WiFi.localIP()); 

  HTTPClient http;
  String host = "michnam.pl:5000";
  http.begin(host.c_str());
  int httpResponseCode = http.GET();
  Serial.print("Test http request code: ");
  Serial.println(httpResponseCode);
  Serial.println(".");
  http.end();
  delay(1000);
}

void bluetoothClientSetup() {
  bleScanner = BLEDevice::getScan(); //create new scan
  bleScanner -> setAdvertisedDeviceCallbacks(new BleScanResult());
  bleScanner -> setActiveScan(true); //active scan uses more power, but get results faster
  bleScanner -> setInterval(100);
  bleScanner -> setWindow(99);  // less or equal setInterval value
  Serial.println(".\nBluetooth client setup successful\n.");
}

void bluetoothServerSetup() {
  BLEDevice::init("");
  BLEServer *pServer = BLEDevice::createServer();
  BLEService *pService = pServer->createService(SERVICE_UUID);
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_WRITE
                                       );

  pCharacteristic->setValue("Hello World");
  pService->start();
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  BLEDevice::startAdvertising();
  Serial.println(".");
}

void wifiScan() {
  int n = WiFi.scanNetworks();
  Serial.print("Wifi devices found: ");
  Serial.println(n);
  json = "{ 'wifi': [ ";
  if (n != 0) {
      for (int i = 0; i < n; ++i) {
          json += String("{ ");
          json += String("'ssid': ") + String("'") + String(WiFi.SSID(i)) + String("', ");
          json += String("'rssi': ") + String(WiFi.RSSI(i)) + String(", ");
          json += String("'esp': ") + String("'") + String(apSsid) + String("' ");
          json += String("},");
          }
  }
  json = json.substring(0, json.length()-1);
  json += String("], ");
}

void bleScan() {
  json += "'ble': [ ";
  BLEScanResults foundDevices = bleScanner->start(bleScanTime, false);
  Serial.print("BLE devices found: ");
  Serial.println(foundDevices.getCount());
  bleScanner->clearResults(); 
  json = json.substring(0, json.length()-1);
  json += String("] } ");
}

void testRequest() {
  HTTPClient http;
  http.begin(serverName);
  http.addHeader("Content-Type", "application/json");

  Serial.print("Sending json: ");
  int httpResponseCode = http.POST(json);
  
  Serial.println(json);
  Serial.print("Sending to: ");
  Serial.println(serverName);
  Serial.print("HTTP Response code: ");
  Serial.println(httpResponseCode);
  http.end();
  Serial.println(".");
}
