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

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authService = AuthService(application)

    private val _loginState = MutableLiveData<UiState<User>>()
    val loginState: LiveData<UiState<User>> = _loginState

    private val _logoutState = MutableLiveData<UiState<Unit>>()
    val logoutState: LiveData<UiState<Unit>> = _logoutState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = UiState.Error("Email dan password tidak boleh kosong.")
            return
        }

        _loginState.value = UiState.Loading
        viewModelScope.launch {
            val result = authService.login(email, password)
            _loginState.postValue(result)
        }
    }

    fun logout() {
        _logoutState.value = UiState.Loading
        viewModelScope.launch {
            val result = authService.logout()
            _logoutState.postValue(result)
        }
    }
}