package com.latihan.si_sapi.network

import com.latihan.si_sapi.models.LoginRequest
import com.latihan.si_sapi.models.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Backend mengembalikan JSON mentah (tidak dibungkus success/data).
// Endpoint lain ditambahkan satu per satu di batch berikutnya.
interface ApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<ResponseBody>
}
