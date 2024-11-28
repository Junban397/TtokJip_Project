package com.example.ttokjip.ui

import BluetoothManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttokjip.R
import com.example.ttokjip.adapter.SearchDeviceAdapter
import com.example.ttokjip.databinding.ActivityCenterViewBinding
import com.example.ttokjip.databinding.ActivitySearchDeviceBinding

class SearchDevice : AppCompatActivity() {
    private val buffer = StringBuilder()
    private lateinit var binding: ActivitySearchDeviceBinding
    private val searchDeviceList = mutableListOf<String>(
        "PIR", "LED1", "LED2", "LED3", "LED4", "LED5"
    )
    private lateinit var searchDeviceAdapter: SearchDeviceAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchDeviceAdapter = SearchDeviceAdapter(searchDeviceList) { deviceName ->
            showDeviceDialog(deviceName) // 아이템 클릭 시 다이얼로그 표시
        }
        binding.searchDeviceRecycler.layoutManager = LinearLayoutManager(this)
        binding.searchDeviceRecycler.adapter = searchDeviceAdapter
        searchDevice()

    }

    private fun searchDevice() {
        if (BluetoothManager.isBluetoothConnected()) {
            BluetoothManager.sendData("searchDevice\n")
            redingDevice()
        } else {
            Toast.makeText(this, "검색된 기기가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // Bluetooth 데이터 읽기 시작
    private fun redingDevice() {
        if (BluetoothManager.isBluetoothConnected()) {
            BluetoothManager.getReceivedData().observe(this) { data ->
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

                // 'PIR_PIN', 'LED_PIN' 관련 데이터만 추출
                if (message.contains("PIR_PIN") || message.contains("LED_PIN")) {
                    buffer.delete(0, endIndex + 1)
                    Log.d("MainView", "누적 전력량: $searchDeviceList")
                    // 리사이클러뷰 업데이트
                } else {
                    buffer.delete(0, endIndex + 1) // 관련 없는 데이터는 버퍼에서 제거
                }
            } else {
                buffer.delete(0, startIndex) // 잘못된 데이터 제거
            }
        }
    }


    // 다이얼로그를 보여주는 함수
    private fun showDeviceDialog(deviceName: String) {
        val dialog = AddDeviceDialog(deviceName)
        dialog.show(supportFragmentManager, "CustomDialog")
    }
}