package com.latihan.si_sapimobile.services

import android.content.Context
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.helpers.errorMessage
import com.latihan.si_sapimobile.models.HealthRecord
import com.latihan.si_sapimobile.network.ApiClient
import java.io.IOException

data class DashboardData(
    val totalLivestock: Int,
    val totalCages: Int,
    val dueVaccinations: List<HealthRecord>
)

class DashboardService(context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun load(): UiState<DashboardData> = try {
        val livestockRes = api.getLivestocks()
        val cagesRes = api.getCages()
        val dueRes = api.getDueVaccinations()

        when {
            livestockRes.code() == 401 || cagesRes.code() == 401 || dueRes.code() == 401 ->
                UiState.Error("Sesi berakhir. Keluar lalu masuk lagi.")
            !livestockRes.isSuccessful ->
                UiState.Error(livestockRes.errorMessage("Gagal memuat data sapi."))
            !cagesRes.isSuccessful ->
                UiState.Error(cagesRes.errorMessage("Gagal memuat data kandang."))
            !dueRes.isSuccessful ->
                UiState.Error(dueRes.errorMessage("Gagal memuat pengingat vaksin."))
            else -> UiState.Success(
                DashboardData(
                    totalLivestock = livestockRes.body()?.size ?: 0,
                    totalCages = cagesRes.body()?.size ?: 0,
                    dueVaccinations = dueRes.body() ?: emptyList()
                )
            )
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }
}