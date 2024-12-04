package com.example.ttokjip.ui

import BluetoothManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.ttokjip.data.Device
import com.example.ttokjip.viewmodel.DeviceViewModel
import com.example.ttokjip.viewmodel.FilterType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class BaseDeviceManger : Fragment() {
    protected lateinit var deviceViewModel: DeviceViewModel
    private lateinit var bluetoothManager: BluetoothManager
    private val buffer = StringBuilder()
    private var token: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 토큰 가져오기
        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""
        bluetoothManager = BluetoothManager
    }

    // Device Info Dialog 출력
    protected fun showDeviceDialog(device: Device) {
        val dialog = DeviceInfoDialog(device)
        dialog.onDismissListener = {
            viewLifecycleOwner.lifecycleScope.launch {
                deviceViewModel.fetchDevices(token)
            }

        }
        dialog.show(parentFragmentManager, "CustomDialog")
    }
    protected fun showSettingDeviceDialog(device: Device) {
        val dialog = DeviceSettingDialog(device)
        dialog.onDismissListener = {device, selectedTime ->

            if(selectedTime=="없음"){
                viewLifecycleOwner.lifecycleScope.launch {
                    // 데이터를 불러오기 전에 fetchDevices 실행
                    deviceViewModel.fetchDevices(token)
                }
            }else{
                val delayTimeInSeconds = when (selectedTime) {
                    "1시간" -> 1000L
                    "2시간" -> 2000L
                    "3시간" -> 3000L
                    "4시간" -> 4000L
                    "5시간" -> 5000L
                    "6시간" -> 6000L
                    "7시간" -> 7000L
                    "8시간" -> 8000L
                    "9시간" -> 9000L
                    "10시간" -> 10000L
                    else -> 86400000L  // "없음"일 경우 0초
                }
                // viewLifecycleOwner.lifecycleScope.launch 내에서 코루틴을 실행
                viewLifecycleOwner.lifecycleScope.launch {
                    // 데이터를 불러오기 전에 fetchDevices 실행
                    deviceViewModel.fetchDevices(token)

                    // 3초 후에 deviceSettingSwitch 실행
                    delay(delayTimeInSeconds) // 코루틴 내에서의 delay 사용 (Handler 대신)
                    deviceViewModel.deviceSettingSwitch(device.deviceId, device.sensorName, false, token)
                    deviceViewModel.fetchDevices(token)
                }
            }
        }
        dialog.show(parentFragmentManager, "CustomDialog")
    }

    // Bluetooth 데이터 읽기 시작
    protected fun startReadingBluetoothData() {
        if (bluetoothManager.isBluetoothConnected()) {
            bluetoothManager.getReceivedData().observe(viewLifecycleOwner) { data ->
                buffer.append(data)
                processBufferedData()
            }
        } else {
            //Toast.makeText(context, "Bluetooth 연결이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun processBufferedData() {
        while (buffer.contains(">") && buffer.contains("<")) {
            val startIndex = buffer.indexOf("<")
            val endIndex = buffer.indexOf(">")

            if (startIndex < endIndex) {
                val message = buffer.substring(startIndex + 1, endIndex).trim()
                buffer.delete(0, endIndex + 1)
                processBluetoothMessage(message)
            } else {
                buffer.delete(0, startIndex)
            }
        }
    }
    // Bluetooth 메시지 처리 - 하위 클래스에서 구현
    protected open fun processBluetoothMessage(message: String) {
        Log.w("BaseDeviceManger", "처리되지 않은 메시지: $message")
    }
}