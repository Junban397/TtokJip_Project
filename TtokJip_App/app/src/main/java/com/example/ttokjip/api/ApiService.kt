package com.example.ttokjip.network

import com.example.ttokjip.data.Device
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @PUT("devices/updateStatus")
    suspend fun updateDeviceStatus(
        @Path("deviceId") deviceId: String,
        @Body status: Boolean
    ): Response<Unit>

    @GET("devices")
    suspend fun getDevices(@Header("Authorization") token: String): Response<List<Device>>
}