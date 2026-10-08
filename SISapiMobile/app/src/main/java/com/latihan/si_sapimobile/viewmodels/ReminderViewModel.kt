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

class ReminderViewModel(app: Application) : AndroidViewModel(app) {
    private val service = HealthRecordService(app)

    private val _vaccineState = MutableLiveData<UiState<List<HealthRecord>>>()
    val vaccineState: LiveData<UiState<List<HealthRecord>>> = _vaccineState

    private val _emergencyState = MutableLiveData<UiState<List<HealthRecord>>>()
    val emergencyState: LiveData<UiState<List<HealthRecord>>> = _emergencyState

    init {
        loadVaccines()
        loadEmergencies()
    }

    fun loadVaccines() {
        _vaccineState.value = UiState.Loading
        viewModelScope.launch { _vaccineState.postValue(service.getDueVaccinations()) }
    }

    fun loadEmergencies() {
        _emergencyState.value = UiState.Loading
        viewModelScope.launch { _emergencyState.postValue(service.getEmergencyReports()) }
    }
}