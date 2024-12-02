package com.example.ttokjip.ui

import BluetoothManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.ttokjip.data.Device
import com.example.ttokjip.viewmodel.DeviceViewModel
import com.example.ttokjip.viewmodel.FilterType
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