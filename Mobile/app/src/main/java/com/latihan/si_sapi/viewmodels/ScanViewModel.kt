package com.latihan.si_sapi.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapi.helpers.UiState
import com.latihan.si_sapi.models.LivestockProfile
import com.latihan.si_sapi.services.LivestockService
import kotlinx.coroutines.launch

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val livestockService = LivestockService(application)

    private val _scanState = MutableLiveData<UiState<LivestockProfile>>()
    val scanState: LiveData<UiState<LivestockProfile>> = _scanState

    fun fetchLivestockByTag(tagNumber: String) {
        _scanState.value = UiState.Loading
        viewModelScope.launch {
            val result = livestockService.getLivestockByTag(tagNumber)
            _scanState.postValue(result)
        }
    }
}