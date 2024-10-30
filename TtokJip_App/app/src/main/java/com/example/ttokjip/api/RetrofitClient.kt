package com.example.ttokjip.api

import com.example.ttokjip.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {


    const val BASE_URL=BuildConfig.SERVER_URL+"/login"
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getInstance(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}