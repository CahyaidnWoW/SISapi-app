package com.latihan.si_sapi.models

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String?,
    @SerializedName("access_token")
    val accessToken: String?,
    val user: User?
)