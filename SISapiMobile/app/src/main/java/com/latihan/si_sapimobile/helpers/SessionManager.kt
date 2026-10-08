package com.latihan.si_sapimobile.helpers

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("si_sapi_session", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUser(name: String, role: String, email: String) =
        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_ROLE, role)
            .putString(KEY_EMAIL, email)
            .apply()

    fun getUserName(): String = prefs.getString(KEY_NAME, "") ?: ""
    fun getUserRole(): String = prefs.getString(KEY_ROLE, "") ?: ""
    fun getUserEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""

    fun clearSession() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_NAME = "user_name"
        const val KEY_ROLE = "user_role"
        const val KEY_EMAIL = "user_email"
    }
}