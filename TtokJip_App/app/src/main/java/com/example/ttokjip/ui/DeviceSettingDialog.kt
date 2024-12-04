package com.example.ttokjip.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.ttokjip.R
import com.example.ttokjip.data.Device
import com.example.ttokjip.data.StatusRequest
import com.example.ttokjip.databinding.DialogDeviceManagementBinding
import com.example.ttokjip.databinding.DialogDeviceSettingBinding
import com.example.ttokjip.network.RetrofitClient
import com.example.ttokjip.viewmodel.DeviceViewModel
import kotlinx.coroutines.launch

class DeviceSettingDialog(private val device: Device) : DialogFragment() {
    private lateinit var deviceViewModel: DeviceViewModel
    private var _binding: DialogDeviceSettingBinding? = null
    private val binding get() = _binding!!
    private var token: String = ""
    private lateinit var loadingDialog: LoadingDialog
    var onDismissListener: ((Device, String) -> Unit)? = null  // 데이터를 전달할 리스너

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // 배경 투명 설정
        deviceViewModel = ViewModelProvider(this).get(DeviceViewModel::class.java)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDeviceSettingBinding.inflate(inflater, container, false)
        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""
        loadingDialog = LoadingDialog(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deviceImgDialog.setImageResource(device.getImageResource())
        binding.deviceTypeDialog.setText(device.deviceType)
        binding.deviceLocDialog.setText(device.deviceLocation)
        // 온도 설정 레이아웃을 deviceType에 따라서 숨기거나 보이게 설정
        val setTempLayout = binding.setTemp
        if (device.deviceType == "에어컨" || device.deviceType == "히터" || device.deviceType == "난방") {
            setTempLayout.visibility = View.VISIBLE
        } else {
            setTempLayout.visibility = View.GONE
        }

        val settingTempUi = binding.settingTemp
        settingTempUi.minValue = 10
        settingTempUi.maxValue = 40
        settingTempUi.wrapSelectorWheel = false

        val typeList = arrayOf(
            "없음",
            "1시간",
            "2시간",
            "3시간",
            "4시간",
            "5시간",
            "6시간",
            "7시간",
            "8시간",
            "9시간",
            "10시간"
        )
        val adapter = ArrayAdapter(
            binding.root.context,
            android.R.layout.simple_dropdown_item_1line,
            typeList
        )
        binding.inputSearchType.setAdapter(adapter)
        binding.inputSearchType.setText("없음", false)
        binding.inputSearchType.setOnClickListener {
            binding.inputSearchType.requestFocus()  // 클릭 시 포커스를 맞추기
            binding.inputSearchType.showDropDown()  // 드롭다운 표시
        }

        binding.submitBtn.setOnClickListener {
            Log.d("DeviceSettingDialog", "submitBtn clicked")
            setOnDevice(device.deviceId,device.sensorName,true)
            loadingDialog.show()
            Handler(Looper.getMainLooper()).postDelayed({
                loadingDialog.dismiss()
                dismiss() // 3초 후 다이얼로그 종료
            }, 1000)  // 3000ms (3초)
        }
    }

    private fun setOnDevice(deviceId:String,deviceSensor:String, status:Boolean){
        viewLifecycleOwner.lifecycleScope.launch {
            deviceViewModel.deviceSettingSwitch(deviceId, deviceSensor, status,token)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // 다이얼로그가 닫힐 때 호출할 메서드 실행
        val selectedTime = binding.inputSearchType.text.toString()
        onDismissListener?.invoke(device,selectedTime)  // 'device' 데이터를 전달
    }


}