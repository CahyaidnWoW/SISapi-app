package com.latihan.si_sapi.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapi.databinding.ActivityMainBinding
import com.latihan.si_sapi.helpers.SessionManager
import com.latihan.si_sapi.viewmodels.AuthViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        binding.tvGreeting.text = "Halo, ${session.getUserName()}"
        binding.tvRole.text = roleLabel(session.getUserRole())

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Keluar dari Si-Sapi?")
                .setMessage("Kamu perlu masuk lagi untuk memakai aplikasi.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Keluar") { _, _ -> viewModel.logout() }
                .show()
        }

        viewModel.logoutDone.observe(this) { done ->
            if (done) {
                startActivity(
                    Intent(this, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
        }
    }

    // Nilai role asli dari backend (huruf kecil). Cek di Logcat/DB lalu sesuaikan kalau beda.
    private fun roleLabel(role: String) = when (role.lowercase()) {
        "manager" -> "Manager"
        "vet" -> "Dokter hewan"
        "worker", "pekerja" -> "Pekerja"
        else -> role
    }
}
