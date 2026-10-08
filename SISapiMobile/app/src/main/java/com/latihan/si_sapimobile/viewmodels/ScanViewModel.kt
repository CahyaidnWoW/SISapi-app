package com.latihan.si_sapimobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.LivestockProfile
import com.latihan.si_sapimobile.services.LivestockService
import kotlinx.coroutines.launch

class ScanViewModel(app: Application) : AndroidViewModel(app) {
    private val service = LivestockService(app)

    // null = belum ada pencarian (layar siap memindai)
    private val _scanState = MutableLiveData<UiState<LivestockProfile>?>()
    val scanState: LiveData<UiState<LivestockProfile>?> = _scanState

    fun fetchLivestockByTag(tag: String) {
        val t = tag.trim()
        if (t.isEmpty()) {
            _scanState.value = UiState.Error("Nomor tag wajib diisi.")
            return
        }
        if (_scanState.value is UiState.Loading) return // abaikan scan ganda saat request berjalan
        _scanState.value = UiState.Loading
        viewModelScope.launch { _scanState.postValue(service.getLivestockByTag(t)) }
    }

    fun reset() {
        _scanState.value = null
    }
}
