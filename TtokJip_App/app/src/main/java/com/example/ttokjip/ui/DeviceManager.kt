package com.example.ttokjip.ui

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ttokjip.R
import com.example.ttokjip.viewmodel.DeviceManagerViewModel

class DeviceManager : Fragment() {

    companion object {
        fun newInstance() = DeviceManager()
    }

    private val viewModel: DeviceManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_device_manager, container, false)
    }
}