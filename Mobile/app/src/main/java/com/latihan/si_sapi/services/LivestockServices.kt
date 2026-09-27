package com.latihan.si_sapi.services

import android.content.Context
import com.latihan.si_sapi.helpers.UiState
import com.latihan.si_sapi.models.LivestockProfile
import com.latihan.si_sapi.network.ApiClient

class LivestockService(context: Context) {
    private val apiService = ApiClient.getApiService(context)

    suspend fun getLivestockByTag(tagNumber: String): UiState<LivestockProfile> {
        return try {
            val response = apiService.getLivestockByTag(tagNumber)
            val data = response.body()?.data

            if (response.isSuccessful && data != null) {
                UiState.Success(data)
            } else {
                UiState.Error("Tag sapi '$tagNumber' tidak ditemukan.")
            }
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Gagal terhubung ke server.")
        }
    }
}