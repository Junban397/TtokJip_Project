package com.example.ttokjip.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ttokjip.data.ModeSetting
import com.example.ttokjip.databinding.ItemDeviceManagerBinding
import com.example.ttokjip.databinding.ItemModeManagerBinding

class ModeSettingAdapter() :
    ListAdapter<ModeSetting,ModeSettingAdapter.ModeSettingViewHolder>(ModeSettingDiffCallBack()) {

    inner class ModeSettingViewHolder(private val binding: ItemModeManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(modeSetting: ModeSetting) {
            binding.modeSetting=modeSetting
            binding.executePendingBindings()
            binding.modeStatusSwitch.isChecked=modeSetting.modeStatus

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeSettingViewHolder {
        val binding =
            ItemModeManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ModeSettingViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ModeSettingViewHolder, position: Int) {
        holder.bind(getItem(position))
        Log.d("ModeSettingAdapter", "Binding item at position: $position")
    }

}
