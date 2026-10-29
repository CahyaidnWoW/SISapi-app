package com.latihan.si_sapimobile.network

import com.latihan.si_sapimobile.models.Cage
import com.latihan.si_sapimobile.models.CreateFeedConsumptionRequest
import com.latihan.si_sapimobile.models.CreateWeightLogRequest
import com.latihan.si_sapimobile.models.FeedStock
import com.latihan.si_sapimobile.models.HealthRecord
import com.latihan.si_sapimobile.models.LivestockProfile
import com.latihan.si_sapimobile.models.LoginRequest
import com.latihan.si_sapimobile.models.LoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<ResponseBody>
    @GET("livestocks")
    suspend fun getLivestocks(): Response<List<com.latihan.si_sapimobile.models.Livestock>>
    @GET("livestocks/{tagNumber}")
    suspend fun getLivestockByTag(@Path("tagNumber") tagNumber: String): Response<LivestockProfile>

    @POST("livestocks/{tagNumber}/weight-logs")
    suspend fun recordWeight(
        @Path("tagNumber") tagNumber: String,
        @Body body: CreateWeightLogRequest
    ): Response<ResponseBody>

    @GET("cages")
    suspend fun getCages(): Response<List<Cage>>

    @GET("feeds")
    suspend fun getFeeds(): Response<List<FeedStock>>

    @POST("cages/{id}/feed")
    suspend fun recordFeedConsumption(
        @Path("id") cageId: Int,
        @Body body: CreateFeedConsumptionRequest
    ): Response<ResponseBody>

    // Dipakai untuk emergency_report, vaccination, dan treatment (endpoint sama)
    @Multipart
    @POST("livestocks/{tagNumber}/health-report")
    suspend fun sendHealthReport(
        @Path("tagNumber") tagNumber: String,
        @Part("type") type: RequestBody,
        @Part("diagnosis") diagnosis: RequestBody?,
        @Part("treatment") treatment: RequestBody?,
        @Part("next_due_date") nextDueDate: RequestBody?,
        @Part("date") date: RequestBody,
        @Part photo: MultipartBody.Part?
    ): Response<ResponseBody>

    // Batch 6
    @GET("health-records/due")
    suspend fun getDueVaccinations(): Response<List<HealthRecord>>

    @GET("livestocks/{tagNumber}/health-records")
    suspend fun getHealthRecordsByTag(@Path("tagNumber") tagNumber: String): Response<List<HealthRecord>>

    @GET("health-records/emergency")
    suspend fun getEmergencyReports(): Response<List<HealthRecord>>

    @GET("worker/daily-tasks")
    suspend fun getDailyTasks(): Response<com.latihan.si_sapimobile.models.WorkerTaskResponse>

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(): Response<com.latihan.si_sapimobile.models.DashboardSummaryResponse>


}