package com.example.ttokjip.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ttokjip.data.Device
import com.example.ttokjip.data.IsFavoriteRequest
import com.example.ttokjip.data.StatusRequest
import com.example.ttokjip.network.RetrofitClient
import retrofit2.HttpException

class DeviceViewModel : ViewModel() {
    private val _deviceList = MutableLiveData<List<Device>>(emptyList())
    val deviceList: LiveData<List<Device>> get() = _deviceList

    private val _filteredDeviceList = MutableLiveData<List<Device>>(emptyList())
    val filteredDeviceList: LiveData<List<Device>> get() = _filteredDeviceList

    // 서버에서 디바이스 목록 가져오기
    suspend fun fetchDevices(token: String) {
        try {
            val response = RetrofitClient.apiService.getDevices("Bearer $token")

            if (response.isSuccessful) {
                val devices = response.body() ?: emptyList()
                _deviceList.postValue(devices)
                _filteredDeviceList.postValue(devices) // 처음에는 모든 데이터로 설정
            }
        } catch (e: Exception) {
            // 오류 처리
        }
    }

    // 필터 적용
    fun applyFilter(filterType: FilterType, location: String?) {
        val filteredList = when (filterType) {
            FilterType.ALL -> _deviceList.value ?: emptyList()
            FilterType.LOCATION -> _deviceList.value?.filter { it.deviceLocation == location } ?: emptyList()
            else -> _deviceList.value ?: emptyList()
        }
        _filteredDeviceList.postValue(filteredList)
    }

    // device 상태 전환
    suspend fun deviceSwitch(deviceId: String, token: String) {
        try {
            val device = _deviceList.value?.find { it.deviceId == deviceId }
            if (device == null) return

            val newStatus = device.deviceStatus?.not() ?: return
            val statusRequest = StatusRequest(deviceId, newStatus)

            val response = RetrofitClient.apiService.updateDeviceStatus(deviceId, statusRequest, "Bearer $token")

            if (response.isSuccessful) {
                fetchDevices(token)  // 디바이스 목록 갱신
            }
        } catch (e: Exception) {
            // 오류 처리
        }
    }

    // 디바이스 즐겨찾기 상태 변경
    suspend fun deviceFavoriteSwitch(deviceId: String,token: String) {
        try{
            val device = _deviceList.value?.find{it.deviceId==deviceId}
            if (device==null) return

            val newIsFavorite=device.isFavorite?.not() ?: return
            val isFavoriteRequest= IsFavoriteRequest(deviceId, newIsFavorite)

            val response=RetrofitClient.apiService.updateDeviceFavorite(deviceId,isFavoriteRequest,"Bearer $token")
            if (response.isSuccessful) {
                fetchDevices(token)  // 디바이스 목록 갱신
            }

        }catch (e: Exception) {
            // 오류 처리
        }
    }
}

enum class FilterType {
    ALL,
    FAVORITE,
    LOCATION
}