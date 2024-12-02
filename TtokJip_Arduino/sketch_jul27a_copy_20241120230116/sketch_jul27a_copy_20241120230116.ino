#include <SoftwareSerial.h>
#include <DHT.h>

#define DHT_PIN A5          // DHT-11 센서가 연결된 핀
#define DHT_TYPE DHT11      // DHT-11 센서 타입
#define MQ2_PIN A4          // MQ-2 핀
#define PIR_PIN 6           // HC-SR501 핀
#define LED_PIN1 7          // LED가 연결된 핀 (D8)
#define LED_PIN2 8          // LED가 연결된 핀 (D9)
#define LED_PIN3 9
#define LED_PIN4 10
#define LED_PIN5 11

#define ACS712_PIN A3         // ACS712 전류 센서가 연결된 핀
#define VOLTAGE_REF 5.0       // 아두이노의 참조 전압 (5V)



SoftwareSerial bleSerial(A0, A1);  // RX, TX
DHT dht(DHT_PIN, DHT_TYPE);       // DHT 센서 객체 생성

// 전역 변수 선언
float current = 0.0;         // 측정된 전류 (암페어)
float power = 0.0;           // 계산된 전력 (와트)
float temperature = 0.0;
float humidity = 0.0;
int mq2Value = 0;
String pirStatus = "safety";

// PIR 센서와 MQ2 센서의 활성화 상태를 저장할 변수 선언
bool pirEnabled = true;  // PIR 센서 활성화 여부
bool mq2Enabled = true;  // MQ2 센서 활성화 여부

void setup()
{
  Serial.begin(9600);  // 시리얼 통신 초기화
  bleSerial.begin(9600);            // 블루투스 시리얼 통신 시작
  dht.begin();                      // DHT 센서 초기화

  pinMode(PIR_PIN, INPUT);          // PIR 핀을 입력으로 설정
  pinMode(LED_PIN1, OUTPUT);        // LED 핀을 출력으로 설정
  pinMode(LED_PIN2, OUTPUT);
  pinMode(LED_PIN3, OUTPUT);
  pinMode(LED_PIN4, OUTPUT);
  pinMode(LED_PIN5, OUTPUT);

  digitalWrite(LED_PIN1, HIGH);     // 초기 LED 상태를 ON로 설정
  digitalWrite(LED_PIN2, HIGH);
  digitalWrite(LED_PIN3, HIGH);
  digitalWrite(LED_PIN4, HIGH);
  digitalWrite(LED_PIN5, LOW);
}

void loop()
{
  delay(1000);
  if (pirEnabled) {
    readPIRSensor();               // PIR 센서 읽기
  }
  
  if (mq2Enabled) {
    readMQ2Sensor();               // MQ2 센서 읽기
  }

  readDHTSensor();
  sendData();                       // 데이터를 전송

  if (bleSerial.available()) {
    String command = bleSerial.readStringUntil('\n'); // 블루투스에서 받은 명령 읽기
    Serial.println(command);
    handleCommand(command); // 명령 처리
    

  }

    // 전류 측정 및 전력 계산
  readCurrent();  // 전류 값 읽기
  calculatePower();  // 전력 계산

  // 전력 값 시리얼 모니터에 출력
  Serial.print("Current: ");
  Serial.print(current);
  Serial.print(" A\t");
  Serial.print("Power: ");
  Serial.print(power);
  Serial.println(" W");
}
void readCurrent()
{
  // ACS712 센서의 아날로그 출력 값 읽기
  int sensorValue = analogRead(ACS712_PIN);
  
  // 센서 값 (0-1023)에서 512를 빼고, 이를 전류로 변환
  int voltage = sensorValue - 512;
  
  // 전류 계산 (185mV/A, 아두이노의 참조 전압은 5V)
  current = (voltage * VOLTAGE_REF) / 1024.0 / 0.185;  // 전류 계산
}

void calculatePower()
{
  // 전력 계산 (전류 * 전압)
  power = current * VOLTAGE_REF;
}

void readDHTSensor()
{
  humidity = dht.readHumidity();
  temperature = dht.readTemperature();

  if (isnan(humidity) || isnan(temperature)) {
    return;  // DHT 센서 오류 처리 없이 종료
  }
}

void readMQ2Sensor()
{
  mq2Value = analogRead(MQ2_PIN);
}

void readPIRSensor()
{
  int pirValue = digitalRead(PIR_PIN);

  // PIR 센서 상태 저장
  if (pirValue == HIGH) {
    pirStatus = "detect";
  } else {
    pirStatus = "safety";
  }
}

void sendData()
{
  // 데이터를 구분자("<>")와 함께 전송
  String data = "<DHT:" + String(temperature) + "," + String(humidity) + ">";

  // MQ2 센서가 비활성화 되어 있을 때 "off"만 전송
  if (mq2Enabled==1) {
    data += "<MQ2:" + String(mq2Value) + ">";
  } else {
    data += "<MQ2:off>";
  }
  
  
  // PIR 센서가 비활성화 되어 있을 때 "off"만 전송
  if (pirEnabled==1) {
    data += "<PIR:" + pirStatus + ">";
  } else {
    data += "<PIR:off>";
  }
  data += "<Power:" + String(power) + ">";  // 전력 (와트) 추가
  data += "<PIR_PIN,LED_PIN1,LED_PIN2,LED_PIN3,LED_PIN4,LED_PIN5>";
  
  bleSerial.print(data);
  bleSerial.println();  // 데이터 끝에 줄 바꿈 추가
}


void handleCommand(String command)
{
  // 명령어를 공백 기준으로 분리
  int spaceIndex = 0;
  String deviceCommand;
  
  while (spaceIndex != -1) {
    spaceIndex = command.indexOf(' ');  // 공백 찾기
    if (spaceIndex != -1) {
      deviceCommand = command.substring(0, spaceIndex);  // 공백 앞부분을 하나의 명령으로 처리
      command = command.substring(spaceIndex + 1);  // 나머지 부분 처리
    } else {
      deviceCommand = command;  // 마지막 명령 처리
      command = ""; // 마지막 명령어 처리 후 종료
    }
    
    // 명령어 형식 처리
    int colonIndex = deviceCommand.indexOf(':');
    if (colonIndex != -1) {
      String deviceName = deviceCommand.substring(0, colonIndex);  // 장치 이름
      String deviceState = deviceCommand.substring(colonIndex + 1);  // 장치 상태 (true 또는 false)

      // LED 제어
      if (deviceName.startsWith("LED")) {
        handleLEDCommand(deviceName, deviceState);
      }
      // PIR 센서 제어
      else if (deviceName == "PIR") {
        if (deviceState == "true") {
          pirEnabled = true;  // PIR 센서 활성화
        } else if (deviceState == "false") {
          pirEnabled = false;  // PIR 센서 비활성화
        }
      }
      // MQ2 센서 제어
      else if (deviceName == "MQ2") {
        if (deviceState == "true") {
          mq2Enabled = true;  // MQ2 센서 활성화
        } else if (deviceState == "false") {
          mq2Enabled = false;  // MQ2 센서 비활성화
        }
      }
    }
  }
}

void handleLEDCommand(String ledName, String ledState)
{
  // "true"일 경우 LED 켜기, "false"일 경우 LED 끄기
  if (ledState == "true") {
    if (ledName == "LED1") {
      digitalWrite(LED_PIN1, HIGH);  // LED1 켜기
    } else if (ledName == "LED2") {
      digitalWrite(LED_PIN2, HIGH);  // LED2 켜기
    } else if (ledName == "LED3") {
      digitalWrite(LED_PIN3, HIGH);  // LED3 켜기
    } else if (ledName == "LED4") {
      digitalWrite(LED_PIN4, HIGH);  // LED4 켜기
    } else if (ledName == "LED5") {
      digitalWrite(LED_PIN5, HIGH);  // LED4 켜기
    }
  } else if (ledState == "false") {
    if (ledName == "LED1") {
      digitalWrite(LED_PIN1, LOW);   // LED1 끄기
    } else if (ledName == "LED2") {
      digitalWrite(LED_PIN2, LOW);   // LED2 끄기
    } else if (ledName == "LED3") {
      digitalWrite(LED_PIN3, LOW);   // LED3 끄기
    } else if (ledName == "LED4") {
      digitalWrite(LED_PIN4, LOW);   // LED4 끄기
    } else if (ledName == "LED5") {
      digitalWrite(LED_PIN5, LOW);  // LED4 끄기
    }
  }
}