package com.latihan.si_sapi.services

import android.content.Context
import com.latihan.si_sapi.helpers.SessionManager
import com.latihan.si_sapi.helpers.UiState
import com.latihan.si_sapi.helpers.errorMessage
import com.latihan.si_sapi.models.LoginRequest
import com.latihan.si_sapi.models.User
import com.latihan.si_sapi.network.ApiClient
import java.io.IOException

class AuthService(context: Context) {
    private val api = ApiClient.getApiService(context)
    private val session = SessionManager(context)

    suspend fun login(email: String, password: String): UiState<User> = try {
        val res = api.login(LoginRequest(email, password))
        val body = res.body()
        if (res.isSuccessful && body != null) {
            session.saveAuthToken(body.accessToken)
            session.saveUser(body.user.name, body.user.role)
            UiState.Success(body.user)
        } else {
            UiState.Error(res.errorMessage("Email atau password salah."))
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }

    // Sesi lokal SELALU dihapus, walau server tidak terjangkau
    suspend fun logout() {
        try {
            api.logout()
        } catch (e: Exception) {
            // diabaikan: yang penting token lokal hilang
        }
        session.clearSession()
    }
}
