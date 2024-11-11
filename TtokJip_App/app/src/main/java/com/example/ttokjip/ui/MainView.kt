package com.example.ttokjip.ui

import GridSpacingItemDecoration
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ttokjip.R
import com.example.ttokjip.adapter.DeviceAdapter
import com.example.ttokjip.data.Device
import com.example.ttokjip.databinding.FragmentMainViewBinding
import com.example.ttokjip.viewmodel.DeviceViewModel
import com.example.ttokjip.viewmodel.FilterType
import kotlinx.coroutines.launch

class MainView : BaseDeviceManger() {
    private lateinit var adapter: DeviceAdapter
    private var _binding: FragmentMainViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainViewBinding.inflate(inflater, container, false)

        setupViewModel()

        // 토큰 가져오기
        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        // 토큰이 null이 아니면 디바이스 목록을 가져오기
        if (token != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                deviceViewModel.fetchDevices(token)
            }
        }

        setupRecyclerView(token!!)
        setupViewModelObservers()

        // 기본적으로 즐겨찾기 필터를 적용하려면, 예를 들어 이곳에서 호출:
        applyFilter(FilterType.FAVORITE)


        binding.modeSettingBtn.setOnClickListener {
            dialogModeSetting()
        }

        return binding.root
    }

    /**
     * RecyclerView, Adapter 연결
     **/
    private fun setupRecyclerView(token: String) {
        adapter = DeviceAdapter(
            onDeviceClick = { deviceId ->
                // 코루틴 내에서 suspend 함수 호출
                viewLifecycleOwner.lifecycleScope.launch {
                    deviceViewModel.deviceSwitch(deviceId, token)
                }
            },
            onFavoriteClick = { deviceId ->
                // 코루틴 내에서 호출
                viewLifecycleOwner.lifecycleScope.launch {
                    deviceViewModel.deviceFavoriteSwitch(deviceId,token)
                }
            },
            onLongClick = { device -> showDeviceDialog(device) }
        )

        binding.favoriteRecyclerview.apply {
            adapter = this@MainView.adapter
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(GridSpacingItemDecoration(32))
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    /**
     * LiveData 설정, RecyclerView 높이 조정
     **/
    private fun setupViewModelObservers() {
        deviceViewModel.deviceList.observe(viewLifecycleOwner) { devices ->
            devices?.let {
                // 필터를 적용한 후 UI 갱신
                val filteredDevices = applyFilter(FilterType.FAVORITE)
                adjustRecyclerViewHeight(filteredDevices.size)  // 필터된 아이템 개수로 높이 조정
            }
        }
    }

    /**
     * RecyclerView 높이 동적 조정
     **/
    private fun adjustRecyclerViewHeight(itemCount: Int) {
        val itemHeight = (180 * resources.displayMetrics.density).toInt()  // 한 아이템의 높이
        val totalHeight = itemHeight * (itemCount / 2 + itemCount % 2)  // 2개씩 배치되므로 높이를 계산

        // RecyclerView 높이 설정
        binding.favoriteRecyclerview.layoutParams =
            binding.favoriteRecyclerview.layoutParams.apply {
                height = totalHeight
            }
    }

    /**
     * 즐겨찾기 필터 적용
     **/
    private fun applyFilter(filterType: FilterType): List<Device> {
        val filteredDevices = when (filterType) {
            FilterType.FAVORITE -> deviceViewModel.deviceList.value?.filter { it.isFavorite } // 즐겨찾기만 필터링
            else -> deviceViewModel.deviceList.value // 다른 필터가 없는 경우 전체 리스트
        } ?: emptyList()  // null일 경우 빈 리스트 반환

        adapter.submitList(filteredDevices)  // 필터링된 디바이스 목록을 Adapter에 제출
        return filteredDevices // 필터링된 리스트 반환
    }

    /**
     * ViewModel 설정
     **/
    private fun setupViewModel() {
        deviceViewModel = ViewModelProvider(this).get(DeviceViewModel::class.java)
    }

    private fun dialogModeSetting(){
            val dialog = ModeSettingDialog()
            dialog.show(parentFragmentManager, "CustomDialog")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}