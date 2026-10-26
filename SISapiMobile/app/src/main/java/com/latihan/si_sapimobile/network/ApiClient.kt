package com.latihan.si_sapimobile.network

import android.content.Context
import com.latihan.si_sapimobile.helpers.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "http://localhost:8000/api/"
//    private const val BASE_URL = "http://sisapi.smkn1sumedang.sch.id/api/"
    private const val LOG_BODY = true

    @Volatile
    private var api: ApiService? = null

    fun getApiService(context: Context): ApiService =
        api ?: synchronized(this) {
            api ?: build(context.applicationContext).also { api = it }
        }

    private fun build(context: Context): ApiService {
        val session = SessionManager(context)

        val logging = HttpLoggingInterceptor().apply {
            level = if (LOG_BODY) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder().addHeader("Accept", "application/json")
                session.getToken()?.takeIf { it.isNotEmpty() }?.let {
                    builder.addHeader("Authorization", "Bearer $it")
                }
                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
