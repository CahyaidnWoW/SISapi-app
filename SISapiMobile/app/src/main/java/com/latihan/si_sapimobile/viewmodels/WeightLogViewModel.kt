package com.latihan.si_sapimobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.services.WeightLogService
import kotlinx.coroutines.launch

class WeightLogViewModel(app: Application) : AndroidViewModel(app) {
    private val service = WeightLogService(app)

    private val _saveState = MutableLiveData<UiState<Unit>>()
    val saveState: LiveData<UiState<Unit>> = _saveState

    fun saveWeight(tagNumber: String, weightText: String) {
        val weight = weightText.trim().replace(',', '.').toDoubleOrNull()
        if (weightText.isBlank()) { _saveState.value = UiState.Error("Berat badan wajib diisi."); return }
        if (weight == null) { _saveState.value = UiState.Error("Berat harus berupa angka."); return }
        if (weight <= 0.0) { _saveState.value = UiState.Error("Berat harus lebih dari 0 kg."); return }
        if (_saveState.value is UiState.Loading) return // cegah ketuk ganda
        _saveState.value = UiState.Loading
        viewModelScope.launch { _saveState.postValue(service.recordWeight(tagNumber, weight)) }
    }
}
