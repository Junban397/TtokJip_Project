package com.example.ttokjip.data

import com.example.ttokjip.R

data class Device(
    val deviceId: String,
    val houseId: String,
    val deviceType: String,
    val deviceName: String,
    val deviceLocation: String,
    val deviceStatus:Boolean,
    val isFavorite: Boolean
){
    fun getImageResource(): Int {
        return when (deviceType) {
            "cctv" -> R.drawable.icon_cctv
            "tv" -> R.drawable.icon_tv
            else -> R.drawable.icon_ttokjib
        }
    }
}
