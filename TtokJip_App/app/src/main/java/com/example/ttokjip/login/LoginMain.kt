package com.example.ttokjip.login

import android.content.Intent
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.volley.VolleyError
import com.example.ttokjip.BuildConfig
import com.example.ttokjip.BuildConfig.SERVER_URL
import com.example.ttokjip.databinding.ActivityLoginMainBinding
import com.example.ttokjip.ui.CenterView
var url=BuildConfig.SERVER_URL+"/login"
class LoginMain : AppCompatActivity() {
    private lateinit var binding: ActivityLoginMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            val userId = binding.setId.text.toString().trim()
            val pw = binding.setPw.text.toString().trim() // 비밀번호 입력 받기
            if (userId.isNotEmpty()) {
                login(userId,pw)
            } else {
                Toast.makeText(this, "ID를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(userId: String, pw:String) {
        //val urla="12aa"
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val jsonObject = JSONObject()
        jsonObject.put("userId", userId)
        jsonObject.put("pw", pw)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            Response.Listener { response ->
                // 로그인 성공 시
                val message = response.optString("message", "로그인 성공!")
                var intent=Intent(this@LoginMain, CenterView::class.java)
                startActivity(intent)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            },
            Response.ErrorListener { error ->
                // 로그인 실패 시, 에러 메시지 출력
                val errorMsg = error.message ?: "로그인 실패"
                Toast.makeText(this, "로그인 실패: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }
}