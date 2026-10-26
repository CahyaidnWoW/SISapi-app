package com.latihan.si_sapimobile.services

import android.content.Context
import com.latihan.si_sapimobile.helpers.errorMessage
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.WorkerTaskResponse
import com.latihan.si_sapimobile.network.ApiClient
import java.io.IOException

class TaskService(context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun getDailyTasks(): UiState<WorkerTaskResponse> = try {
        val res = api.getDailyTasks()
        if (res.isSuccessful && res.body() != null) {
            UiState.Success(res.body()!!)
        } else {
            UiState.Error(res.errorMessage("Gagal memuat tugas harian."))
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi jaringan.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan saat memuat tugas.")
    }
}
