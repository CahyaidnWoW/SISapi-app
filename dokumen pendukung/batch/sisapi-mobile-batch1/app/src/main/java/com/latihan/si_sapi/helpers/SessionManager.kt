package com.latihan.si_sapi.helpers

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("si_sapi_session", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUser(name: String, role: String) =
        prefs.edit().putString(KEY_NAME, name).putString(KEY_ROLE, role).apply()

    fun getUserName(): String = prefs.getString(KEY_NAME, "") ?: ""
    fun getUserRole(): String = prefs.getString(KEY_ROLE, "") ?: ""

    fun clearSession() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_NAME = "user_name"
        const val KEY_ROLE = "user_role"
    }
}
