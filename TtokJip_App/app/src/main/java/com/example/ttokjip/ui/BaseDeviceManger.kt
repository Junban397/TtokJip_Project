package com.example.ttokjip.ui

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ttokjip.data.Device
import com.example.ttokjip.viewmodel.DeviceViewModel
import com.example.ttokjip.viewmodel.FilterType

open class BaseDeviceManger : Fragment() {
    protected lateinit var deviceViewModel: DeviceViewModel


    // Device Info Dialog 출력
    protected fun showDeviceDialog(device: Device) {
        val dialog = DeviceInfoDialog(device)
        dialog.show(parentFragmentManager, "CustomDialog")
    }



}