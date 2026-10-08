package com.latihan.si_sapimobile.models

import com.google.gson.annotations.SerializedName

// Bentuk JSON asli dari POST /api/login:
// { "message": "...", "access_token": "...", "token_type": "Bearer",
//   "user": { "id": 1, "name": "...", "email": "...", "role": "manager", "phone": "..." } }
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String? = null,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String? = null,
    val user: User
)
