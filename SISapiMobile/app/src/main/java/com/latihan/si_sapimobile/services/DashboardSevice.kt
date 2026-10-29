package com.latihan.si_sapimobile.services

import android.content.Context
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.helpers.errorMessage
import com.latihan.si_sapimobile.models.DashboardSummaryData
import com.latihan.si_sapimobile.network.ApiClient
import java.io.IOException

class DashboardService(context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun load(): UiState<DashboardSummaryData> = try {
        val res = api.getDashboardSummary()
        when {
            res.code() == 401 -> UiState.Error("Sesi berakhir. Keluar lalu masuk lagi.")
            !res.isSuccessful -> UiState.Error(res.errorMessage("Gagal memuat ringkasan dashboard."))
            res.body()?.data == null -> UiState.Error("Data dashboard kosong.")
            else -> UiState.Success(res.body()!!.data)
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }
}
