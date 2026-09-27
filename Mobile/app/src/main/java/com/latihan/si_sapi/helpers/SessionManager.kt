package com.latihan.si_sapi.helpers

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("si_sapi_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUser(name: String, role: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_ROLE, role)
            .apply()
    }

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, "")
    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, "")

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}