package com.example.ttokjip.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ttokjip.databinding.ItemSearchDeviceBinding

class SearchDeviceAdapter(
    private val deviceList: List<String>,
    private val onDeviceClick: (String) -> Unit
) : RecyclerView.Adapter<SearchDeviceAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding =
            ItemSearchDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val deviceName = deviceList[position]
        holder.bind(deviceName)

        holder.itemView.setOnClickListener {
            onDeviceClick(deviceName)
        }
    }

    override fun getItemCount(): Int = deviceList.size

    // DeviceViewHolder 수정
    class DeviceViewHolder(private val binding: ItemSearchDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(deviceName: String) {
            binding.deviceNameSearch.text = deviceName // 리사이클러뷰의 아이템에 데이터를 설정
        }
    }
}