package com.example.ttokjip.network

import com.example.ttokjip.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER_URL)  // 서버 URL을 BuildConfig에서 가져옵니다.
        .addConverterFactory(GsonConverterFactory.create())  // GsonConverter를 사용하여 JSON을 객체로 변환
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}