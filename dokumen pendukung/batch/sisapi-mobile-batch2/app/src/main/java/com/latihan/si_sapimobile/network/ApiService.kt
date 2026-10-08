package com.latihan.si_sapimobile.network

import com.latihan.si_sapimobile.models.LivestockProfile
import com.latihan.si_sapimobile.models.LoginRequest
import com.latihan.si_sapimobile.models.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Backend mengembalikan JSON mentah (tidak dibungkus success/data).
interface ApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<ResponseBody>

    // Batch 2
    @GET("livestocks/{tagNumber}")
    suspend fun getLivestockByTag(@Path("tagNumber") tagNumber: String): Response<LivestockProfile>
}
