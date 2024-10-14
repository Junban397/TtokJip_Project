package com.example.ttokjip.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ttokjip.data.Device

class DeviceViewModel : ViewModel() {
    private val _deviceList = MutableLiveData<List<Device>>(emptyList())
    val deviceList: LiveData<List<Device>> get() = _deviceList


    private val allDevices = listOf(    //샘플데이터
        Device("1", "house1", "tv", "Living Room Light", "거실", false),
        Device("2", "house1", "tv", "Kitchen Light", "주방", true),
        Device("3", "house1", "cctv", "Entrance CCTV", "입구", false),
        Device("4", "house1", "cctva", "Entrance CCTV", "입구", true),
        Device("5", "house1", "cctvb", "Entrance CCTV", "입구", true),
        Device("6", "house1", "tv", "Entrance CCTV", "입구", true),
        Device("7", "house1", "tv", "Entrance CCTV", "입구", true),
        Device("8", "house1", "cctvc", "Entrance CCTV", "입구", true),
        Device("9", "house1", "cctvc", "Entrance CCTV", "입구", true),
        Device("10", "house2", "tv", "Living Room TV", "거실", true)
    )
    fun houseIdFilterDevices(houseId: String){
        _deviceList.value = allDevices.filter { it.houseId == houseId }
    }

    fun filterDevices(filterType: FilterType, location: String?) {
        _deviceList.value = when (filterType) {
            FilterType.ALL -> _deviceList.value // 현재 기기 리스트 반환
            FilterType.FAVORITE -> _deviceList.value?.filter { it.isFavorite }
            FilterType.LOCATION -> _deviceList.value?.filter { it.DeviceLocation == location }
            //else -> _deviceList.value // 기본값으로 현재 리스트 반환
        }
    }

    fun getDevicesByHouseId(houseId: String): List<Device> {
        return allDevices.filter { it.houseId == houseId }
    }

}

enum class FilterType {
    ALL,
    FAVORITE,
    LOCATION
}

