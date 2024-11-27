package com.example.ttokjip.ui

import BluetoothManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.ttokjip.data.WeeklyData
import com.example.ttokjip.databinding.FragmentStatisticsViewBinding
import com.example.ttokjip.network.RetrofitClient
import com.example.ttokjip.network.RetrofitClient.apiService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsView : BaseDeviceManger() {
    private var _binding: FragmentStatisticsViewBinding? = null
    private val binding get() = _binding!!
    private var totalWattage: Float = 0.0f
    private var temperature: Float = 0.0f
    private var humidity: Float = 0.0f
    private var token: String = ""
    private lateinit var bluetoothManager: BluetoothManager

    // Handler와 Runnable을 정의
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            // 10초마다 실행될 코드
            handler.postDelayed(this, 10000) // 10초마다 실행되도록 설정
            uploadSensorDataToServer() // 서버로 데이터 업로드
        }
    }

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
    override fun onStart() {
        super.onStart()
        // 10초마다 uploadSensorDataToServer 호출
        handler.post(runnable)
    }

    // onStop에서 반복 작업을 멈춤
    override fun onStop() {
        super.onStop()
        // 반복 작업 멈추기
        handler.removeCallbacks(runnable)
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
            message.startsWith("DHT:") -> processDhtMessage(message)
            message.startsWith("MQ2:") -> processMq2Message(message)
            message.startsWith("PIR:") -> processPirMessage(message)
            message.startsWith("Power:") -> processPowerMessage(message)
            else -> Log.w("MainView", "알 수 없는 메시지: $message")
        }
    }
    private fun processDhtMessage(message: String) {
        val data = message.substringAfter("DHT:").split(",")
        if (data.size == 2) {
            temperature = data[0].toFloatOrNull() ?: 0.0f
            humidity = data[1].toFloatOrNull() ?: 0.0f
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
                        Log.d(
                            "StatisticsView",
                            "로그 데이터 성공적으로 가져옴: ${data.toString()}"
                        )  // 응답 데이터 확인
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
        Log.d(
            "StatisticsView",
            "data.totalWattageAvg: ${data.averageMonthlyWattage}, data.lastMonthTotalWattage: ${data.lastMonthTotalWattage}, data.monthlyTotalWattage: ${data.monthlyTotalWattage}"
        )

        binding.lastMonth.text = getMonth(-1) + "월 전기 사용량"
        binding.thisMonth.text = getMonth() + "월 전기 사용량"
        binding.totalWattage.text =
            "우리 집 평균 전기 사용량 " + String.format("%.2f", data.averageMonthlyWattage) + "W"
        binding.lastMonthTotalWattage.text = String.format("%.2f", data.lastMonthTotalWattage) + "W"
        binding.thisMonthTotalWattage.text = String.format("%.2f", data.monthlyTotalWattage) + "W"

        binding.lastMonthFeedback.text = when {
            data.lastMonthTotalWattage <= data.averageMonthlyWattage - 100 -> {
                binding.lastMonthFeedback.setTextColor(Color.GREEN)
                "잘 아끼고 있어요!"
            }

            data.lastMonthTotalWattage >= data.averageMonthlyWattage + 100 -> {
                binding.lastMonthFeedback.setTextColor(Color.RED)
                "평소 보다 소비가 많아요!"
            }

            else -> {
                binding.lastMonthFeedback.setTextColor(Color.parseColor("#FFA500"))
                "평균에 가까워 지고 있어요!"
            }
        }
        binding.thisMonthFeedback.text = when {
            data.monthlyTotalWattage <= data.averageMonthlyWattage - 100 -> {
                binding.thisMonthFeedback.setTextColor(Color.GREEN)
                "잘 아끼고 있어요!"
            }

            data.monthlyTotalWattage >= data.averageMonthlyWattage + 100 -> {
                binding.thisMonthFeedback.setTextColor(Color.RED)
                "평소 보다 소비가 많아요!"
            }

            else -> {
                binding.thisMonthFeedback.setTextColor(Color.parseColor("#FFA500"))
                "평균에 가까워 지고 있어요!"
            }
        }
        weeklyChart(binding.weeklyLineChart, data.weeklyData)

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

    private fun weeklyChart(lineChart: LineChart, weeklyData: List<WeeklyData>) {
        if (weeklyData.isEmpty()) {
            lineChart.clear()
            lineChart.description.text = "데이터가 없습니다."
            return
        }
        val xLabels = weeklyData.map { it.date }
        // 온도와 습도를 Entry로 변환
        val temperatureEntries =
            weeklyData.mapIndexed { index, data -> Entry(index.toFloat(), data.temperature) }
        val humidityEntries =
            weeklyData.mapIndexed { index, data -> Entry(index.toFloat(), data.humidity) }
        // DataSet 생성
        val temperatureDataSet = LineDataSet(temperatureEntries, "온도").apply {
            color = Color.RED
            lineWidth = 2f
            circleRadius = 5f
            setCircleColor(Color.RED)
            valueTextColor = Color.RED
            setDrawValues(false) // 데이터 포인트 값 숨기기
        }

        val humidityDataSet = LineDataSet(humidityEntries, "습도").apply {
            color = Color.BLUE
            lineWidth = 2f
            circleRadius = 5f
            setCircleColor(Color.BLUE)
            valueTextColor = Color.BLUE
            setDrawValues(false) // 데이터 포인트 값 숨기기
        }

        // LineData 생성
        val lineData = LineData(temperatureDataSet, humidityDataSet)

        // 차트 설정
        lineChart.apply {
            setExtraOffsets(10f, 20f, 20f, 20f)
            data = lineData
            description.isEnabled = false
            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            }
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            // X축 설정
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                labelRotationAngle = 0f
                yOffset = 15f
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val fullDate = xLabels.getOrElse(value.toInt()) { "" } // 전체 날짜 형식 (예: 2024-11-11)
                        return if (fullDate.contains("-")) {
                            fullDate.substring(5) // "2024-11-11" → "11-11"
                        } else {
                            fullDate
                        }
                    }
                }
            }

            // Y축 설정
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f // 필요시 설정
                granularity=1f
            }
            axisRight.isEnabled = false

            // 애니메이션
            animateX(100)
        }
    }
    private fun uploadSensorDataToServer() {
        // 비동기 작업을 위한 코루틴
        lifecycleScope.launch {
            try {
                val sensorData = SensorDataRequest(
                    date = getCurrentDate(),
                    temperature = temperature,
                    humidity = humidity,
                    totalWattage = totalWattage
                )
                val response = apiService.uploadSensorData(sensorData, "Bearer $token")

                if (response.isSuccessful) {
                    Log.d("uploadSensorData", "센서 데이터 업로드 성공!")
                    totalWattage = 0.0f
                } else {
                    Log.e("uploadSensorData", "센서 데이터 업로드 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("uploadSensorData", "업로드 중 오류 발생: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
