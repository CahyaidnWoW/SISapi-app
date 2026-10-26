package com.latihan.si_sapimobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.WorkerTaskResponse
import com.latihan.si_sapimobile.services.TaskService
import kotlinx.coroutines.launch

class TaskViewModel(app: Application) : AndroidViewModel(app) {
    private val service = TaskService(app)

    private val _tasksState = MutableLiveData<UiState<WorkerTaskResponse>>()
    val tasksState: LiveData<UiState<WorkerTaskResponse>> = _tasksState

    fun loadDailyTasks() {
        _tasksState.value = UiState.Loading
        viewModelScope.launch {
            _tasksState.postValue(service.getDailyTasks())
        }
    }
}
