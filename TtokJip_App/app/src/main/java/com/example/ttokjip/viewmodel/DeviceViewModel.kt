package com.example.ttokjip.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ttokjip.data.Device
import com.example.ttokjip.network.RetrofitClient
import retrofit2.HttpException

class DeviceViewModel : ViewModel() {
    private val _deviceList = MutableLiveData<List<Device>>(emptyList())
    val deviceList: LiveData<List<Device>> get() = _deviceList

    // 서버에서 디바이스 목록 가져오기
    suspend fun fetchDevices(token: String) {
        try {
            // 요청에 Authorization 헤더를 추가하여 서버로 보냄
            val response = RetrofitClient.apiService.getDevices("Bearer $token")

            if (response.isSuccessful) {
                _deviceList.postValue(response.body() ?: emptyList())
            }
        } catch (e: HttpException) {
            // 오류 처리
        } catch (e: Exception) {
            // 오류 처리
        }
    }


    // Device의 Status 변경 메서드 (로컬 상태 변경 제거)
    suspend fun deviceSwitch(deviceId: String, token: String) {
        try {
            // 서버로 상태 변경 요청 보내기
            val device = _deviceList.value?.find { it.deviceId == deviceId }
            val newStatus = device?.deviceStatus?.not() ?: return
            Log.d("DeviceViewModel", "디바이스 상태 변경 요청: deviceId = $deviceId, 새로운 상태 = $newStatus")
            // 서버에 상태 변경을 요청하는 함수 호출
            val response = RetrofitClient.apiService.updateDeviceStatus(deviceId, newStatus)
            if (response.isSuccessful) {
                // 서버에서 상태가 변경되면, 다시 최신 디바이스 목록을 가져옴
                fetchDevices(token)  // 디바이스 목록을 새로 가져옴
            } else {
                Log.e("DeviceViewModel", "디바이스 상태 변경 실패")
            }
        } catch (e: Exception) {
            Log.e("DeviceViewModel", "서버 오류: ${e.message}")
        }
    }


    fun deviceFavoriteSwitch(deviceId: String) {
        _deviceList.value = _deviceList.value?.map { device ->
            if (device.deviceId == deviceId) {
                device.copy(isFavorite = !device.isFavorite)
            } else {
                device
            }
        }
    }
}

enum class FilterType {
    ALL,
    FAVORITE,
    LOCATION
}