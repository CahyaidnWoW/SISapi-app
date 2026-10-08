package com.latihan.si_sapi.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapi.helpers.UiState
import com.latihan.si_sapi.models.User
import com.latihan.si_sapi.services.AuthService
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val service = AuthService(app)

    private val _loginState = MutableLiveData<UiState<User>>()
    val loginState: LiveData<UiState<User>> = _loginState

    private val _logoutDone = MutableLiveData<Boolean>()
    val logoutDone: LiveData<Boolean> = _logoutDone

    fun login(email: String, password: String) {
        val e = email.trim()
        if (e.isEmpty() || password.isEmpty()) {
            _loginState.value = UiState.Error("Email dan password wajib diisi.")
            return
        }
        _loginState.value = UiState.Loading
        viewModelScope.launch { _loginState.postValue(service.login(e, password)) }
    }

    fun logout() {
        viewModelScope.launch {
            service.logout()
            _logoutDone.postValue(true)
        }
    }
}
