package com.latihan.si_sapimobile

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class SiSapiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Paksa mode terang supaya tampilan konsisten, apa pun setelan HP
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}