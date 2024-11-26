import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.IOException

object BluetoothManager {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var connectedThread: ConnectedThread? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private var isConnected = false

    // Bluetooth 초기화 및 권한 확인
    fun init(context: Context): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            (context as? Activity)?.runOnUiThread {
            Toast.makeText(context, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show()
            }
            return false
        }

        if (!bluetoothAdapter!!.isEnabled) {
            (context as? Activity)?.runOnUiThread {
                Toast.makeText(context, "블루투스를 켜주세요.", Toast.LENGTH_SHORT).show()
            }
            return false
        }
        return true
    }

    // Bluetooth 연결
    fun connectToDevice(device: BluetoothDevice, context: Context, callback: (Boolean) -> Unit) {
        bluetoothDevice = device
        val uuid = device.uuids.first().uuid
        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
        Log.w("qwerqweraa", "알 수 없는 데이터 형식: $bluetoothSocket")

        Thread {
            try {
                bluetoothSocket?.connect()
                connectedThread = ConnectedThread(bluetoothSocket!!)
                connectedThread?.start()
                isConnected = true
                // 연결 성공 후 UI 업데이트
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "블루투스 연결 성공", Toast.LENGTH_SHORT).show()
                }
                callback(true) // 연결 성공 시 true 반환
            } catch (e: IOException) {
                e.printStackTrace()
                isConnected = false
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "블루투스 연결 실패", Toast.LENGTH_SHORT).show()
                }
                callback(false) // 연결 실패 시 false 반환
            }
        }.start()
    }

    // 데이터 전송
    fun sendData(message: String) {
        connectedThread?.write(message)
    }

    // 데이터 수신
    fun getReceivedData(): LiveData<String> {
        return connectedThread?.receivedData ?: MutableLiveData()
    }

    // 연결 해제
    fun disconnect() {
        connectedThread?.cancel()
        bluetoothSocket?.close()
        isConnected = false
    }

    // 연결 상태 확인
    fun isBluetoothConnected(): Boolean {
        return isConnected
    }

    // 내부 스레드: Bluetooth 연결 및 데이터 송수신
    private class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream = socket.inputStream
        private val outputStream = socket.outputStream
        val receivedData = MutableLiveData<String>()

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    val receivedMessage = String(buffer, 0, bytes)
                    receivedData.postValue(receivedMessage)
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }

        fun write(message: String) {
            try {
                outputStream.write(message.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}