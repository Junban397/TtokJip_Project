import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class BluetoothData(val temperature: Float, val humidity: Float)

class BluetoothViewModel : ViewModel() {
    private val _bluetoothData = MutableLiveData<BluetoothData>()
    val bluetoothData: LiveData<BluetoothData> get() = _bluetoothData

    fun updateBluetoothData(data: BluetoothData) {
        _bluetoothData.value = data
    }
}