package com.example.ttokjip.ui

import GridSpacingItemDecoration
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ttokjip.R
import com.example.ttokjip.adapter.DeviceAdapter
import com.example.ttokjip.data.Device
import com.example.ttokjip.databinding.FragmentDeviceManagerBinding
import com.example.ttokjip.viewmodel.DeviceViewModel
import com.example.ttokjip.viewmodel.FilterType

class DeviceManagerView : Fragment() {
    private lateinit var adapter: DeviceAdapter
    private lateinit var deviceViewModel: DeviceViewModel
    private var _binding: FragmentDeviceManagerBinding? = null
    private val binding get() = _binding!!
    private var selectedButton: Button? = null
    private var saveDeviceList: List<Device> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceManagerBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupViewModel()

        // 기본 필터 설정 및 위치 버튼 추가
        val houseId = "house1"
        deviceViewModel.houseIdFilterDevices(houseId)
        setLocationButtons(houseId)

        return binding.root
    }

    /**
        RecyclerView, Adapter 연결
     **/
    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(context, 2)
        adapter = DeviceAdapter(
            onDeviceClick = {deviceId ->
                deviceViewModel.deviceSwitch(deviceId)
            },
            onFavoriteClick = {deviceId ->
                deviceViewModel.deviceFavoriteSwitch(deviceId)
            },onLongClick ={device -> showDeviceDialog(device)}
        )

        binding.filterDeviceRecyclerview.apply {
            layoutManager = gridLayoutManager
            adapter = this@DeviceManagerView.adapter
            addItemDecoration(GridSpacingItemDecoration(32))
        }
    }

    /**
        LiveData 설정
     */
    private fun setupViewModel() {
        deviceViewModel = ViewModelProvider(this).get(DeviceViewModel::class.java)
        deviceViewModel.deviceList.observe(viewLifecycleOwner) { devices ->
            saveDeviceList = devices // 원본 데이터 저장
            adapter.submitList(devices)
        }
    }

    /**
        device 필터
     */
    private fun applyFilter(filterType: FilterType, location: String?) {
        val filteredList = when (filterType) {
            FilterType.ALL -> saveDeviceList
            FilterType.LOCATION -> saveDeviceList.filter { it.deviceLocation == location }
            else->saveDeviceList
        }
        adapter.submitList(filteredList)
    }

    /**
        Location으로 버튼 동적 생성
     */
    private fun addLocationButton(text: String): Button {
        val houseId="house1"
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
                } else {
                    applyFilter(FilterType.LOCATION, text)
                }
                updateSelectedButton(this)
            }

            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8) // 여백 설정
            }

            binding.buttonLocation.addView(this)
        }
    }

    /**
        선택버튼 스타일 변경
     */
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

    /**
        deivceLocation Button생성 함수
     */
    private fun setLocationButtons(houseId: String) {
        val devices = deviceViewModel.deviceList.value ?: return

        // 중복 값 제거
        val uniqueLocations = devices.map(Device::deviceLocation).distinct()

        // 모든 기기 버튼 추가
        addLocationButton("모든 기기").apply {
            selectedButton = this
            setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    com.google.android.material.R.color.material_grey_50
                )
            )
        }

        // 위치별 버튼 추가
        uniqueLocations.forEach { location ->
            addLocationButton(location)
        }
    }
    private fun showDeviceDialog(device: Device){
        val dialog = DeviceInfoDialog(device)
        dialog.show(parentFragmentManager, "CustomDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}