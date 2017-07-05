#include <DHT.h>
#include <DHT_U.h>

#include <Firebase.h>
#include <FirebaseArduino.h>
#include <FirebaseCloudMessaging.h>
#include <FirebaseError.h>
#include <FirebaseHttpClient.h>
#include <FirebaseObject.h>
#include <Adafruit_Sensor.h>
#include <MQ2.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

#define ssid     "TGiang2"
#define password "truonggiang1512"
#define LEDPIN 13
#define DHTPIN 14
#define DHTTYPE DHT11
#define FIREBASE_HOST "demonhung-cb7f1.firebaseio.com"
#define FIREBASE_AUTH "7AZqdwaoLOqjT8b0q2nDDmOhxCCEJd8w7fThg3kN"

  int pin = A0;
  
  MQ2 mq2(pin);
  DHT dht(DHTPIN, DHTTYPE, 11);
  WiFiServer server(80);
  float nhietDo, doAm;
  float co, smoke, lpg;
  bool ledOn;
  
  void setup(){
  Serial.begin(115200);
  setupESP();
  setupLed();
  setUpFirebase();
  ledOn = false;
  dht.begin();
  mq2.begin();
  }

  void setupESP(){
    WiFi.begin(ssid, password);
    Serial.print("connecting");
    while (WiFi.status() != WL_CONNECTED) {
      Serial.print(".");
      delay(500);
    }
    Serial.println();
    Serial.print("connected: ");
    Serial.println(WiFi.localIP());
    Serial.println();
  }

  void setupLed(){
    pinMode(LEDPIN, OUTPUT);
    digitalWrite(LEDPIN, LOW);
  }

  void setUpFirebase(){
    Firebase.begin(FIREBASE_HOST,FIREBASE_AUTH);
  }

  void sendDataToFirebase(float nhietDo, float doAm, float lpg, float co, float smoke){
    Firebase.setFloat("nhietDo",nhietDo);
    Firebase.setFloat("doAm",doAm);
    Firebase.setFloat("lpg", lpg);
    Firebase.setFloat("co", co);
    Firebase.setFloat("smoke", smoke);
    Serial.println("Upload thanh cong!");
    Serial.println();
  }
  
  void loop() {
    nhietDo = dht.readTemperature();
    doAm = dht.readHumidity();
    lpg = mq2.readLPG();  
    co = mq2.readCO();
    smoke = mq2.readSmoke();

    if(!isnan(nhietDo) && !isnan(doAm)){
      Serial.print("Temp: ");
      Serial.println(nhietDo);
      Serial.print("Humid: ");
      Serial.println(doAm);
      Serial.print("LPG: ");
      Serial.println(lpg);
      Serial.print("CO: ");
      Serial.println(co);
      Serial.print("Smoke: ");
      Serial.println(smoke);

      if(lpg>=5000 || co >= 5000 || smoke >= 5000){
         if(ledOn == true){
        }else{
          digitalWrite(LEDPIN, HIGH);
          ledOn = true;
        }
      }else{
        if(ledOn == false){
        }else{
          digitalWrite(LEDPIN, LOW);
          ledOn = false;
        }
      }

      sendDataToFirebase(nhietDo, doAm, lpg, co, smoke);
    }
    delay(1000);
}
