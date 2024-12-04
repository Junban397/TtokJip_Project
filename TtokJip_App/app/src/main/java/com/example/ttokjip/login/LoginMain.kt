package com.example.ttokjip.login

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ttokjip.BuildConfig.SERVER_URL
import com.example.ttokjip.databinding.ActivityLoginMainBinding
import com.example.ttokjip.ui.CenterView
import com.android.volley.VolleyError

class LoginMain : AppCompatActivity() {
    private lateinit var binding: ActivityLoginMainBinding
    private val sharedPreferences by lazy { getSharedPreferences("userPreferences", MODE_PRIVATE) }
    private val url = "$SERVER_URL/auth/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 권한 요청
        requestNotificationPermission()


        // 이미 로그인된 사용자 확인 후 메인 화면으로 이동
        if (isUserLoggedIn()) {
            navigateToCenterView()
        }

        // 로그인 버튼 클릭 이벤트
        binding.loginBtn.setOnClickListener {
            val userId = binding.setId.text.toString().trim()
            val pw = binding.setPw.text.toString().trim()
            if (userId.isNotEmpty()) {
                login(userId, pw)
            } else {
                Toast.makeText(this, "ID를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**사용자 로그인 상태 확인
     * SharedPreferences에 저장된 houseId와 token이 있는지 확인하여 이미 로그인된 사용자라면 true를 반환
     **/
    private fun isUserLoggedIn(): Boolean {
        val houseId = sharedPreferences.getString("houseId", null)
        val token = sharedPreferences.getString("token", null)
        return !houseId.isNullOrEmpty() && !token.isNullOrEmpty()
    }

    /** 서버로 로그인 요청 보내기
     * 사용자가 입력한 userId와 pw를 JSON 객체로 서버에 전송하여 로그인 요청을 처리합니다.
     **/
    private fun login(userId: String, pw: String) {
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("userId", userId)
            put("pw", pw)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            Response.Listener { response ->
                Log.d("LoginMain", "로그인 성공 응답: $response")  // 성공 응답 로그 추가
                handleLoginSuccess(response, userId)
            },
            Response.ErrorListener { error ->
                val errorMsg = if (error.networkResponse != null && error.networkResponse.data != null) {
                    String(error.networkResponse.data, Charsets.UTF_8)  // UTF-8로 변환해 에러 메시지를 읽음
                } else {
                    "로그인 실패"
                }
                Log.e("LoginMain", "로그인 오류: $errorMsg")  // 오류 응답 로그 추가
                handleLoginError(error)
            }
        )

        Log.d("LoginMain", "로그인 요청 전송: $jsonObject")  // 요청 전송 로그 추가
        requestQueue.add(jsonObjectRequest)
    }

    /** 로그인 성공 처리
     * 로그인 성공 시 서버에서 받은 응답을 SharedPreferences에 저장하고, 메인 화면으로 이동합니다.
     **/
    private fun handleLoginSuccess(response: JSONObject, userId: String) {
        val message = response.optString("message", "로그인 성공!")
        saveUserCredentials(
            userId = userId,
            houseId = response.optString("houseId"),
            token = response.optString("token")
        )
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        navigateToCenterView()
    }

    /** 사용자 자격 증명 저장
     * SharedPreferences에 사용자 ID, 집 ID, 토큰을 저장하여 로그인 상태를 유지합니다.
     **/
    private fun saveUserCredentials(userId: String, houseId: String?, token: String?) {
        with(sharedPreferences.edit()) {
            putString("userId", userId)
            putString("houseId", houseId)
            putString("token", token)
            apply()
        }
    }

    /** 로그인 오류 처리
     * 서버에서 오류가 반환된 경우, 에러 메시지를 추출하여 Toast 메시지로 보여줍니다.
     **/
    private fun handleLoginError(error: VolleyError) {
        val errorMsg = if (error.networkResponse != null && error.networkResponse.data != null) {
            String(error.networkResponse.data, Charsets.UTF_8)  // UTF-8로 변환해 에러 메시지를 읽음
        } else {
            "로그인 실패"
        }
        Toast.makeText(this, "로그인 실패: ID/PW를 확인해주세요", Toast.LENGTH_SHORT).show()
        Log.e("LoginMain", "Error: ${error.message}")
    }

    /** CenterView로 이동
     * 사용자가 로그인에 성공한 경우 메인 화면인 CenterView로 이동하며 로그인 화면을 종료합니다.
     **/
    private fun navigateToCenterView() {
        startActivity(Intent(this@LoginMain, CenterView::class.java))
        finish()
    }
    // 권한 요청 함수
    private fun requestNotificationPermission() {
        // Android 13 이상에서만 알림 권한 요청
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1 // 요청 코드
                )
            }
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인됨
                Toast.makeText(this, "알림 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 권한이 거부됨
                Toast.makeText(this, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}