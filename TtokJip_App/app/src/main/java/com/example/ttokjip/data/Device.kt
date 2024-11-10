package com.example.ttokjip.data

import com.example.ttokjip.R

data class Device(
    val deviceId: String,
    val houseId: String,
    var deviceType: String,
    var deviceName: String,
    var deviceLocation: String,
    var deviceStatus:Boolean,
    var isFavorite: Boolean
){
    fun getImageResource(): Int {
        return when (deviceType) {
            "cctv" -> R.drawable.icon_cctv
            "tv" -> R.drawable.icon_tv
            else -> R.drawable.icon_ttokjib
        }
    }
}
data class StatusRequest(
    val deviceId: String,
    val status: Boolean
)
data class IsFavoriteRequest(
    val deviceId: String,
    val isFavorite: Boolean
)