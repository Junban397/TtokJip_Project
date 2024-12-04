package com.example.ttokjip.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.ttokjip.data.NewPw
import com.example.ttokjip.data.UserInfo
import com.example.ttokjip.databinding.ActivityMenuMyPageBinding
import com.example.ttokjip.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenuMyPage : AppCompatActivity() {
    private lateinit var binding: ActivityMenuMyPageBinding
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuMyPageBinding.inflate(layoutInflater)

        // 토큰 가져오기
        val sharedPreferences =
            this.getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""

        fetchUserInfo()
        binding.updatePwBtn.setOnClickListener {
            apiNewPw()
        }
        binding.confirmBtn.setOnClickListener {
            finish()
        }
        setContentView(binding.root)
    }

    private fun fetchUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Retrofit 호출
                val response = RetrofitClient.apiService.getUserInfo("Bearer $token")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        // 배열로 반환된 사용자 정보 리스트 처리
                        val userInfoList: List<UserInfo> = response.body()!!

                        if (userInfoList.isNotEmpty()) {
                            // 첫 번째 사용자 정보 가져오기
                            val userInfo = userInfoList[0]
                            updateUI(userInfo)
                        } else {
                            Toast.makeText(this@MenuMyPage, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(this@MenuMyPage, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MenuMyPage, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                    Log.e("fetchUserInfo", "사용자 정보 로드 실패: ${e.message}")
                }
            }
        }
    }

    private fun updateUI(userInfo: UserInfo) {
        binding.textUserId.text = "${userInfo.userId}"
        binding.textUserName.text = "${userInfo.UserName}"
        binding.textPhoneNumber.text = "${userInfo.PhoneNu}"
        binding.textHouseName.text = "${userInfo.houseId}"
    }

    private fun apiNewPw() {
        // 입력된 비밀번호 가져오기
        val newPassword = binding.textPw.text.toString()

        // 비밀번호 입력 값 검증
        if (newPassword.isBlank()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response =
                    RetrofitClient.apiService.changePw(NewPw(newPassword), "Bearer $token")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // 성공 시
                        Toast.makeText(
                            this@MenuMyPage,
                            "비밀번호가 성공적으로 변경되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 1500)
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "비밀번호 변경에 실패했습니다."
                        Toast.makeText(this@MenuMyPage, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MenuMyPage, "서버 오류: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}