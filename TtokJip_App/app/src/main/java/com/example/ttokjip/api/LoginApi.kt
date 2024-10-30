package com.example.ttokjip.api

import com.example.ttokjip.data.UserRequest
import com.example.ttokjip.data.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/register")
    fun registerUser(@Body userRequest: UserRequest): Call<UserResponse>

    @POST("/login")
    fun loginUser(@Body userRequest: UserRequest): Call<UserResponse>
}