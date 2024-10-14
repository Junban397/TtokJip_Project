package com.example.ttokjip.ui

import GridSpacingItemDecoration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ttokjip.adapter.DeviceAdapter
import com.example.ttokjip.databinding.FragmentMainViewBinding
import com.example.ttokjip.viewmodel.DeviceViewModel
import com.example.ttokjip.viewmodel.FilterType

class MainView : Fragment() {
    private lateinit var adapter: DeviceAdapter
    private lateinit var deviceViewModel: DeviceViewModel
    private var _binding: FragmentMainViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainViewBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupViewModelObservers()
        val houseId = "house1"
        deviceViewModel.houseIdFilterDevices(houseId)
        applyFilter(FilterType.FAVORITE, "")

        return binding.root
    }

    /**
     * RecyclerView 초기 설정 및 Adapter 연결
     */
    private fun setupRecyclerView() {
        adapter = DeviceAdapter()
        binding.favoriteRecyclerview.apply {
            adapter = this@MainView.adapter
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(GridSpacingItemDecoration(32))
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    /**
     * ViewModel 관찰 설정 및 RecyclerView 높이 조정
     */
    private fun setupViewModelObservers() {
        deviceViewModel = ViewModelProvider(this).get(DeviceViewModel::class.java)
        deviceViewModel.deviceList.observe(viewLifecycleOwner) { devices ->
            devices?.let { // devices가 null이 아닐 때만 실행
                adapter.submitList(it) // RecyclerView에 장치 목록 제출
                adjustRecyclerViewHeight(it.size) // RecyclerView 높이 조정
            }
        }
    }

    /**
     * RecyclerView 높이 동적 조정
     */
    private fun adjustRecyclerViewHeight(itemCount: Int) {
        val itemHeight = (180 * resources.displayMetrics.density).toInt() // 아이템 높이 (dp -> pixels 변환)
        val totalHeight = itemHeight * (itemCount / 2 + itemCount % 2) // 총 높이 계산 (2개씩 배치)

        // RecyclerView 높이 설정
        binding.favoriteRecyclerview.layoutParams = binding.favoriteRecyclerview.layoutParams.apply {
            height = totalHeight
        }
    }

    /**
     * 필터 적용 메서드
     */
    private fun applyFilter(filterType: FilterType, location: String?) {
        deviceViewModel.filterDevices(filterType, location)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}