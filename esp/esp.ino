#include <WiFi.h>
#include <WiFiClient.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEScan.h>
#include <BLEServer.h>
#include <BLEAdvertisedDevice.h>
#include <Arduino_JSON.h>
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
const char* serverName = "michnam.pl:5000/check";
String json = "";

class BleScanResult: public BLEAdvertisedDeviceCallbacks {
    void onResult(BLEAdvertisedDevice advertisedDevice) {
      Serial.printf("Advertised Device: %s \n", advertisedDevice.toString().c_str());
      String addr = advertisedDevice.getAddress().toString().c_str();
      Serial.println(addr);
      int rssi = advertisedDevice.getRSSI();
      Serial.println(rssi);

      json += String("{ ");
      json += String("'addr': ") + String("'") + String(addr) + String("', ");
      json += String("'rssi': ") + String(rssi) + String(", ");
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
  Serial.print("My AP ssid: ");
  Serial.print(apSsid);
  Serial.print(", IP: ");
  Serial.println(apIP);
  Serial.println(".");
}

void stationSetup() {
  WiFi.begin(wifiSsid,wifiPassword,7,0,5);

  while(WiFi.status() != WL_CONNECTED){
    Serial.println(".");
    delay(500);
  }
  Serial.print("IP address station: ");
  Serial.println(WiFi.localIP()); 
  Serial.println(".");
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
  Serial.println(".");
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
  Serial.println("Characteristic defined! Now you can read it in your phone!");
  Serial.println(".");
}

void wifiScan() {
  Serial.println("scan start");
  int n = WiFi.scanNetworks();
  Serial.println("scan done");
  json = "{ 'wifi': [ ";
  if (n != 0) {
      for (int i = 0; i < n; ++i) {
          // Print SSID and RSSI for each network found
          Serial.print(i + 1);
          Serial.print(": ");
          Serial.print(WiFi.SSID(i));
          Serial.print(" ");
          Serial.println(WiFi.RSSI(i));

          json += String("{ ");
          json += String("'ssid': ") + String("'") + String(WiFi.SSID(i)) + String("', ");
          json += String("'rssi': ") + String(WiFi.RSSI(i)) + String(", ");
          json += String("}, ");
          }
  }
  json = json.substring(0, json.length()-1);
  json += String("], ");
  Serial.println(".");
  delay(1000);
}

void bleScan() {
  json += "'ble': [ ";
  BLEScanResults foundDevices = bleScanner->start(bleScanTime, false);
  Serial.print("Devices found: ");
  Serial.println(foundDevices.getCount());
  Serial.println("Scan done!");
  bleScanner->clearResults(); 
  json = json.substring(0, json.length()-1);
  json += String("] } ");
  delay(2000);
}

void testRequest() {
  HTTPClient http;
  http.begin("http://michnam.pl:5000/check");
  http.addHeader("Content-Type", "application/json");
  int httpResponseCode = http.POST(json);
  Serial.print("HTTP Response code: ");
  Serial.println(httpResponseCode);
  Serial.println(json);
  http.end();
  json = "{ 'data': [ "; //reset json
}
