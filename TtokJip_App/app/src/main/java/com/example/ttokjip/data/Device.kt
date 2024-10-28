package com.example.ttokjip.data

data class Device(
    val deviceId: String,
    val houseId: String,
    val deviceType: String,
    val deviceName: String,
    val deviceLocation: String,
    val deviceStatus:Boolean,
    val isFavorite: Boolean
)
