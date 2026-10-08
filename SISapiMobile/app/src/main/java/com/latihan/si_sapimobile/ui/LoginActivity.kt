package com.latihan.si_sapimobile.ui

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapimobile.databinding.ActivityLoginBinding
import com.latihan.si_sapimobile.helpers.SessionManager
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.viewmodels.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager(this).getToken().isNullOrEmpty()) {
            goToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            viewModel.login(binding.etEmail.text.toString(), binding.etPassword.text.toString())
        }
        binding.tvTogglePassword.setOnClickListener { togglePassword() }

        viewModel.loginState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.tvError.visibility = View.GONE
                    setBusy(true)
                }
                is UiState.Success -> {
                    setBusy(false)
                    goToMain()
                }
                is UiState.Error -> {
                    setBusy(false)
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.progressBar.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !busy
        binding.btnLogin.text = if (busy) "Memproses..." else "Masuk"
    }

    private fun togglePassword() {
        passwordVisible = !passwordVisible
        binding.etPassword.transformationMethod =
            if (passwordVisible) HideReturnsTransformationMethod.getInstance()
            else PasswordTransformationMethod.getInstance()
        binding.etPassword.setSelection(binding.etPassword.text.length)
        binding.tvTogglePassword.text = if (passwordVisible) "Sembunyikan" else "Tampilkan"
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
