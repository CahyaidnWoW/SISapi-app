package com.latihan.si_sapi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapi.helpers.SessionManager
import com.latihan.si_sapi.helpers.UiState
import com.latihan.si_sapi.ui.LoginActivity
import com.latihan.si_sapi.viewmodels.AuthViewModel
import com.latihan.si_sapi.ui.ScanActivity


class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private lateinit var tvWelcomeName: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var btnLogout: Button

    private lateinit var btnMenuScan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        initViews()
        setupUserInfo()
        observeViewModel()

        btnLogout.setOnClickListener {
            btnLogout.isEnabled = false
            authViewModel.logout()
        }

        btnMenuScan.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initViews() {
        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        tvUserRole = findViewById(R.id.tvUserRole)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupUserInfo() {
        val name = sessionManager.getUserName() ?: "Peternak"
        val role = sessionManager.getUserRole() ?: "Worker"

        tvWelcomeName.text = "Halo, $name!"
        tvUserRole.text = "Role: $role"
    }

    private fun observeViewModel() {
        authViewModel.logoutState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    Toast.makeText(this, "Mengeluarkan akun...", Toast.LENGTH_SHORT).show()
                }
                is UiState.Success -> {
                    Toast.makeText(this, "Berhasil keluar.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is UiState.Error -> {
                    btnLogout.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}