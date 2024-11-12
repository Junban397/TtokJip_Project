package com.example.ttokjip.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttokjip.R
import com.example.ttokjip.adapter.ModeSettingAdapter
import com.example.ttokjip.data.ModeRequest
import com.example.ttokjip.data.ModeSetting
import com.example.ttokjip.databinding.ModeManagerBinding
import com.example.ttokjip.network.RetrofitClient
import com.example.ttokjip.viewmodel.DeviceViewModel
import kotlinx.coroutines.launch

class ModeSettingDialog() : DialogFragment() {
    private var _binding: ModeManagerBinding? = null
    private val binding get() = _binding!!
    private lateinit var modeSettingAdapter: ModeSettingAdapter
    private lateinit var deviceViewModel: DeviceViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            // DP 값을 픽셀로 변환
            val width = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                350f,
                resources.displayMetrics
            ).toInt()
            val height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                700f,
                resources.displayMetrics
            ).toInt()

            dialog.window?.setLayout(width, height)  // 300dp 너비와 500dp 높이 설정
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ModeManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        modeSettingAdapter = ModeSettingAdapter { modeSetting, newStatus ->
            // 상태가 변경된 후 백엔드에 API 호출
            updateModeStatusOnServer(modeSetting, newStatus)
        }

        binding.modeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.modeRecyclerView.adapter = modeSettingAdapter

        binding.outingBtn.setOnClickListener { modeClick("outing") }
        binding.homecomingBtn.setOnClickListener { modeClick("homecoming") }
        binding.sleepingBtn.setOnClickListener { modeClick("sleeping") }
        binding.ventBtn.setOnClickListener { modeClick("vent") }
        binding.powerSavingBtn.setOnClickListener { modeClick("powerSaving") }

        deviceViewModel = ViewModelProvider(this).get(DeviceViewModel::class.java)
        observeModeSettings()

    }

    private fun updateModeStatusOnServer(modeSetting: ModeSetting, newStatus: Boolean) {
        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        // 토큰이 null이 아니면 서버에 상태 변경 요청
        if (token != null) {
            val modeRequest = ModeRequest(
                houseId = modeSetting.houseId,
                deviceId = modeSetting.deviceId,
                mode = modeSetting.mode,
                newStatus = newStatus
            )

            // 서버에 상태 변경 요청
            viewLifecycleOwner.lifecycleScope.launch {
                val response =
                    RetrofitClient.apiService.updateModeSetting(modeRequest, "Bearer $token")
                if (response.isSuccessful) {
                    Log.d("ModeSetting", "Mode status updated successfully.")
                } else {
                    Log.e("ModeSetting", "Failed to update mode status: ${response.code()}")
                }
            }
        }
    }

    private fun modeClick(mode: String) {
        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        // 토큰이 null이 아니면 디바이스 목록을 가져오기
        if (token != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                deviceViewModel.fetchModeSetting(token, mode)
            }
        }
        updateButtonColors(mode)
    }

    private fun updateButtonColors(mode: String) {
        binding.outingBtn.setBackgroundColor(resources.getColor(R.color.white))
        binding.homecomingBtn.setBackgroundColor(resources.getColor(R.color.white))
        binding.sleepingBtn.setBackgroundColor(resources.getColor(R.color.white))
        binding.ventBtn.setBackgroundColor(resources.getColor(R.color.white))
        binding.powerSavingBtn.setBackgroundColor(resources.getColor(R.color.white))

        when (mode) {
            "outing" -> binding.outingBtn.setBackgroundColor(resources.getColor(R.color.selected_button_color))
            "homecoming" -> binding.homecomingBtn.setBackgroundColor(resources.getColor(R.color.selected_button_color))
            "sleeping" -> binding.sleepingBtn.setBackgroundColor(resources.getColor(R.color.selected_button_color))
            "vent" -> binding.ventBtn.setBackgroundColor(resources.getColor(R.color.selected_button_color))
            "powerSaving" -> binding.powerSavingBtn.setBackgroundColor(resources.getColor(R.color.selected_button_color))
        }
    }

    private fun observeModeSettings() {
        deviceViewModel.modeSettingList.observe(viewLifecycleOwner) { modeSettings ->
            modeSettingAdapter.submitList(modeSettings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.CustomDialogTheme


}
