package com.latihan.si_sapimobile.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.databinding.ActivityProfileBinding
import com.latihan.si_sapimobile.helpers.BottomNav
import com.latihan.si_sapimobile.helpers.SessionManager
import com.latihan.si_sapimobile.viewmodels.AuthViewModel

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        val name = session.getUserName().ifBlank { "Pengguna" }
        val email = session.getUserEmail().ifBlank { "-" }
        val role = session.getUserRole().lowercase()

        binding.tvAvatar.text = initials(name)
        binding.tvName.text = name
        binding.tvEmail.text = email
        binding.tvRole.text = roleLabel(role)
        binding.tvVersion.text = "1.0"

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

        BottomNav.setup(this, BottomNav.TAB_PROFILE)
    }

    private fun initials(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts[0].take(1).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    private fun roleLabel(role: String) = when (role) {
        "manager" -> "Manager"
        "vet" -> "Dokter hewan"
        "worker", "pekerja" -> "Pekerja"
        else -> role
    }
}