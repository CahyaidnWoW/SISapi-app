package com.latihan.si_sapi.services

import android.content.Context
import com.latihan.si_sapi.helpers.SessionManager
import com.latihan.si_sapi.helpers.UiState
import com.latihan.si_sapi.models.LoginRequest
import com.latihan.si_sapi.models.User
import com.latihan.si_sapi.network.ApiClient

class AuthService(context: Context) {
    private val apiService = ApiClient.getApiService(context)
    private val sessionManager = SessionManager(context)

    suspend fun login(email: String, password: String): UiState<User> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val token = body.accessToken
                val user = body.user

                if (!token.isNullOrEmpty() && user != null) {
                    // Simpan token & user session
                    sessionManager.saveAuthToken(token)
                    sessionManager.saveUser(user.name, user.role)
                    UiState.Success(user)
                } else {
                    UiState.Error("Respon server tidak valid.")
                }
            } else {
                UiState.Error(response.message() ?: "Email atau password salah.")
            }
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Gagal terhubung ke server.")
        }
    }

    suspend fun logout(): UiState<Unit> {
        return try {
            val response = apiService.logout()
            // Hapus session lokal terlepas dari respon server
            sessionManager.clearSession()

            if (response.isSuccessful) {
                UiState.Success(Unit)
            } else {
                UiState.Success(Unit) // Tetap sukses logout lokal
            }
        } catch (e: Exception) {
            sessionManager.clearSession()
            UiState.Success(Unit)
        }
    }
}