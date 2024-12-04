package com.example.ttokjip.data

// UserRequest.kt
data class UserRequest(
    val userId: String,
    val houseId: String,
    val userName: String,
    val pw: String,
    val phoneNum: String
)

// UserResponse.kt
data class UserResponse(
    val message: String,
    val user: User? // 사용자 정보를 포함할 경우
)

data class User(
    val userId: String,
    val houseId: String,
    val userName: String,
    val phoneNum: String,
    val admin: Boolean
)
data class UserInfo(
    val _id: String,
    val userId: String,
    val pw: String,
    val UserName: String,
    val PhoneNu: String,
    val Admin: Boolean,
    val houseId: String
)