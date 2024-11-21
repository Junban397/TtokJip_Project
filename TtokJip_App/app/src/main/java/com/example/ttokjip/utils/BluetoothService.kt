package com.example.ttokjip.utils


import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothService : Service() {

    private val binder = LocalBinder()
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null


    init {
        if (bluetoothAdapter == null) {
            Log.e("BluetoothService", "블루투스를 지원하지 않는 기기입니다.")
            // 서비스 내에서 Toast를 띄우는 경우 MainThread에서 호출해야 하므로 Handler 사용
            android.os.Handler(mainLooper).post {
                Toast.makeText(applicationContext, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("BluetoothService", "블루투스 어댑터 초기화 성공")
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    // 블루투스 장치 검색 및 목록 표시
    fun searchBluetoothDevices(context: Context, onDeviceSelected: (BluetoothDevice) -> Unit) {
        Log.d("BluetoothService", "블루투스 장치 검색 시작")
        val devices = bluetoothAdapter.bondedDevices
        if (devices.isNotEmpty()) {
            val deviceNames = devices.map { it.name }
            val deviceAddresses = devices.map { it.address }

            Log.d("BluetoothService", "검색된 장치 목록: $deviceNames")

            // 장치 목록을 AlertDialog로 표시
            AlertDialog.Builder(context)
                .setTitle("페어링된 블루투스 장치")
                .setItems(deviceNames.toTypedArray()) { _, which ->
                    val device = devices.first { it.address == deviceAddresses[which] }
                    Log.d("BluetoothService", "선택된 장치: ${device.name}, ${device.address}")
                    onDeviceSelected(device) // 선택된 장치로 연결
                    saveDeviceToPreferences(device) // 선택된 장치 정보 저장
                }
                .setNegativeButton("취소", null)
                .show()
        } else {
            Log.d("BluetoothService", "페어링된 블루투스 장치가 없습니다.")
            android.os.Handler(mainLooper).post {
                Toast.makeText(context, "페어링된 블루투스 장치가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 선택된 장치에 연결
    fun connectToDevice(device: BluetoothDevice) {
        val uuid = device.uuids.first().uuid
        Log.d("BluetoothService", "장치에 연결 시도: ${device.name}, UUID: $uuid")
        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)

        Thread {
            try {
                bluetoothSocket?.connect()
                inputStream = bluetoothSocket?.inputStream
                outputStream = bluetoothSocket?.outputStream
                // UI 스레드에서 Toast 호출
                android.os.Handler(mainLooper).post {
                    Log.d("BluetoothService", "블루투스 연결 성공")
                    Toast.makeText(applicationContext, "블루투스 연결 성공", Toast.LENGTH_SHORT).show()
                }

                // 데이터 수신 시작
                listenForData()
            } catch (e: IOException) {
                e.printStackTrace()
                // UI 스레드에서 Toast 호출
                android.os.Handler(mainLooper).post {
                    Log.e("BluetoothService", "블루투스 연결 실패: ${e.message}")
                    Toast.makeText(applicationContext, "블루투스 연결 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // 데이터를 계속해서 읽기 위한 메소드
    fun listenForData() {
        Log.d("BluetoothService", "수신을 시작함")
        val buffer = StringBuilder()
        Thread {
            try {
                val byteBuffer  = ByteArray(1024)  // 수신 데이터를 담을 버퍼
                var bytes: Int

                while (bluetoothSocket?.isConnected == true) {
                    // 데이터를 읽어들임
                    bytes = inputStream?.read(byteBuffer) ?: -1
                    if (bytes > 0) {
                        val readMessage = String(byteBuffer, 0, bytes)
                        Log.d("BluetoothService", "수신된 데이터: $readMessage")
                        buffer.append(readMessage)  // 받은 데이터를 누적

                        // 데이터가 "숫자, 숫자" 형식이면 처리
                        if (buffer.contains(",")) {
                            val data = buffer.toString().trim()
                            Log.d("BluetoothService", "완전한 데이터 수신: $data")
                            processReceivedData(data)  // 데이터 처리

                            // 처리 후 버퍼 초기화
                            buffer.clear()
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    // 수신된 데이터를 처리하는 메소드
    private fun processReceivedData(data: String) {
        Log.d("BluetoothService", "수신된 데이터 처리: $data")
        val regex = "([0-9.]+),\\s*([0-9.]+)".toRegex()
        val matchResult = regex.find(data)
        if (matchResult != null) {
            val temperature = matchResult.groupValues[1].toFloat()
            val humidity = matchResult.groupValues[2].toFloat()
            Log.d("BluetoothService", "온도: $temperature, 습도: $humidity")

            // Broadcast로 데이터 전송
            val intent = Intent("com.example.ttokjip.BLUETOOTH_DATA")
            intent.putExtra("temperature", temperature)
            intent.putExtra("humidity", humidity)
            sendBroadcast(intent)
        } else {
            Log.e("BluetoothService", "데이터 형식 오류: $data")
        }
    }

    // SharedPreferences에서 저장된 장치 정보 가져오기
    fun getDeviceFromPreferences(): BluetoothDevice? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val deviceName = sharedPreferences.getString("deviceName", null)
        val deviceAddress = sharedPreferences.getString("deviceAddress", null)
        val deviceUUIDString = sharedPreferences.getString("deviceUUID", null)

        Log.d("BluetoothService", "SharedPreferences에서 장치 정보 확인")

        // 저장된 값이 있으면, BluetoothDevice 객체를 반환
        if (deviceName != null && deviceAddress != null && deviceUUIDString != null) {
            val deviceUUID = UUID.fromString(deviceUUIDString)
            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            Log.d("BluetoothService", "저장된 장치로 연결 시도: ${device.name}, ${device.address}")
            return device
        }
        Log.d("BluetoothService", "저장된 장치 정보가 없습니다.")
        return null
    }

    // 장치 정보를 SharedPreferences에 저장
    private fun saveDeviceToPreferences(device: BluetoothDevice) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = sharedPreferences.edit()

        // 장치 정보 저장 (UUID 포함)
        editor.putString("deviceName", device.name)
        editor.putString("deviceAddress", device.address)
        editor.putString("deviceUUID", device.uuids?.firstOrNull()?.uuid.toString())  // UUID 저장
        editor.apply()

        Log.d("BluetoothService", "장치 정보 저장 완료: ${device.name}, ${device.address}")
    }
}