package com.example.ttokjip.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.ttokjip.data.Device
import com.example.ttokjip.data.IsFavoriteRequest
import com.example.ttokjip.data.ModeRequest
import com.example.ttokjip.data.ModeSetting
import com.example.ttokjip.data.StatusRequest
import com.example.ttokjip.data.UpdateModeRequest
import com.example.ttokjip.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DeviceViewModel : ViewModel() {
    private val _deviceList = MutableLiveData<List<Device>>(emptyList())
    val deviceList: LiveData<List<Device>> get() = _deviceList

    private val _filteredDeviceList = MutableLiveData<List<Device>>(emptyList())
    val filteredDeviceList: LiveData<List<Device>> get() = _filteredDeviceList

    private val _modeSettingList=MutableLiveData<List<ModeSetting>>(emptyList())
    val modeSettingList:LiveData<List<ModeSetting>>get()=_modeSettingList

    // 서버에서 디바이스 목록 가져오기
    suspend fun fetchDevices(token: String) {
        try {
            val response = RetrofitClient.apiService.getDevices("Bearer $token")

            if (response.isSuccessful) {
                val devices = response.body() ?: emptyList()
                _deviceList.postValue(devices)
                _filteredDeviceList.postValue(devices) // 처음에는 모든 데이터로 설정
                for (device in devices){
                    bluetoothStatus(device.sensorName, device.deviceStatus)
                }

                Log.d("ModeSettingaaaaaaaaaaaaaaaaa", "Mode devices fetched: ${devices} devices found")
            }
        } catch (e: Exception) {
            // 오류 처리
        }
    }

    suspend fun fetchModeSetting(token: String, mode:String){
        try{
            //val modeRequest = ModeRequest(mode)  // 모드 요청 객체 생성
            val response = RetrofitClient.apiService.fetchModeSetting(mode,"Bearer $token")

            if (response.isSuccessful) {
                val modeDevices = response.body() ?: emptyList()
                Log.d("ModeSetting", "Mode devices fetched: ${modeDevices.size} devices found")
                val mappedModeDevices = modeDevices.map { modeDevices ->
                    ModeSetting(
                        houseId = modeDevices.houseId,
                        deviceId = modeDevices.deviceId,
                        deviceName = modeDevices.deviceName,
                        deviceLocation = modeDevices.deviceLocation,
                        mode=modeDevices.mode,
                        modeStatus = modeDevices.modeStatus
                    )
                }
                _modeSettingList.postValue(mappedModeDevices)
            }else{
                Log.e("ModeSetting", "Response failed with code: ${response.code()}")
            }
        }catch (e: Exception) {
            Log.e("ModeSetting", "Error fetching mode settings: ${e.message}")
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
            //블루투스 수신
            bluetoothStatus(device.sensorName,newStatus)

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
    suspend fun deviceUpdateModeStatus(mode: String,token: String){
            val modeRequest = UpdateModeRequest(mode)
            val response =
                RetrofitClient.apiService.updateModeDevice(modeRequest, "Bearer $token")
            if (response.isSuccessful) {
                fetchDevices(token)
                Log.d("ModeSetting", "Mode status updated successfully.")
            } else {
                Log.e("ModeSetting", "Failed to update mode status: ${response.code()}")
            }
    }
    private fun bluetoothStatus(sensorName:String, sensorNewStatus:Boolean){

        var messageStatus="$sensorName:$sensorNewStatus "
        Log.e("ModeSetting", "Failed to update mode status: $messageStatus")
        if (BluetoothManager.isBluetoothConnected()) {
            BluetoothManager.sendData(messageStatus)
        } else {
            // 연결되지 않은 경우 처리
        }

    }
}

enum class FilterType {
    ALL,
    FAVORITE,
    LOCATION
}