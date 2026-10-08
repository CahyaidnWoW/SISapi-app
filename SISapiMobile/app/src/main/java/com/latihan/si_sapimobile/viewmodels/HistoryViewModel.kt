package com.latihan.si_sapimobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.HealthRecord
import com.latihan.si_sapimobile.services.HealthRecordService
import kotlinx.coroutines.launch

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val service = HealthRecordService(app)

    // null = belum ada pencarian
    private val _state = MutableLiveData<UiState<List<HealthRecord>>?>()
    val state: LiveData<UiState<List<HealthRecord>>?> = _state

    fun load(tag: String) {
        val t = tag.trim()
        if (t.isEmpty()) {
            _state.value = UiState.Error("Nomor tag wajib diisi.")
            return
        }
        if (_state.value is UiState.Loading) return
        _state.value = UiState.Loading
        viewModelScope.launch { _state.postValue(service.getHistory(t)) }
    }

    fun reset() {
        _state.value = null
    }
}