package com.example.ttokjip.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ttokjip.R
import com.example.ttokjip.databinding.ActivityCenterViewBinding
import com.example.ttokjip.login.LoginMain  // 로그인 화면으로 이동할 수 있도록 LoginMain 임포트

class CenterView : AppCompatActivity() {
    private lateinit var binding: ActivityCenterViewBinding
    private lateinit var sharedPreferences: SharedPreferences

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
            setFragmentView(MainView())
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

        // 로그아웃 버튼 클릭 시
        binding.logout.setOnClickListener {
            logout()
        }
    }

    /** 로그아웃 처리
     * SharedPreferences에서 로그인 정보를 삭제하고, 로그인 화면으로 이동합니다.
     */
    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        navigateToLogin()
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
}