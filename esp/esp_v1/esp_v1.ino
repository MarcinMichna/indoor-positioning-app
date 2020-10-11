#include <WiFi.h>
#include <WiFiClient.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEScan.h>
#include <BLEServer.h>
#include <BLEAdvertisedDevice.h>

// Bluetooth server
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

// Access Point
const char *apSsid = "ESP_1";
const char *apPassword= "polska123";

// Wifi connection
const char *wifiSsid = "Marcin <3";
const char *wifiPassword= "polska123";

// Bluetooth
int bleScanTime = 5; // in seconds
BLEScan* bleScanner;

class BleScanResult: public BLEAdvertisedDeviceCallbacks {
    void onResult(BLEAdvertisedDevice advertisedDevice) {
      Serial.printf("Advertised Device: %s \n", advertisedDevice.toString().c_str());
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
  if (n != 0) {
      for (int i = 0; i < n; ++i) {
          // Print SSID and RSSI for each network found
          Serial.print(i + 1);
          Serial.print(": ");
          Serial.print(WiFi.SSID(i));
          Serial.print(" ");
          Serial.println(WiFi.RSSI(i));
      }
  }
  Serial.println(".");
  delay(1000);
}

void bleScan() {
  BLEScanResults foundDevices = bleScanner->start(bleScanTime, false);
  Serial.print("Devices found: ");
  Serial.println(foundDevices.getCount());
  Serial.println("Scan done!");
  bleScanner->clearResults();   // delete results fromBLEScan buffer to release memory
  delay(2000);
}
