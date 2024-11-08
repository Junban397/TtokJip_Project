package com.example.ttokjip.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ttokjip.data.Device
import com.example.ttokjip.data.StatusRequest
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

    suspend fun deviceSwitch(deviceId: String, token: String) {
        try {
            val device = _deviceList.value?.find { it.deviceId == deviceId }
            if (device == null) {
                return  // 디바이스가 없으면 더 이상 진행하지 않음
            }

            val newStatus = device.deviceStatus?.not() ?: return  // 상태 반전

            // deviceId와 상태를 함께 보내는 요청 데이터 생성
            val statusRequest = StatusRequest(deviceId, newStatus)

            val response = RetrofitClient.apiService.updateDeviceStatus(deviceId, statusRequest, "Bearer $token")

            if (response.isSuccessful) {
                fetchDevices(token)  // 디바이스 목록을 갱신
            }
        } catch (e: Exception) {
            // 오류 처리
        }
    }

    // 디바이스 즐겨찾기 상태 변경
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