package com.example.ttokjip.ui

import BluetoothManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.ttokjip.data.GetLogDate
import com.example.ttokjip.data.SensorDataRequest
import com.example.ttokjip.data.StatisticsResponse
import com.example.ttokjip.databinding.FragmentStatisticsViewBinding
import com.example.ttokjip.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsView : BaseDeviceManger() {
    private var _binding: FragmentStatisticsViewBinding? = null
    private val binding get() = _binding!!
    private var totalWattage: Float = 0.0f
    private var token: String = ""
    private lateinit var bluetoothManager: BluetoothManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticsViewBinding.inflate(inflater, container, false)
        bluetoothManager = BluetoothManager // BluetoothManager를 인스턴스화
        // BluetoothManager를 통해 연결이 되어 있는지 확인

        // 토큰 가져오기
        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""

        setupBluetoothManager()
        getStatistics()

        return binding.root
    }
    private fun setupBluetoothManager() {
        if (bluetoothManager.isBluetoothConnected()) {
            startReadingBluetoothData()
        } else {
            Toast.makeText(context, "Bluetooth 연결이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun processBluetoothMessage(message: String) {
        when {
            message.startsWith("MQ2:") -> processMq2Message(message)
            message.startsWith("PIR:") -> processPirMessage(message)
            message.startsWith("Power:") -> processPowerMessage(message)
            else -> Log.w("MainView", "알 수 없는 메시지: $message")
        }
    }


    private fun processMq2Message(message: String) {
        val mq2Value = message.substringAfter("MQ2:").toIntOrNull()
        Log.d("MainView", "$mq2Value")
        when (mq2Value) {
            in 301..1000 -> {
                Log.d("MainView", "화재 감지 뺴예ㅖㅖㅖㅖㅖㅖㅖㅖㅖㅖㅖㅖ")
            }

            else -> {
            }
        }
    }

    private fun processPirMessage(message: String) {
        val pirStatus = message.substringAfter("PIR:").trim()
        Log.d("MainView", "$pirStatus")
        when (pirStatus) {
            "detect" -> {
                Log.d("MainView", "움직임 감지 뺴예ㅖㅖㅖㅖㅖㅖㅖㅖㅖㅖㅖㅖ")
                "움직임 감지"
            }
            else -> {
            }
        }
    }

    private fun processPowerMessage(message: String) {
        val power = message.substringAfter("Power:").toFloatOrNull()
        if (power != null) {
            totalWattage += power
            Log.d("MainView", "누적 전력량: $totalWattage")
        }
    }
    private fun getStatistics() {
        // 비동기 작업을 위한 코루틴
        lifecycleScope.launch {
            try {
                // 로그 날짜 생성
                //val logDate = GetLogDate(date = getCurrentDate())
                val logDate = getCurrentDate()
                Log.d("StatisticsView", "로그 날짜: ${logDate}")  // 로그 날짜 확인

                // API 호출
                val response = RetrofitClient.apiService.getStatistics(logDate, "Bearer $token")

                // 응답이 성공적일 경우
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        Log.d("StatisticsView", "로그 데이터 성공적으로 가져옴: ${data.toString()}")  // 응답 데이터 확인
                        // 응답 받은 데이터로 UI 업데이트
                        updateUI(data)
                    } ?: run {
                        Log.e("StatisticsView", "응답 본문이 비어있습니다.")
                    }
                } else {
                    Log.e("StatisticsView", "센서 데이터 업로드 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // 예외 처리
                Log.e("StatisticsView", "get 중 오류 발생: ${e.message}")
            }
        }
    }
    private fun updateUI(data: StatisticsResponse) {
        // UI 업데이트 전 로그
        Log.d("StatisticsView", "updateUI 호출됨")
        Log.d("StatisticsView", "data.totalWattageAvg: ${data.totalWattageAvg}, data.lastMonthTotalWattage: ${data.lastMonthTotalWattage}, data.monthlyTotalWattage: ${data.monthlyTotalWattage}")

        binding.lastMonth.text = getMonth(-1)+"월 전기 사용량"
        binding.thisMonth.text = getMonth()+"월 전기 사용량"
        binding.totalWattage.text = "우리 집 평균 전기 사용량 "+String.format("%.2f", data.totalWattageAvg)+"W"
        binding.lastMonthTotalWattage.text = String.format("%.2f", data.lastMonthTotalWattage)+"W"
        binding.thisMonthTotalWattage.text = String.format("%.2f", data.monthlyTotalWattage)+"W"
    }

    private fun getCurrentDate(): String {
        // 현재 날짜를 String으로 변환하는 로직
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun getMonth(offset: Int = 0): String {
        // 현재 날짜를 가져옵니다.
        val calendar = Calendar.getInstance()
        // offset 값을 통해 한 달 전, 한 달 후 등을 설정
        calendar.add(Calendar.MONTH, offset)
        // 월을 구합니다
        val sdf = SimpleDateFormat("MM", Locale.getDefault()) // 월만 출력
        return sdf.format(calendar.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
