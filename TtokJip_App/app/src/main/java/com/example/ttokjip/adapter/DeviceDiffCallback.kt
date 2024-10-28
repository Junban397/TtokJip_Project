package com.example.ttokjip.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.ttokjip.data.Device

class DeviceDiffCallback : DiffUtil.ItemCallback<Device>() {
    override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem.deviceId == newItem.deviceId // ID로 항목 비교
    }

    override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem == newItem // 내용 비교
    }
}