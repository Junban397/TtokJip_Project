package com.example.ttokjip.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.ttokjip.R
import com.example.ttokjip.data.Device
import com.example.ttokjip.databinding.DialogDeviceManagementBinding

class DeviceInfoDialog(private val device: Device) : DialogFragment() {
    private var _binding: DialogDeviceManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Dialog 인스턴스를 생성하고, 기본 다이얼로그 설정을 사용합니다.
        val dialog = super.onCreateDialog(savedInstanceState)

        // 다이얼로그의 배경을 투명하게 설정합니다.
        //dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //dialog?.window?.setDimAmount(0.7f) // 배경 어둡게 설정

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDeviceManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // 이 부분은 필요 없을 수 있습니다. onCreateDialog에서 이미 처리했기 때문입니다.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun getTheme(): Int = R.style.CustomDialogTheme


}