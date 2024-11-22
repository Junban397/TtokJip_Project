package com.example.ttokjip.ui

import BluetoothManager
import GridSpacingItemDecoration
import android.app.Dialog
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
import com.example.ttokjip.data.UpdateModeRequest
import com.example.ttokjip.databinding.FragmentMainViewBinding
import com.example.ttokjip.network.RetrofitClient
import com.example.ttokjip.viewmodel.DeviceViewModel
import com.example.ttokjip.viewmodel.FilterType
import kotlinx.coroutines.launch

class MainView : BaseDeviceManger() {
    private lateinit var adapter: DeviceAdapter
    private var _binding: FragmentMainViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var bluetoothManager: BluetoothManager
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainViewBinding.inflate(inflater, container, false)

        setupViewModel()
        bluetoothManager = BluetoothManager // BluetoothManager를 인스턴스화
        // BluetoothManager를 통해 연결이 되어 있는지 확인
        if (BluetoothManager.isBluetoothConnected()) {
            startReadingData()
        } else {
            Toast.makeText(context, "Bluetooth 연결이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
        // 토큰 가져오기
        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

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
            button.setOnClickListener { updateModeStatusOnServer(mode,token!!) }
        }


        setupRecyclerView(token!!)
        setupViewModelObservers()

        // 기본적으로 즐겨찾기 필터를 적용하려면, 예를 들어 이곳에서 호출:
        applyFilter(FilterType.FAVORITE)


        binding.modeSettingBtn.setOnClickListener {
            dialogModeSetting()
        }

        return binding.root
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
                    deviceViewModel.deviceFavoriteSwitch(deviceId,token)
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
            deviceViewModel.deviceUpdateModeStatus(mode,token)
        }
    }

    /**
     * ViewModel 설정
     **/
    private fun setupViewModel() {
        deviceViewModel = ViewModelProvider(this).get(DeviceViewModel::class.java)
    }

    private fun dialogModeSetting(){
            val dialog = ModeSettingDialog()
            dialog.show(parentFragmentManager, "CustomDialog")

    }

    private fun startReadingData() {
        Log.d("BluetoothService", "수신을 시작함")
        val buffer = StringBuilder() // 수신된 데이터를 누적할 버퍼

        bluetoothManager.getReceivedData().observe(viewLifecycleOwner, Observer { data ->
            Log.d("BluetoothService", "수신된 데이터: $data")
            buffer.append(data) // 데이터를 버퍼에 추가

            // 구분자("><")를 기준으로 데이터를 분리
            while (buffer.contains(">") && buffer.contains("<")) {
                val startIndex = buffer.indexOf("<")
                val endIndex = buffer.indexOf(">")

                if (startIndex < endIndex) {
                    val message = buffer.substring(startIndex + 1, endIndex) // <> 사이의 데이터 추출
                    buffer.delete(0, endIndex + 1) // 처리한 데이터 제거

                    processMessage(message.trim()) // 메시지 처리
                } else {
                    buffer.delete(0, startIndex) // 형식이 잘못된 경우 시작 인덱스 이전 데이터 제거
                }
            }
        })
    }

    /**
     * 수신 메시지를 처리
     */
    private fun processMessage(message: String) {
        when {
            message.startsWith("DHT:") -> { // 온도 및 습도 데이터 처리
                val dhtData = message.substringAfter("DHT:").split(",")
                if (dhtData.size == 2) {
                    val temperature = dhtData[0].toFloatOrNull()
                    val humidity = dhtData[1].toFloatOrNull()

                    if (temperature != null && humidity != null) {
                        Log.d("BluetoothService", "온도: $temperature, 습도: $humidity")
                        binding.temp.text = "$temperature°C"
                        binding.humt.text = "$humidity%"
                    }
                }
            }
            message.startsWith("MQ2:") -> { // MQ-2 데이터 처리
                val mq2Value = message.substringAfter("MQ2:").toIntOrNull()
                if (mq2Value != null) {
                    Log.d("BluetoothService", "MQ-2 가스 값: $mq2Value")
                    binding.sensorFire.text = when (mq2Value) {
                        in 0..100 -> {
                            binding.sensorFire.setTextColor(Color.GREEN)
                            "이상없음"
                        }
                        in 101..300 -> {
                            binding.sensorFire.setTextColor(Color.parseColor("#FFA500"))
                            "주의"
                        }
                        in 301..1000 ->{
                            binding.sensorFire.setTextColor(Color.RED)
                            "경고"
                        }
                        else -> {
                            binding.sensorFire.setTextColor(Color.RED)
                            "점검"
                        }
                    }
                }
            }
            message.startsWith("PIR:") -> { // PIR 데이터 처리
                val pirStatus = message.substringAfter("PIR:").trim()
                Log.d("BluetoothService", "PIR 센서 상태: $pirStatus")
                binding.sensorRip.text = when (pirStatus) {
                    "detect" -> {
                        binding.sensorRip.setTextColor(Color.RED)
                        "움직임 감지"}
                    "safety" -> {
                        binding.sensorRip.setTextColor(Color.GREEN)
                        "이상 없음"}
                    else -> {
                        binding.sensorRip.setTextColor(Color.BLACK)
                        "OFF"
                    }
                }
            }
            else -> {
                Log.w("BluetoothService", "알 수 없는 데이터 형식: $message")
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}