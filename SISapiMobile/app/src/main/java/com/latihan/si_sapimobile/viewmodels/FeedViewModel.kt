package com.latihan.si_sapimobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.services.FeedConsumptionService
import com.latihan.si_sapimobile.services.FeedOptions
import kotlinx.coroutines.launch

class FeedViewModel(app: Application) : AndroidViewModel(app) {
    private val service = FeedConsumptionService(app)

    private val _optionsState = MutableLiveData<UiState<FeedOptions>>()
    val optionsState: LiveData<UiState<FeedOptions>> = _optionsState

    // null = belum ada penyimpanan / sudah ditangani (mencegah toast ulang saat layar diputar)
    private val _saveState = MutableLiveData<UiState<Unit>?>()
    val saveState: LiveData<UiState<Unit>?> = _saveState

    init {
        loadOptions()
    }

    fun loadOptions() {
        _optionsState.value = UiState.Loading
        viewModelScope.launch { _optionsState.postValue(service.loadOptions()) }
    }

    fun save(cageId: Int?, feedId: Int?, amountText: String) {
        if (cageId == null || feedId == null) {
            _saveState.value = UiState.Error("Pilih kandang dan jenis pakan dulu.")
            return
        }
        val amount = amountText.trim().replace(',', '.').toDoubleOrNull()
        if (amountText.isBlank()) { _saveState.value = UiState.Error("Jumlah pakan wajib diisi."); return }
        if (amount == null) { _saveState.value = UiState.Error("Jumlah pakan harus berupa angka."); return }
        if (amount <= 0.0) { _saveState.value = UiState.Error("Jumlah pakan harus lebih dari 0."); return }
        if (_saveState.value is UiState.Loading) return // cegah ketuk ganda

        _saveState.value = UiState.Loading
        viewModelScope.launch { _saveState.postValue(service.record(cageId, feedId, amount)) }
    }

    fun clearSaveState() {
        _saveState.value = null
    }
}
