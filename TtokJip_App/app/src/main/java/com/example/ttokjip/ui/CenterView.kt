package com.example.ttokjip.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ttokjip.R
import com.example.ttokjip.databinding.ActivityCenterViewBinding
import com.example.ttokjip.login.LoginMain  // 로그인 화면으로 이동할 수 있도록 LoginMain 임포트

class CenterView : AppCompatActivity() {
    private lateinit var binding: ActivityCenterViewBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var progressDialog: AlertDialog? = null  // 로딩 다이얼로그


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCenterViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences 객체를 한 번만 생성하여 재사용
        sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE)

        // 로그인 상태 확인
        val token = sharedPreferences.getString("token", null)
        // 토큰이 없으면 로그인 화면으로 이동
        if (token == null) {
            navigateToLogin()
        } else {
            // 로그인 상태라면, MainView Fragment 설정
        }

        // Bluetooth 초기화 및 연결
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "이 기기는 블루투스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 블루투스 활성화 확인
            if (!bluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
            } else {
                // 블루투스가 이미 활성화되었으면 기기 목록을 다이얼로그로 띄운다
                autoConnectBluetooth()
            }
        }

        // 하단 네비게이션 메뉴 선택 처리
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> {
                    setFragmentView(MainView())
                    true
                }
                R.id.menu_deviceManager -> {
                    setFragmentView(DeviceManagerView())
                    true
                }
                R.id.menu_chart -> {
                    setFragmentView(StatisticsView())
                    true
                }
                R.id.menu_setting -> {
                    setFragmentView(SettingView())
                    true
                }
                else -> false
            }
        }
    }



    /** 로그인 화면으로 이동
     * 로그인 화면(LoginMain)으로 이동하고 현재 화면을 종료합니다.
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginMain::class.java)
        startActivity(intent)
        finish()
    }

    /** Fragment 전환 처리
     * 주어진 Fragment를 화면에 표시합니다. 같은 Fragment가 이미 있으면 추가하지 않도록 처리합니다.
     */
    private fun setFragmentView(fragmentId: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentId.javaClass.simpleName)
        if (existingFragment == null) {
            fragmentTransaction.replace(R.id.center_frame, fragmentId, fragmentId.javaClass.simpleName)
        }
        fragmentTransaction.commit()
    }
    /** 이전에 연결된 블루투스 장치 자동 연결 */
    private fun autoConnectBluetooth() {
        val savedDeviceAddress = sharedPreferences.getString("bluetooth_device_address", null)
        if (!savedDeviceAddress.isNullOrEmpty()) {
            val device = bluetoothAdapter?.bondedDevices?.find { it.address == savedDeviceAddress }
            if (device != null) {
                showLoadingDialog()
                connectToBluetoothDevice(device)
                return
            }
        }
        showPairedDevicesDialog()
    }

    /** 블루투스 연결 시도
     * 사용할 BluetoothDevice를 선택하여 연결을 시도합니다.
     */
    private fun showPairedDevicesDialog() {
        val pairedDevices = bluetoothAdapter?.bondedDevices
        if (pairedDevices.isNullOrEmpty()) {
            Toast.makeText(this, "페어링된 블루투스 기기가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceNames = pairedDevices.map { it.name }
        val deviceAddresses = pairedDevices.map { it.address }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("연결할 블루투스 기기 선택")
        builder.setItems(deviceNames.toTypedArray()) { _, which ->
            val selectedDevice = pairedDevices.elementAt(which)
            saveBluetoothDevice(selectedDevice)
            showLoadingDialog()
            connectToBluetoothDevice(selectedDevice)
        }
        builder.setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    /** 블루투스 연결 시도
     * 선택된 BluetoothDevice에 연결을 시도합니다.
     */
    private fun connectToBluetoothDevice(device: BluetoothDevice) {
        // 블루투스 연결 시도 (BluetoothManager에서 연결 처리)
        BluetoothManager.connectToDevice(device, this) { isConnected ->
            dismissLoadingDialog()  // 연결 실패 시 로딩 다이얼로그 종료
            if (isConnected) {
                setFragmentView(MainView())  // MainView로 이동
            } else {
                setFragmentView(MainView())  // MainView로 이동
//                Toast.makeText(this, "블루투스 연결 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }
    /** 블루투스 장치 정보 저장 */
    private fun saveBluetoothDevice(device: BluetoothDevice) {
        val editor = sharedPreferences.edit()
        editor.putString("bluetooth_device_address", device.address)
        editor.apply()
    }
    // 로딩 다이얼로그 표시
    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("네트워크 연결중...")
        builder.setCancelable(false)  // 클릭 불가
        progressDialog = builder.create()
        progressDialog?.show()
    }

    // 로딩 다이얼로그 숨기기
    private fun dismissLoadingDialog() {
        progressDialog?.dismiss()
    }
}