package com.example.ttokjip.ui

import BluetoothManager
import GridSpacingItemDecoration
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ttokjip.R
import com.example.ttokjip.adapter.DeviceAdapter
import com.example.ttokjip.data.Device
import com.example.ttokjip.data.ModeRequest
import com.example.ttokjip.data.ModeSetting
import com.example.ttokjip.data.SensorDataRequest
import com.example.ttokjip.data.UpdateModeRequest
import com.example.ttokjip.databinding.FragmentMainViewBinding
import com.example.ttokjip.network.RetrofitClient
import com.example.ttokjip.network.RetrofitClient.apiService
import com.example.ttokjip.viewmodel.DeviceViewModel
import com.example.ttokjip.viewmodel.FilterType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainView : BaseDeviceManger() {
    private lateinit var adapter: DeviceAdapter
    private var _binding: FragmentMainViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var bluetoothManager: BluetoothManager
    private var totalWattage: Float = 0.0f
    private var temperature: Float = 0.0f
    private var humidity: Float = 0.0f
    private var token: String = ""
    private val sensorDataHandler = Handler(Looper.getMainLooper())
    private val airQualityHandler = Handler(Looper.getMainLooper())

    private val sensorDataRunnable = object : Runnable {
        override fun run() {
            uploadSensorDataToServer() // 10초마다 실행
            sensorDataHandler.postDelayed(this, 10000) // 다음 실행 예약
        }
    }
    private val airQualityRunnable = object : Runnable {
        override fun run() {
            updateAirQualityStatus() // 3초마다 실행
            airQualityHandler.postDelayed(this, 3000) // 다음 실행 예약
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainViewBinding.inflate(inflater, container, false)

        setupViewModel()
        bluetoothManager = BluetoothManager // BluetoothManager를 인스턴스화


        // 토큰 가져오기
        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""

        // 토큰이 null이 아니면 디바이스 목록을 가져오기
        if (token != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                deviceViewModel.fetchDevices(token)
            }
        }
        val modeButtons = mapOf(
            binding.outingModeBtn to "outing",
            binding.homeComeModeBtnBtn to "homecoming",
            binding.sleepingModeBtn to "sleeping",
            binding.ventModeModeBtn to "vent",
            binding.powerSavingModeBtn to "powerSaving"
        )
        modeButtons.forEach { (button, mode) ->
            button.setOnClickListener { updateModeStatusOnServer(mode, token!!) }
        }


        setupRecyclerView(token!!)
        setupViewModelObservers()
        setupBluetoothManager()

        // 기본적으로 즐겨찾기 필터를 적용하려면, 예를 들어 이곳에서 호출:
        applyFilter(FilterType.FAVORITE)


        binding.modeSettingBtn.setOnClickListener {
            dialogModeSetting()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        sensorDataHandler.postDelayed(sensorDataRunnable, 10000) // 센서 데이터 업로드 시작
        airQualityHandler.post(airQualityRunnable)
    }

    // onStop에서 반복 작업을 멈춤
    override fun onStop() {
        super.onStop()
        sensorDataHandler.removeCallbacks(sensorDataRunnable) // 센서 데이터 반복 작업 멈춤
        airQualityHandler.removeCallbacks(airQualityRunnable) // 공기 질 업데이트 반복 작업 멈춤
    }

    /**
     * RecyclerView, Adapter 연결
     **/
    private fun setupRecyclerView(token: String) {
        adapter = DeviceAdapter(
            onDeviceClick = { deviceId ->
                // 코루틴 내에서 suspend 함수 호출
                viewLifecycleOwner.lifecycleScope.launch {
                    deviceViewModel.deviceSwitch(deviceId, token)
                }
            },
            onFavoriteClick = { deviceId ->
                // 코루틴 내에서 호출
                viewLifecycleOwner.lifecycleScope.launch {
                    deviceViewModel.deviceFavoriteSwitch(deviceId, token)
                }
            },
            onLongClick = { device -> showDeviceDialog(device) }
        )

        binding.favoriteRecyclerview.apply {
            adapter = this@MainView.adapter
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(GridSpacingItemDecoration(32))
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    /**
     * LiveData 설정, RecyclerView 높이 조정
     **/
    private fun setupViewModelObservers() {
        deviceViewModel.deviceList.observe(viewLifecycleOwner) { devices ->
            devices?.let {
                // 필터를 적용한 후 UI 갱신
                val filteredDevices = applyFilter(FilterType.FAVORITE)
                adjustRecyclerViewHeight(filteredDevices.size)  // 필터된 아이템 개수로 높이 조정
            }
        }
    }

    /**
     * RecyclerView 높이 동적 조정
     **/
    private fun adjustRecyclerViewHeight(itemCount: Int) {
        val itemHeight = (180 * resources.displayMetrics.density).toInt()  // 한 아이템의 높이
        val totalHeight = itemHeight * (itemCount / 2 + itemCount % 2)  // 2개씩 배치되므로 높이를 계산

        // RecyclerView 높이 설정
        binding.favoriteRecyclerview.layoutParams =
            binding.favoriteRecyclerview.layoutParams.apply {
                height = totalHeight
            }
    }

    /**
     * 즐겨찾기 필터 적용
     **/
    private fun applyFilter(filterType: FilterType): List<Device> {
        val filteredDevices = when (filterType) {
            FilterType.FAVORITE -> deviceViewModel.deviceList.value?.filter { it.isFavorite } // 즐겨찾기만 필터링
            else -> deviceViewModel.deviceList.value // 다른 필터가 없는 경우 전체 리스트
        } ?: emptyList()  // null일 경우 빈 리스트 반환

        adapter.submitList(filteredDevices)  // 필터링된 디바이스 목록을 Adapter에 제출
        return filteredDevices // 필터링된 리스트 반환
    }

    /**
     *모드 일괄적용
     **/
    private fun updateModeStatusOnServer(mode: String, token: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            deviceViewModel.deviceUpdateModeStatus(mode, token)
        }
    }

    /**
     * ViewModel 설정
     **/
    private fun setupViewModel() {
        deviceViewModel = ViewModelProvider(this).get(DeviceViewModel::class.java)
    }

    private fun dialogModeSetting() {
        val dialog = ModeSettingDialog()
        dialog.show(parentFragmentManager, "CustomDialog")

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
            binding.temp.text = "$temperature°C"
            binding.humt.text = "$humidity%"
        }
    }

    private fun processMq2Message(message: String) {
        val mq2Value = message.substringAfter("MQ2:").toIntOrNull()
        binding.sensorFire.text = when (mq2Value) {
            in 0..100 -> {
                binding.sensorFire.setTextColor(Color.GREEN)
                "이상없음"
            }

            in 101..300 -> {
                binding.sensorFire.setTextColor(Color.parseColor("#FFA500"))
                "주의"
            }

            in 301..1000 -> {
                binding.sensorFire.setTextColor(Color.RED)
                "경고"
            }

            else -> {
                binding.sensorFire.setTextColor(Color.RED)
                "점검"
            }
        }
    }

    private fun processPirMessage(message: String) {
        val pirStatus = message.substringAfter("PIR:").trim()
        binding.sensorRip.text = when (pirStatus) {
            "detect" -> {
                binding.sensorRip.setTextColor(Color.RED)
                "움직임 감지"
            }

            "safety" -> {
                binding.sensorRip.setTextColor(Color.GREEN)
                "이상 없음"
            }

            else -> {
                binding.sensorRip.setTextColor(Color.BLACK)
                "OFF"
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

    /**
     * 로그 업로드
     */
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

    private fun getCurrentDate(): String {
        // 현재 날짜를 String으로 변환하는 로직
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun updateAirQualityStatus() {
        //18~26
        //40~70
        binding.airQualityStatus.text = when {
            // 좋음 조건
            temperature in 18f..26f && humidity in 30f..60f && binding.dustStatus.text.toString() == "좋음" -> {
                binding.airQualityStatus.setTextColor(Color.parseColor("#80ed99"))
                "좋음"
            }
            // 보통 조건
            (temperature in 15f..17f || temperature in 27f..30f) &&
                    (humidity in 20f..29f || humidity in 61f..65f) &&
                    binding.dustStatus.text.toString() == "보통" -> {
                binding.airQualityStatus.setTextColor(Color.parseColor("#FFE500"))
                "보통"
            }

            // 나쁨 조건
            else -> {
                binding.airQualityStatus.setTextColor(Color.parseColor("#FF0000"))
                "나쁨"
            }
        }
        val airQualityColor = when {
            // 좋음 조건
            temperature in 18f..26f && humidity in 30f..60f && binding.dustStatus.text.toString() == "좋음" -> R.drawable.circle_good

            // 보통 조건
            (temperature in 15f..17f || temperature in 27f..30f) &&
                    (humidity in 20f..29f || humidity in 61f..65f) &&
                    binding.dustStatus.text.toString() == "보통" -> R.drawable.circle_normal

            // 나쁨 조건
            else -> R.drawable.circle_bad
        }
        binding.airQualityStatusColor.background =
            ContextCompat.getDrawable(requireContext(), airQualityColor)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 업로드 완료 후 뷰를 안전하게 파괴
    }
}