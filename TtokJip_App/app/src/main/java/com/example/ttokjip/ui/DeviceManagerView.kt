package com.example.ttokjip.ui

import GridSpacingItemDecoration
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
     * RecyclerView 초기 설정 및 Adapter 연결
     */
    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(context, 2)
        adapter = DeviceAdapter()

        binding.filterDeviceRecyclerview.apply {
            layoutManager = gridLayoutManager
            adapter = this@DeviceManagerView.adapter
            addItemDecoration(GridSpacingItemDecoration(32))
        }
    }

    /**
     * ViewModel 초기화 및 데이터 관찰
     */
    private fun setupViewModel() {
        deviceViewModel = ViewModelProvider(this).get(DeviceViewModel::class.java)
        deviceViewModel.deviceList.observe(viewLifecycleOwner) { devices ->
            saveDeviceList = devices // 원본 데이터 저장
            adapter.submitList(devices)
        }
    }

    /**
     * 필터 적용 메서드
     */
    private fun applyFilter(filterType: FilterType, location: String?) {
        val filteredList = when (filterType) {
            FilterType.ALL -> saveDeviceList // 모든 기기
            FilterType.LOCATION -> saveDeviceList.filter { it.DeviceLocation == location } // 특정 위치
            else->saveDeviceList
        }
        adapter.submitList(filteredList) // 필터링된 리스트로 업데이트
    }

    /**
     * 특정 텍스트로 버튼 생성 및 추가
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
     * 선택된 버튼을 업데이트하고 이전 버튼의 스타일을 초기화
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
     * 특정 houseId에 해당하는 기기의 위치별로 버튼 생성
     */
    private fun setLocationButtons(houseId: String) {
        val devices = deviceViewModel.deviceList.value ?: return

        // 중복된 위치 제거
        val uniqueLocations = devices.map(Device::DeviceLocation).distinct()

        // 기본 "모든 기기" 버튼 추가
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}