package com.example.ttokjip.network

import com.example.ttokjip.data.AddDevice
import com.example.ttokjip.data.Device
import com.example.ttokjip.data.GetLogDate
import com.example.ttokjip.data.IsFavoriteRequest
import com.example.ttokjip.data.ModeRequest
import com.example.ttokjip.data.ModeSetting
import com.example.ttokjip.data.NewPw
import com.example.ttokjip.data.SensorDataRequest
import com.example.ttokjip.data.StatisticsResponse
import com.example.ttokjip.data.StatusRequest
import com.example.ttokjip.data.UpdateModeRequest
import com.example.ttokjip.data.UserInfo
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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

    @PUT("/devices/updateFavorite")
    suspend fun updateDeviceFavorite(
        @Query("deviceId") deviceId: String,
        @Body favoriteRequest: IsFavoriteRequest,
        @Header("Authorization") token: String
    ): Response<Device>

    @GET("devices")
    suspend fun getDevices(@Header("Authorization") token: String): Response<List<Device>>

    //모드셋팅 정보
    @GET("/devices/modeSetting")
    suspend fun fetchModeSetting(
        @Query("mode") mode: String,
        @Header("Authorization") token: String
    ): Response<List<ModeSetting>>

    @PUT("/devices/updateModeSetting")
    suspend fun updateModeSetting(
        @Body modeRequest: ModeRequest,
        @Header("Authorization") token: String
    ): Response<ModeSetting>

    @PUT("/devices/updateModeDevice")
    suspend fun updateModeDevice(
        @Body modeRequest: UpdateModeRequest,
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    @POST("/log/uploadData")
    suspend fun uploadSensorData(
        @Body sensorData: SensorDataRequest,
        @Header("Authorization") token: String
    ): Response<ResponseBody>
    @GET("/log/statistics")
    suspend fun getStatistics(
        @Query("date") logDate:String,
        @Header("Authorization") token: String
    ): Response<StatisticsResponse>
    @POST("/devices/addDevice")
    suspend fun addDevice(
        @Body addDeviceInfo: AddDevice,
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    @DELETE("/devices/deleteDevice/{deviceId}")
    suspend fun deleteDevice(
        @Path("deviceId") deviceId: String,
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    @GET("/auth/getUserInfo")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): Response<List<UserInfo>>

    @PUT("/auth/changePw")
    suspend fun changePw(
        @Body newPw: NewPw,
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}