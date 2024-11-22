#include <SoftwareSerial.h>
#include <DHT.h>

#define DHT_PIN A5          // DHT-11 센서가 연결된 핀
#define DHT_TYPE DHT11      // DHT-11 센서 타입
#define MQ2_PIN A4          //MQ-2 핀
#define PIR_PIN 6           // HC-SR501 핀
#define LED_PIN1 7          // LED가 연결된 핀 (D8)
#define LED_PIN2 8          // LED가 연결된 핀 (D9)
#define LED_PIN3 9
#define LED_PIN4 10


SoftwareSerial bleSerial(A0, A1);  // RX, TX 
DHT dht(DHT_PIN, DHT_TYPE);       // DHT 센서 객체 생성

void setup()
{
  Serial.begin(9600);               // 시리얼 모니터용
  Serial.println("ByPass Test Started!");
  bleSerial.begin(9600);            // 블루투스 시리얼 통신 시작
  dht.begin();                      // DHT 센서 초기화

  pinMode(PIR_PIN, INPUT);          // PIR 핀을 입력으로 설정
  pinMode(LED_PIN1, OUTPUT);         // LED 핀을 출력으로 설정
  pinMode(LED_PIN2, OUTPUT);
  pinMode(LED_PIN3, OUTPUT);
  pinMode(LED_PIN4, OUTPUT);

  digitalWrite(LED_PIN1, HIGH);       // 초기 LED 상태를 ON로 설정
  digitalWrite(LED_PIN2, HIGH);   
  digitalWrite(LED_PIN3, HIGH); 
  digitalWrite(LED_PIN4, HIGH); 
}   

void loop()
{

  delay(5000);  // 30초 대기
  readDHTSensor();
  readMQ2Sensor();
  readPIRSensor();


}

void readDHTSensor()
{
  // DHT 센서로부터 온도 및 습도 값 읽기
  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature();

  // 센서 값이 정상적으로 읽혔는지 확인
  if (isnan(humidity) || isnan(temperature)) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }

  // 온도와 습도 데이터를 문자열로 만들어 블루투스를 통해 전송
  String data = String(temperature) + ", " + String(humidity);
  bleSerial.println(data);  // 블루투스로 데이터 전송
  Serial.println("DHT Data: " + data);  // 시리얼 모니터에 출력
}

void readMQ2Sensor()
{
  int mq2Value = analogRead(MQ2_PIN);  // MQ-2 센서 값 읽기

  // MQ-2 센서 값 출력
  Serial.print("MQ-2 Gas Value: ");
  Serial.println(mq2Value);

  // MQ-2 데이터를 블루투스와 시리얼 모니터로 전송
  bleSerial.print("MQ-2 Gas Value: ");
  bleSerial.println(mq2Value);
}

void readPIRSensor()
{
  int pirValue = digitalRead(PIR_PIN);  // PIR 센서 값 읽기

  // PIR 센서 상태 출력
  if (pirValue == HIGH) {
    Serial.println("PIR Sensor: Motion Detected!");
    //bleSerial.println("PIR Sensor: Motion Detected!");
  } else {
    Serial.println("PIR Sensor: No Motion.");
    //bleSerial.println("PIR Sensor: No Motion.");
  }
}