package com.example.ttokjip.data

import com.example.ttokjip.R

data class Device(
    val deviceId: String,
    val houseId: String,
    var deviceType: String,
    var sensorName:String,
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
data class AddDevice(
    val deviceId: String,
    var sensorName:String,
    var deviceName: String,
    var deviceType: String,
    var deviceLocation: String,
    var deviceStatus:Boolean,
    var isFavorite: Boolean
)
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

data class SensorDataRequest(
    val date: String,
    val temperature: Float,
    val humidity: Float,
    val totalWattage: Float
)
data class GetLogDate(
    val date:String
)
data class StatisticsResponse(
    val weeklyData: List<WeeklyData>,      // 예: 일주일간의 온도 및 습도 변화
    val monthlyTotalWattage: Float,        // 이번 달 총 전력량
    val lastMonthTotalWattage: Float,      // 저번 달 총 전력량
    val averageMonthlyWattage: Float            // 평균 전력량
)

data class WeeklyData(
    val date: String,                      // 날짜
    val temperature: Float,                // 온도
    val humidity: Float                    // 습도
)