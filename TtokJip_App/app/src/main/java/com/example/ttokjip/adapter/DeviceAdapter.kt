package com.example.ttokjip.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ttokjip.R
import com.example.ttokjip.data.Device
import com.example.ttokjip.databinding.ItemDeviceManagerBinding

class DeviceAdapter(
    private val onDeviceClick: (String) -> Unit,
    private val onFavoriteClick:(String) -> Unit) :
    ListAdapter<Device, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    class DeviceViewHolder(private val binding: ItemDeviceManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device, onDeviceClick: (String) -> Unit, onFavoriteClick: (String) -> Unit) {
            binding.device = device
            binding.deviceImage.setImageResource(
                when (device.deviceType) {
                    "cctv" -> R.drawable.icon_cctv
                    "tv" -> R.drawable.icon_tv
                    else -> R.drawable.icon_ttokjib
                }
            )

            // 상태 UI 설정
            binding.deviceStatus.setBackgroundResource(
                if (device.deviceStatus) R.drawable.rounded_border_device_status_true
                else R.drawable.rounded_border
            )

            // 즐겨찾기 이미지 설정
            binding.favoriteImage.setImageResource(
                if (device.isFavorite) R.drawable.baseline_star_24
                else R.drawable.book_mark
            )

            // 클릭 리스너 추가
            binding.root.setOnClickListener {
                onDeviceClick(device.deviceId) // 클릭 시 deviceId 전달
            }
            binding.favoriteImage.setOnClickListener{
                onFavoriteClick(device.deviceId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDeviceManagerBinding.inflate(inflater, parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position), onDeviceClick, onFavoriteClick) // deviceId를 전달
    }
}