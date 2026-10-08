package com.latihan.si_sapimobile.services

import android.content.Context
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.helpers.errorMessage
import com.latihan.si_sapimobile.helpers.todayString
import com.latihan.si_sapimobile.models.Cage
import com.latihan.si_sapimobile.models.CreateFeedConsumptionRequest
import com.latihan.si_sapimobile.models.FeedStock
import com.latihan.si_sapimobile.network.ApiClient
import java.io.IOException

data class FeedOptions(val cages: List<Cage>, val feeds: List<FeedStock>)

class FeedConsumptionService(context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun loadOptions(): UiState<FeedOptions> = try {
        val cagesRes = api.getCages()
        val feedsRes = api.getFeeds()
        val cages = cagesRes.body()
        val feeds = feedsRes.body()
        when {
            cagesRes.code() == 401 || feedsRes.code() == 401 ->
                UiState.Error("Sesi berakhir. Keluar lalu masuk lagi.")
            !cagesRes.isSuccessful || cages == null ->
                UiState.Error(cagesRes.errorMessage("Gagal memuat daftar kandang."))
            !feedsRes.isSuccessful || feeds == null ->
                UiState.Error(feedsRes.errorMessage("Gagal memuat daftar pakan."))
            cages.isEmpty() || feeds.isEmpty() ->
                UiState.Error("Data kandang atau jenis pakan masih kosong. Isi dulu lewat aplikasi desktop.")
            else -> UiState.Success(FeedOptions(cages, feeds))
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }

    // Stok kurang -> backend membalas 422 {"message": "Stok pakan tidak mencukupi. Tersedia: X, dibutuhkan: Y"}.
    // Pesan itu ditampilkan apa adanya lewat errorMessage().
    suspend fun record(cageId: Int, feedId: Int, amount: Double): UiState<Unit> = try {
        val res = api.recordFeedConsumption(
            cageId,
            CreateFeedConsumptionRequest(cageId, feedId, amount, todayString())
        )
        when {
            res.isSuccessful -> UiState.Success(Unit)
            res.code() == 401 -> UiState.Error("Sesi berakhir. Keluar lalu masuk lagi.")
            else -> UiState.Error(res.errorMessage("Gagal menyimpan pemakaian pakan."))
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }
}
