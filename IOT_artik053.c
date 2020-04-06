
#include <WiFi.h>
#include <AWS_IOT.h>
#include <ArduinoJson.h>
#include<Wire.h>
#include <BluetoothSerial.h>

BluetoothSerial bt;
char ch;
String Lati = "";
String Long = "";
double la, lo;
int flag = 0;
bool push_one = false;
const int ledPin = 18;
const int MPU=0x68;  // I2C address of the MPU-6050
const int vibration_sensor = 4;
int16_t AcX,AcY,AcZ,Tmp,GyX,GyY,GyZ;
AWS_IOT hornbill; // AWS_IOT instance
char WIFI_SSID[]="okok";
char WIFI_PASSWORD[]= "12341234";
char HOST_ADDRESS[]="a1o3rc9x07id8y-ats.iot.ap-northeast-2.amazonaws.com";
char CLIENT_ID[]= "espDH11";
char TOPIC_NAME_update[]= "$aws/things/IOTApp/shadow/update";
char TOPIC_NAME_delta[]= "$aws/things/IOTApp/shadow/update/delta";
int status = WL_IDLE_STATUS;
int tick = 0, msgCount = 0, msgReceived = 0;
char payload[512];
char rcvdPayload[512];
int tilt_count = 0;
double latitude = 0, longitude = 0;
boolean emergency_state = false;
boolean tilt_state = false;
boolean siren_state = false;
unsigned long startDeb = millis();
int inService = 0;
boolean shocked = false;
boolean ESP32_emergency = false;

// subscribe ../update/delta
void callBackDelta(char *topicName, int payloadLen, char *payLoad)
{
strncpy(rcvdPayload,payLoad,payloadLen);
rcvdPayload[payloadLen] = 0;
msgReceived = 1;
}

void con() {
  //와이파이
  // Connect to WPA/WPA2 network. Change this line if using open or WEP network:
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID,  WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(WIFI_SSID);
    // wait 5 seconds for connection:
    delay(5000);
  }
  Serial.println("Connected to wifi");

  //aws
  if (hornbill.connect(HOST_ADDRESS, CLIENT_ID) == 0) { // Connect to AWS
    Serial.println("Connected to AWS");
    delay(1000);
    if (0 == hornbill.subscribe(TOPIC_NAME_delta, callBackDelta))
      Serial.println("Subscribe(../update/delta) Successfull");
    else {
      Serial.println("Subscribe(../update/delta) Failed, Check the Thing Name, Certificates");
      while (1);
    }
    tilt_count=10;
  }
  else {
    Serial.println("AWS connection failed, Check the HOST Address");
    while (1);
  }
}


void IRAM_ATTR detect_vibration() {
if(inService==1) return;
  inService=1;
  shocked=true;
  startDeb = millis();
  return;
}
void setup() {
Wire.begin();
Wire.beginTransmission(MPU);
Wire.write(0x6B);  // PWR_MGMT_1 register
Wire.write(0);     // set to zero (wakes up the MPU-6050)
Wire.endTransmission(true);
Serial.begin(115200);
bt.begin("ESP32_SSC2");
Serial.println("Waiting for pairing");
delay(2000);
pinMode(ledPin,OUTPUT);
pinMode(vibration_sensor,INPUT);
attachInterrupt(digitalPinToInterrupt(vibration_sensor),detect_vibration , 1);
Serial.println(esp_get_free_heap_size());
delay(2000);
}

void loop() {
Wire.beginTransmission(MPU);
Wire.write(0x3B);  // starting with register 0x3B (ACCEL_XOUT_H)
Wire.endTransmission(false);
Wire.requestFrom(MPU,14,true);  // request a total of 14 registers
AcX=Wire.read()<<8|Wire.read();  // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)   
AcY=Wire.read()<<8|Wire.read();  // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
AcZ=Wire.read()<<8|Wire.read();  // 0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)
Tmp=Wire.read()<<8|Wire.read();  // 0x41 (TEMP_OUT_H) & 0x42 (TEMP_OUT_L)
GyX=Wire.read()<<8|Wire.read();  // 0x43 (GYRO_XOUT_H) & 0x44 (GYRO_XOUT_L)
GyY=Wire.read()<<8|Wire.read();  // 0x45 (GYRO_YOUT_H) & 0x46 (GYRO_YOUT_L)
GyZ=Wire.read()<<8|Wire.read();  // 0x47 (GYRO_ZOUT_H) & 0x48 (GYRO_ZOUT_L)

if (isnan(AcX) || isnan(AcY) || isnan(AcZ)) {
Serial.println("Failed to read from Gyro sensor!");
}
else {

if(AcX>13000||AcX<-13000||AcZ>13000||AcZ<-13000){
  tilt_state = true;
}

if(AcX<13000&&AcX>-13000&&AcZ<13000&&AcZ>-13000){
  tilt_state = false;
  tilt_count = 0;
}

if(tilt_state){
  tilt_count += 1;
  if(우와){
    ESP32_emergency = true;
  } else{
    ESP32_emergency = false;
  }
  if(tilt_count>3){
    ESP32_emergency = true;
  } else {
    ESP32_emergency = false;
  }
}
if(우와){
  if(emergency_state==false){
  if(millis()-startDeb>20000){
    shocked=false;
    inService=0;
  }
  }
}
if(WiFi.status() != WL_CONNECTED){
  while (1) {
    if (bt.available()) {
      Serial.printf("tilt_state : %s\n" , (tilt_state?"YES":"NO"));
      Serial.printf("ESP32_emergecy : %s\n" , (ESP32_emergency?"YES":"NO"));
      ch = bt.read();
      if (flag == 0) {
        Lati += ch;
      } else if (flag == 2) {
        Long += ch;
      }
      if (ch == '\n') {
        flag++;
        if (flag == 4 && Lati.length() && Long.length()) {
          la = Lati.toDouble();
          lo = Long.toDouble();
          latitude = la;
          longitude = lo;
          Serial.printf("Lati : %f\n", la);
          Serial.printf("Long : %f\n", lo);
          Lati = "";
          Long = "";
          flag = 0;
          break;
        }
      }
    }
  }
  if (ESP32_emergency) {
    if (!push_one) {
      bt.println(1);
      delay(200);
      bt.end();
      con();
      push_one = true;
    }
  }
} else{
// at first, handle subscribe messages..
StaticJsonDocument<200> msg; // reserve stack mem for handling json msg
if(msgReceived == 1) {
msgReceived = 0;
Serial.print("Received Message(Update/Delta):");
Serial.println(rcvdPayload);
// Deserialize the JSON document
if (deserializeJson(msg, rcvdPayload)) { // if error
Serial.print(F("deserializeJson() failed.. \n"));
while(1);
}

JsonObject state = msg["state"];
String tilt = state["Tilted"];
String emergency = state["Emergency"];
String siren = state["Siren"];

if (tilt.equals("YES")){
tilt_state = true;
sprintf(payload,"{\"state\":{\"reported\":{\"Tilted\":\"%s\"}}}",(tilt_state?"YES":"NO"));
}
else if (tilt.equals("NO")) {
tilt_state = false;
sprintf(payload,"{\"state\":{\"reported\":{\"Tilted\":\"%s\"}}}",(tilt_state?"YES":"NO"));
}
else { // invalid delta
}
if (emergency.equals("YES")){
emergency_state = true;
sprintf(payload,"{\"state\":{\"reported\":{\"Emergency\":\"%s\"}}}",(emergency_state?"YES":"NO"));
}
else if (emergency.equals("NO")){
emergency_state = false;
sprintf(payload,"{\"state\":{\"reported\":{\"Emergency\":\"%s\"}}}",(emergency_state?"YES":"NO"));
}
else { // invalid delta
}
if (siren.equals("ON")){
siren_state = true;
sprintf(payload,"{\"state\":{\"reported\":{\"Siren\":\"%s\"}}}",(siren_state?"ON":"OFF"));
}
else if (siren.equals("OFF")){
siren_state = false;
sprintf(payload,"{\"state\":{\"reported\":{\"Siren\":\"%s\"}}}",(siren_state?"ON":"OFF"));
}
else { // invalid delta
}
digitalWrite(ledPin, emergency_state);
if(hornbill.publish(TOPIC_NAME_update,payload) == 0) { // Publish the message
Serial.print("Publish Message: ");
Serial.println(payload);
}
else {
Serial.print("Publish failed: ");
Serial.println(payload);
}
}

sprintf(payload,"{\"state\":{\"reported\":{\"AcX\":%d,\"AcY\":%d,\"AcZ\":%d,\"Emergency\":\"%s\",\"Siren\":\"%s\",\"Latitude\":%f,\"Longitude\":%f,\"Tilted\":\"%s\",\"Shocked\":\"%s\",\"TiltCount\":%d}}}",AcX,AcY,AcZ,(emergency_state?"YES":"NO"),(siren_state?"ON":"OFF"),latitude,longitude,(tilt_state?"YES":"NO"),(shocked?"YES":"NO"),tilt_count);
if(hornbill.publish(TOPIC_NAME_update,payload) == 0) { // Publish the message
Serial.print("Publish Message: ");
Serial.println(payload);
}
else {
Serial.print("Publish failed: ");
Serial.println(payload);
}
// publish the temp and humidity every 5 seconds.
vTaskDelay(5000 / portTICK_RATE_MS);
}
}
}