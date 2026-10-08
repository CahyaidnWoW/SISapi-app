package com.latihan.si_sapimobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.services.DashboardData
import com.latihan.si_sapimobile.services.DashboardService
import kotlinx.coroutines.launch

class DashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val service = DashboardService(app)

    private val _state = MutableLiveData<UiState<DashboardData>>()
    val state: LiveData<UiState<DashboardData>> = _state

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch { _state.postValue(service.load()) }
    }
}