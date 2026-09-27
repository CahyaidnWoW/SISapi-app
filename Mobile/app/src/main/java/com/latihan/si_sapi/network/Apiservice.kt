package com.latihan.si_sapi.network

import com.latihan.si_sapi.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- AUTHENTICATION ---
    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("me")
    suspend fun getProfile(): Response<User>

    @POST("logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    // --- LIVESTOCK & SCAN QR ---
    @GET("livestocks/{tagNumber}")
    suspend fun getLivestockByTag(
        @Path("tagNumber") tagNumber: String
    ): Response<ApiResponse<LivestockProfile>>

    // --- WEIGHT LOG ---
    @POST("livestocks/{tagNumber}/weight")
    suspend fun recordWeight(
        @Path("tagNumber") tagNumber: String,
        @Body request: CreateWeightLogRequest
    ): Response<ApiResponse<WeightLog>>

    // --- CAGE & FEED ---
    @GET("cages")
    suspend fun getCages(): Response<ApiResponse<List<Cage>>>

    @GET("feeds")
    suspend fun getFeeds(): Response<ApiResponse<List<FeedStock>>>

    @POST("cages/{id}/feed")
    suspend fun recordFeedConsumption(
        @Path("id") cageId: Int,
        @Body request: CreateFeedConsumptionRequest
    ): Response<ApiResponse<Unit>>

    // --- HEALTH RECORD & EMERGENCY REPORT (Dukungan Foto) ---
    @Multipart
    @POST("livestocks/{tagNumber}/health-report")
    suspend fun sendHealthReport(
        @Path("tagNumber") tagNumber: String,
        @Part("type") type: RequestBody,
        @Part("diagnosis") diagnosis: RequestBody,
        @Part("treatment") treatment: RequestBody?,
        @Part photo: MultipartBody.Part?
    ): Response<ApiResponse<HealthRecord>>
}