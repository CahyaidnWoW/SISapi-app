package com.latihan.si_sapimobile.services

import android.content.Context
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.helpers.errorMessage
import com.latihan.si_sapimobile.helpers.todayString
import com.latihan.si_sapimobile.models.CreateWeightLogRequest
import com.latihan.si_sapimobile.network.ApiClient
import java.io.IOException

class WeightLogService(context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun recordWeight(tagNumber: String, weight: Double): UiState<Unit> = try {
        val res = api.recordWeight(tagNumber, CreateWeightLogRequest(weight, todayString()))
        when {
            res.isSuccessful -> UiState.Success(Unit)
            res.code() == 404 -> UiState.Error("Sapi dengan tag $tagNumber tidak ditemukan.")
            res.code() == 401 -> UiState.Error("Sesi berakhir. Keluar lalu masuk lagi.")
            else -> UiState.Error(res.errorMessage("Gagal menyimpan timbangan."))
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }
}
