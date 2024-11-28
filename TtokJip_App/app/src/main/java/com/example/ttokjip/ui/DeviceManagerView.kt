package com.example.ttokjip.ui

import GridSpacingItemDecoration
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ttokjip.R
import com.example.ttokjip.adapter.DeviceAdapter
import com.example.ttokjip.data.Device
import com.example.ttokjip.databinding.FragmentDeviceManagerBinding
import com.example.ttokjip.viewmodel.DeviceViewModel
import com.example.ttokjip.viewmodel.FilterType
import kotlinx.coroutines.launch
import io.socket.client.Socket

class DeviceManagerView : BaseDeviceManger() {
    private lateinit var adapter: DeviceAdapter
    private var _binding: FragmentDeviceManagerBinding? = null
    private val binding get() = _binding!!
    private var selectedButton: Button? = null
    private var nowFilter = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceManagerBinding.inflate(inflater, container, false)
        setupViewModel()
        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        // 토큰이 null이 아니면 디바이스 목록을 가져오기
        if (token != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    deviceViewModel.fetchDevices(token)
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
        }

        setupRecyclerView(token!!)
        binding.addDeviceBtn.setOnClickListener {
            val intent = Intent(requireContext(), SearchDevice::class.java)
            startActivity(intent)
        }

        // 기본 필터 설정 및 위치 버튼 추가
        return binding.root
    }

    private fun setupRecyclerView(token: String) {
        val gridLayoutManager = GridLayoutManager(context, 2)
        adapter = DeviceAdapter(
            onDeviceClick = { deviceId ->
                // 코루틴 내에서 suspend 함수 호출
                viewLifecycleOwner.lifecycleScope.launch {
                    deviceViewModel.deviceSwitch(deviceId, token!!)
                }
            },
            onFavoriteClick = { deviceId ->
                // 코루틴 내에서 호출
                viewLifecycleOwner.lifecycleScope.launch {
                    deviceViewModel.deviceFavoriteSwitch(deviceId,token!!)
                }
            },
            onLongClick = { device -> showDeviceDialog(device) }
        )

        binding.filterDeviceRecyclerview.apply {
            layoutManager = gridLayoutManager
            adapter = this@DeviceManagerView.adapter
            addItemDecoration(GridSpacingItemDecoration(32))

        }
    }

    private fun setupViewModel() {
        deviceViewModel = ViewModelProvider(this).get(DeviceViewModel::class.java)
        deviceViewModel.deviceList.observe(viewLifecycleOwner) { devices ->
            setLocationButtons()
            if (nowFilter == "") {
                adapter.submitList(devices)
            }


        }
        deviceViewModel.filteredDeviceList.observe(viewLifecycleOwner) { filteredDevices ->
            if (nowFilter.isNotEmpty()) {
                adapter.submitList(filteredDevices)
                if (nowFilter == "모든 기기") {
                    applyFilter(FilterType.ALL, null)
                } else {
                    applyFilter(FilterType.LOCATION, nowFilter)
                }
            }
        }
    }

    private fun applyFilter(filterType: FilterType, location: String?) {
        deviceViewModel.applyFilter(filterType, location)

    }

    private fun addLocationButton(text: String): Button {
        return Button(requireContext()).apply {
            this.text = text

            textSize = 18f
            setPadding(20, 5, 20, 5)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setBackgroundResource(android.R.color.transparent)
            typeface = ResourcesCompat.getFont(context, R.font.a1009_1)
            setOnClickListener {
                if (text == "모든 기기") {
                    applyFilter(FilterType.ALL, null)
                    nowFilter = text
                } else {
                    applyFilter(FilterType.LOCATION, text)
                    nowFilter = text
                }
                updateSelectedButton(this)
            }

            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }

            binding.buttonLocation.addView(this)
        }
    }

    // 버튼 상태 갱신 및 초기화
    private fun setLocationButtons() {
        val devices = deviceViewModel.deviceList.value ?: return

        // 버튼 상태 초기화 (리스트가 변경될 때마다 버튼 상태를 갱신하지 않도록 주의)
        if (nowFilter.isNotEmpty()) return // 이미 필터가 적용된 상태라면 초기화 안함

        binding.buttonLocation.removeAllViews()

        val uniqueLocations = devices.map(Device::deviceLocation).distinct()

        // 필터 상태에 맞는 버튼 선택 상태 유지
        addLocationButton("모든 기기").apply {
            if (nowFilter == "모든 기기") {
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.google.android.material.R.color.material_grey_50
                    )
                )
                selectedButton = this
            }else if(nowFilter==""){
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.google.android.material.R.color.material_grey_50
                    )
                )
                selectedButton = this
            }
        }

        uniqueLocations.forEach { location ->
            addLocationButton(location).apply {
                if (nowFilter == location) {
                    setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            com.google.android.material.R.color.material_grey_50
                        )
                    )
                    selectedButton = this
                }
            }
        }
    }

    private fun updateSelectedButton(selected: Button) {
        selectedButton?.apply {
            setBackgroundResource(android.R.color.transparent)
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        selected.apply {
            setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    com.google.android.material.R.color.material_grey_50
                )
            )
        }

        selectedButton = selected
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}