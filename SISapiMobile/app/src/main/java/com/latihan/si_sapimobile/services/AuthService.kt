package com.latihan.si_sapimobile.services

import android.content.Context
import com.latihan.si_sapimobile.helpers.SessionManager
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.helpers.errorMessage
import com.latihan.si_sapimobile.models.LoginRequest
import com.latihan.si_sapimobile.models.User
import com.latihan.si_sapimobile.network.ApiClient
import java.io.IOException

class AuthService(private val context: Context) {
    private val api = ApiClient.getApiService(context)
    private val session = SessionManager(context)

    suspend fun login(email: String, password: String): UiState<User> = try {
        val res = api.login(LoginRequest(email, password))
        val body = res.body()
        if (res.isSuccessful && body != null) {
            session.saveAuthToken(body.accessToken)
            session.saveUser(body.user.name, body.user.role, body.user.email)
            UiState.Success(body.user)
        } else {
            UiState.Error(res.errorMessage("Email atau password salah."))
        }
    } catch (e: IOException) {
        android.util.Log.e("LOGIN", "Gagal konek: $e")
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")} catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }

    suspend fun logout() {
        try {
            api.logout()
        } catch (e: Exception) {
            // diabaikan: yang penting token lokal hilang
        }
        com.latihan.si_sapimobile.workers.VetNotificationWorker.cancel(context)
        session.clearSession()
    }
}
