package com.latihan.si_sapimobile.services

import android.content.Context
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.helpers.errorMessage
import com.latihan.si_sapimobile.models.LivestockProfile
import com.latihan.si_sapimobile.network.ApiClient
import java.io.IOException

class LivestockService(context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun getLivestockByTag(tag: String): UiState<LivestockProfile> = try {
        val res = api.getLivestockByTag(tag)
        val body = res.body()
        when {
            res.isSuccessful && body != null -> UiState.Success(body)
            res.code() == 404 -> UiState.Error("Sapi dengan tag $tag tidak ditemukan.")
            res.code() == 401 -> UiState.Error("Sesi berakhir. Keluar lalu masuk lagi.")
            else -> UiState.Error(res.errorMessage("Gagal memuat data sapi."))
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }
}
