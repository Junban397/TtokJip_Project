package com.example.ttokjip.network

import com.example.ttokjip.data.Device
import com.example.ttokjip.data.StatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @PUT("/devices/updateStatus")
    suspend fun updateDeviceStatus(
        @Query("deviceId") deviceId: String,
        @Body statusRequest: StatusRequest,
        @Header("Authorization") token: String
    ): Response<Device>

    @GET("devices")
    suspend fun getDevices(@Header("Authorization") token: String): Response<List<Device>>
}