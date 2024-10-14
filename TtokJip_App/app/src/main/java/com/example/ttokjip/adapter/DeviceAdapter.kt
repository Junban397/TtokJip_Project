package com.example.ttokjip.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ttokjip.R
import com.example.ttokjip.data.Device
import com.example.ttokjip.databinding.ItemDeviceManagerBinding

class DeviceAdapter : ListAdapter<Device, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    class DeviceViewHolder(private val binding: ItemDeviceManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device) {
            binding.device = device // 데이터 바인딩 사용
            binding.deviceImage.setImageResource(
                when (device.DeviceType) {
                    "cctv" -> R.drawable.icon_cctv
                    "tv" -> R.drawable.icon_tv
                    else -> R.drawable.icon_ttokjib
                }

            )
            if (device.isFavorite) {
                binding.favoriteImage.setImageResource(R.drawable.baseline_star_24)
            }else
                binding.favoriteImage.setImageResource(R.drawable.book_mark)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDeviceManagerBinding.inflate(inflater, parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}