package com.example.ttokjip.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.ttokjip.data.ModeSetting

class ModeSettingDiffCallBack: DiffUtil.ItemCallback<ModeSetting>() {
    override fun areItemsTheSame(oldItem: ModeSetting, newItem: ModeSetting): Boolean {
        return oldItem.deviceId == newItem.deviceId // 각 항목의 고유 id를 기준으로 비교합니다.
    }

    override fun areContentsTheSame(oldItem: ModeSetting, newItem: ModeSetting): Boolean {
        return oldItem == newItem // 전체 내용을 비교하여 동일 여부를 반환합니다.
    }
}