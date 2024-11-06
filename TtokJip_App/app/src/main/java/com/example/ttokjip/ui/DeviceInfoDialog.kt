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
        val dialog = super.onCreateDialog(savedInstanceState)


        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDeviceManagementBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deviceTypeDialog.setText(device.deviceType)
        binding.deviceNameDialog.setText(device.deviceName)
        binding.deviceLocDialog.setText(device.deviceLocation)
        binding.deviceImgDialog.setImageResource(device.getImageResource())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun getTheme(): Int = R.style.CustomDialogTheme


}