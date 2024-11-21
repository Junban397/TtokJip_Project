#include <SoftwareSerial.h>
#include <DHT.h>

#define DHT_PIN A5          // DHT-11 센서가 연결된 핀
#define DHT_TYPE DHT11      // DHT-11 센서 타입

SoftwareSerial bleSerial(A0, A1);  // RX, TX (블루c:\Users\y\Desktop\TtokJip_Project\TtokJip_Arduino\sketch_jul27a_copy_20241120230116\sketch_jul27a_copy_20241120230116.ino투스 시리얼)
DHT dht(DHT_PIN, DHT_TYPE);       // DHT 센서 객체 생성

void setup()
{
  Serial.begin(9600);               // 시리얼 모니터용
  Serial.println("ByPass Test Started!");
  bleSerial.begin(9600);            // 블루투스 시리얼 통신 시작
  dht.begin();                      // DHT 센서 초기화
}

void loop()
{
  // 30초마다 DHT 센서로부터 온도와 습도 값 읽기
  delay(5000);  // 30초 대기

  // DHT 센서로부터 온도 및 습도 값 읽기
  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature();

  // 센서 값이 정상적으로 읽혔는지 확인
  if (isnan(humidity) || isnan(temperature)) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }

  // 온도와 습도 데이터를 문자열로 만들어 블루투스를 통해 전송
  String data =String(temperature) + ", " + String(humidity);
  bleSerial.println(data);  // 블루투스로 데이터 전송
  Serial.println(data);     // 시리얼 모니터에 출력
}