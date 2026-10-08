package com.latihan.si_sapimobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapimobile.helpers.ImageHelper
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.services.HealthRecordService
import kotlinx.coroutines.launch
import java.io.File

class HealthRecordViewModel(app: Application) : AndroidViewModel(app) {
    private val service = HealthRecordService(app)
    private val photoDir = File(app.cacheDir, "photos")

    private var pendingRaw: File? = null

    private val _photoState = MutableLiveData<UiState<File>?>()
    val photoState: LiveData<UiState<File>?> = _photoState

    private val _reportState = MutableLiveData<UiState<Unit>>()
    val reportState: LiveData<UiState<Unit>> = _reportState

    // ---------- foto (untuk laporan darurat) ----------

    fun createRawFile(): File {
        photoDir.mkdirs()
        return File(photoDir, "raw_${System.currentTimeMillis()}.jpg").also { pendingRaw = it }
    }

    fun processPhoto() {
        val raw = pendingRaw ?: return
        _photoState.value = UiState.Loading
        viewModelScope.launch {
            val out = ImageHelper.compress(raw, photoDir)
            raw.delete()
            pendingRaw = null
            if (out != null) {
                (_photoState.value as? UiState.Success<File>)?.data?.delete()
                _photoState.postValue(UiState.Success(out))
            } else {
                _photoState.postValue(UiState.Error("Foto gagal diproses. Coba ambil ulang."))
            }
        }
    }

    fun discardRaw() {
        pendingRaw?.delete()
        pendingRaw = null
    }

    fun clearPhoto() {
        (_photoState.value as? UiState.Success<File>)?.data?.delete()
        _photoState.value = null
    }

    // ---------- kirim ----------

    fun sendReport(tagNumber: String, symptoms: String, firstAction: String?) {
        if (symptoms.isBlank()) {
            _reportState.value = UiState.Error("Gejala wajib diisi.")
            return
        }
        if (_photoState.value is UiState.Loading) {
            _reportState.value = UiState.Error("Foto masih diproses. Tunggu sebentar.")
            return
        }
        if (_reportState.value is UiState.Loading) return

        val photo = (_photoState.value as? UiState.Success<File>)?.data
        _reportState.value = UiState.Loading
        viewModelScope.launch {
            _reportState.postValue(service.sendEmergencyReport(tagNumber, symptoms.trim(), firstAction, photo))
        }
    }

    fun sendVaccination(tagNumber: String, vaccineName: String, note: String?, nextDueDate: String?) {
        if (vaccineName.isBlank()) {
            _reportState.value = UiState.Error("Jenis vaksin wajib diisi.")
            return
        }
        if (_reportState.value is UiState.Loading) return
        _reportState.value = UiState.Loading
        viewModelScope.launch {
            _reportState.postValue(service.sendVaccination(tagNumber, vaccineName.trim(), note, nextDueDate))
        }
    }

    fun sendTreatment(tagNumber: String, diagnosis: String, treatment: String?) {
        if (diagnosis.isBlank()) {
            _reportState.value = UiState.Error("Diagnosis wajib diisi.")
            return
        }
        if (_reportState.value is UiState.Loading) return
        _reportState.value = UiState.Loading
        viewModelScope.launch {
            _reportState.postValue(service.sendTreatmentRecord(tagNumber, diagnosis.trim(), treatment))
        }
    }
}