package com.example.ttokjip.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.ttokjip.R
import com.example.ttokjip.data.Device
import com.example.ttokjip.databinding.DialogDeviceManagementBinding
import com.example.ttokjip.network.RetrofitClient
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceInfoDialog(private val device: Device) : DialogFragment() {
    private var _binding: DialogDeviceManagementBinding? = null
    private val binding get() = _binding!!
    private var token: String = ""
    var onDismissListener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)


        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDeviceManagementBinding.inflate(inflater, container, false)
        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deviceTypeDialog.setText(device.deviceType)
        binding.deviceNameDialog.setText(device.deviceName)
        binding.deviceLocDialog.setText(device.deviceLocation)
        binding.deviceImgDialog.setImageResource(device.getImageResource())

        binding.deleteBtnInfo.setOnClickListener {
            deleteDeviceApi(device.deviceId)

        }
    }

    private fun deleteDeviceApi(deviceId: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteDevice(deviceId, "Bearer $token")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("deleteDevice", "디바이스 삭제 성공!")
                        Snackbar.make(binding.root, "기기 삭제가 완료되었습니다", Snackbar.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            dismiss() // 3초 후 다이얼로그 종료
                        }, 3000)  // 3000ms (3초)
                    } else {
                        // 실패 응답에 대해 추가 로그 및 Snackbar
                        Log.e("deleteDeivce", "디바이스 삭제 실패: ${response.errorBody()?.string()}")
                        Snackbar.make(
                            binding.root,
                            "서버 오류: ${response.errorBody()?.string()}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // 예외 처리에서 Snackbar
                    Snackbar.make(binding.root, "서버 오류: ${e.message}", Snackbar.LENGTH_SHORT).show()

                }
                Log.e("deleteDeivce", "디바이스 삭제 중 오류 발생: ${e.message}")
            }
        }
    }
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // 다이얼로그가 닫힐 때 호출할 메서드 실행
        onDismissListener?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.CustomDialogTheme


}