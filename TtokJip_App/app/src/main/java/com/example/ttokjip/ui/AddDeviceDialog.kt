package com.example.ttokjip.ui

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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.ttokjip.R
import com.example.ttokjip.data.AddDevice
import com.example.ttokjip.data.Device
import com.example.ttokjip.data.SensorDataRequest
import com.example.ttokjip.databinding.DialogAddDeviceBinding
import com.example.ttokjip.databinding.DialogDeviceManagementBinding
import com.example.ttokjip.network.RetrofitClient
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddDeviceDialog(private val deviceName: String) : DialogFragment() {
    private var _binding: DialogAddDeviceBinding? = null
    private val binding get() = _binding!!
    private var token: String = ""
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)


        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddDeviceBinding.inflate(inflater, container, false)

        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val typeList = arrayOf(
            "전등",
            "에어컨",
            "히터",
            "선풍기",
            "공기 청정기",
            "난방",
            "제습기",
            "CCTV",
            "냉장고",
            "세탁기",
            "전자레인지",
            "로봇 청소기",
            "TV",
            "가습기"
        )
        val adapter = ArrayAdapter(
            binding.root.context,
            android.R.layout.simple_dropdown_item_1line,
            typeList
        )
        binding.searchDeviceNameDialog.setText(deviceName)
        binding.inputSearchType.setAdapter(adapter)

        // 초기 데이터 설정
        binding.searchDeviceNameDialog.text = deviceName

        // AutoCompleteTextView 클릭 시 드롭다운 강제 표시
        binding.inputSearchType.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.inputSearchType.showDropDown()
            }
        }

        binding.addSearchDeviceBtn.setOnClickListener {
            val inputDeviceName = binding.inputSearchName.text.toString()
            val inputDeviceType = binding.inputSearchType.text.toString()
            val inputDeviceLoc = binding.inputSearchLoc.text.toString()
            if (inputDeviceName.isNotEmpty() && inputDeviceType.isNotEmpty() && inputDeviceLoc.isNotEmpty()) {
                val newDevice = AddDevice(
                    deviceId = System.currentTimeMillis().toString(), // 고유 ID 생성
                    sensorName = deviceName,
                    deviceName = inputDeviceName,
                    deviceType = inputDeviceType,
                    deviceLocation = inputDeviceLoc,
                    deviceStatus = false,
                    isFavorite = false
                )
                uploadSensorDataToServer(newDevice)

            } else {
                Toast.makeText(requireContext(), "서버 오류", Toast.LENGTH_SHORT).show()

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.CustomDialogTheme

    private fun uploadSensorDataToServer(newDevice: AddDevice) {
        // 비동기 작업을 위한 코루틴
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.addDevice(newDevice, "Bearer $token")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("addDevice", "디바이스 업로드 성공!")
                        Snackbar.make(binding.root, "기기 추가가 완료되었습니다", Snackbar.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            dismiss() // 3초 후 다이얼로그 종료
                        }, 3000)  // 3000ms (3초)
                    } else {
                        // 실패 응답에 대해 추가 로그 및 Snackbar
                        Log.e("addDevice", "디바이스 업로드 실패: ${response.errorBody()?.string()}")
                        Snackbar.make(binding.root, "서버 오류: ${response.errorBody()?.string()}", Snackbar.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // 예외 처리에서 Snackbar
                    Snackbar.make(binding.root, "서버 오류: ${e.message}", Snackbar.LENGTH_SHORT).show()

                }
                Log.e("addDevice", "디바이스 업로드 중 오류 발생: ${e.message}")
            }
        }
    }
}