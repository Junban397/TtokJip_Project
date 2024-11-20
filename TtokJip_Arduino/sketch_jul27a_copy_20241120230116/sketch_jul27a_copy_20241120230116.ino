#include <SoftwareSerial.h>

SoftwareSerial bleSerial(A0, A1); // RX, TX

void setup()
{
  Serial.begin(9600);
  Serial.println("ByPass Test Started!");
  bleSerial.begin(9600);
  bleSerial.print("at\r");
  bleSerial.setTimeout(2);
  bleSerial.readString();
}

void loop() {
  if(bleSerial.available())
    Serial.write(bleSerial.read());
  if(Serial.available())
    bleSerial.write(Serial.read());
}
