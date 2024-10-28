package com.example.ttokjip.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ttokjip.data.Device

class DeviceViewModel : ViewModel() {
    private val _deviceList = MutableLiveData<List<Device>>(emptyList())
    val deviceList: LiveData<List<Device>> get() = _deviceList


    private val allDevices = listOf(    //샘플데이터
        Device("1", "house1", "tv", "Living Room Light", "거실", false, true),
        Device("2", "house1", "tv", "Kitchen Light", "주방", true, true),
        Device("3", "house1", "cctv", "Entrance CCTV", "입구", false, true),
        Device("4", "house1", "cctva", "Entrance CCTV", "입구", true, false),
        Device("5", "house1", "cctvb", "Entrance CCTV", "입구", true, true),
        Device("6", "house1", "tv", "Entrance CCTV", "입구", true, true),
        Device("7", "house1", "tv", "Entrance CCTV", "입구", true, true),
        Device("8", "house1", "cctvc", "Entrance CCTV", "입구", true, false),
        Device("9", "house1", "cctvc", "Entrance CCTV", "입구", true, false),
        Device("10", "house2", "tv", "Living Room TV", "거실", true, false)
    )

    fun houseIdFilterDevices(houseId: String) {
        _deviceList.value = allDevices.filter { it.houseId == houseId }
    }

    fun filterDevices(filterType: FilterType, location: String?) {
        _deviceList.value = when (filterType) {
            FilterType.ALL -> _deviceList.value
            FilterType.FAVORITE -> _deviceList.value?.filter { it.isFavorite }
            FilterType.LOCATION -> _deviceList.value?.filter { it.deviceLocation == location }
        }
    }

    fun getDevicesByHouseId(houseId: String): List<Device> {
        return allDevices.filter { it.houseId == houseId }
    }
    // Device의 Status 변경 메서드
    fun deviceSwitch(deviceId: String) {
        _deviceList.value = _deviceList.value?.map { device ->
            if (device.deviceId == deviceId) {
                device.copy(deviceStatus = !device.deviceStatus)
            } else {
                device
            }
        }
    }

    fun deviceFavoriteSwitch(deviceId: String){
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

