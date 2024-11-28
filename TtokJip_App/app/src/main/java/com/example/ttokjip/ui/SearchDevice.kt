package com.example.ttokjip.ui

import BluetoothManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ttokjip.R
import com.example.ttokjip.databinding.ActivityCenterViewBinding
import com.example.ttokjip.databinding.ActivitySearchDeviceBinding

class SearchDevice : AppCompatActivity() {
    private lateinit var bluetoothManager: BluetoothManager
    private val buffer = StringBuilder()
    private lateinit var binding: ActivitySearchDeviceBinding
    private val searchDeviceList= mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun searchDevice() {
        if (BluetoothManager.isBluetoothConnected()) {
            BluetoothManager.sendData("searchDevice")
        } else {
            Toast.makeText(this, "검색된 기기가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // Bluetooth 데이터 읽기 시작
    private fun redingDevice() {
        if (bluetoothManager.isBluetoothConnected()) {
            bluetoothManager.getReceivedData().observe(this) { data ->
                buffer.append(data)
                processBufferedData()
            }
        } else {
        }

    }
    private fun processBufferedData() {
        while (buffer.contains(">") && buffer.contains("<")) {
            val startIndex = buffer.indexOf("<")
            val endIndex = buffer.indexOf(">")

            if (startIndex < endIndex) {
                val message = buffer.substring(startIndex + 1, endIndex).trim()
                searchDeviceList.add(message)
                buffer.delete(0, endIndex + 1)
            } else {
                buffer.delete(0, startIndex)
            }
        }
    }
}