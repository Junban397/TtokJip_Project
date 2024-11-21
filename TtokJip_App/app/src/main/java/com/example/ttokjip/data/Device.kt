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

data class ModeSetting(
    val houseId: String,
    val deviceId:String,
    val deviceName:String,
    val deviceLocation:String,
    val mode:String,
    val modeStatus:Boolean
)

data class ModeRequest(
    val houseId: String,
    val deviceId: String,
    val mode: String,
    val newStatus: Boolean
)

data class UpdateModeRequest(
    val mode: String
)

data class SensorData(
    val temperature: Float,
    val humidity: Float
)