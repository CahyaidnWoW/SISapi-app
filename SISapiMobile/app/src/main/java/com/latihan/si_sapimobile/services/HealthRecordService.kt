package com.latihan.si_sapimobile.services

import android.content.Context
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.helpers.errorMessage
import com.latihan.si_sapimobile.helpers.todayString
import com.latihan.si_sapimobile.models.HealthRecord
import com.latihan.si_sapimobile.network.ApiClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class HealthRecordService(context: Context) {
    private val api = ApiClient.getApiService(context)

    // ---------- Lapor sakit (pekerja) ----------

    suspend fun sendEmergencyReport(
        tagNumber: String,
        symptoms: String,
        firstAction: String?,
        photoFile: File?
    ): UiState<Unit> = send(
        tagNumber = tagNumber,
        type = "emergency_report",
        diagnosis = symptoms,
        treatment = firstAction?.takeIf { it.isNotBlank() },
        nextDueDate = null,
        photoFile = photoFile
    )

    // ---------- Catat tindakan (dokter hewan) ----------

    suspend fun sendVaccination(
        tagNumber: String,
        vaccineName: String,
        note: String?,
        nextDueDate: String?
    ): UiState<Unit> = send(
        tagNumber = tagNumber,
        type = "vaccination",
        diagnosis = vaccineName,
        treatment = note?.takeIf { it.isNotBlank() },
        nextDueDate = nextDueDate,
        photoFile = null
    )

    suspend fun sendTreatmentRecord(
        tagNumber: String,
        diagnosis: String,
        treatment: String?
    ): UiState<Unit> = send(
        tagNumber = tagNumber,
        type = "treatment",
        diagnosis = diagnosis,
        treatment = treatment?.takeIf { it.isNotBlank() },
        nextDueDate = null,
        photoFile = null
    )

    // ---------- Riwayat & pengingat ----------

    suspend fun getDueVaccinations(): UiState<List<HealthRecord>> = getList(
        call = { api.getDueVaccinations() },
        defaultError = "Gagal memuat pengingat vaksin."
    )

    suspend fun getHistory(tagNumber: String): UiState<List<HealthRecord>> = getList(
        call = { api.getHealthRecordsByTag(tagNumber) },
        defaultError = "Gagal memuat riwayat kesehatan."
    )

    // ---------- internal ----------

    private suspend fun send(
        tagNumber: String,
        type: String,
        diagnosis: String?,
        treatment: String?,
        nextDueDate: String?,
        photoFile: File?
    ): UiState<Unit> = try {
        val text = "text/plain".toMediaType()
        val photoPart = photoFile?.let {
            MultipartBody.Part.createFormData("photo", it.name, it.asRequestBody("image/jpeg".toMediaType()))
        }
        val res = api.sendHealthReport(
            tagNumber = tagNumber,
            type = type.toRequestBody(text),
            diagnosis = diagnosis?.toRequestBody(text),
            treatment = treatment?.toRequestBody(text),
            nextDueDate = nextDueDate?.toRequestBody(text),
            date = todayString().toRequestBody(text),
            photo = photoPart
        )
        when {
            res.isSuccessful -> UiState.Success(Unit)
            res.code() == 404 -> UiState.Error("Sapi dengan tag $tagNumber tidak ditemukan.")
            res.code() == 401 -> UiState.Error("Sesi berakhir. Keluar lalu masuk lagi.")
            else -> UiState.Error(res.errorMessage("Gagal mengirim data."))
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }

    private suspend fun getList(
        call: suspend () -> retrofit2.Response<List<HealthRecord>>,
        defaultError: String
    ): UiState<List<HealthRecord>> = try {
        val res = call()
        val body = res.body()
        when {
            res.isSuccessful && body != null -> UiState.Success(body)
            res.code() == 401 -> UiState.Error("Sesi berakhir. Keluar lalu masuk lagi.")
            else -> UiState.Error(res.errorMessage(defaultError))
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }

    suspend fun getEmergencyReports(): UiState<List<HealthRecord>> = getList(
        call = { api.getEmergencyReports() },
        defaultError = "Gagal memuat laporan darurat."
    )
}