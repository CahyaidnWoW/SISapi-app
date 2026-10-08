package com.latihan.si_sapimobile.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.databinding.ActivityMainBinding
import com.latihan.si_sapimobile.databinding.ItemToolBinding
import com.latihan.si_sapimobile.helpers.SessionManager
import com.latihan.si_sapimobile.viewmodels.AuthViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        val role = session.getUserRole().lowercase()

        // Untuk memastikan nilai role asli dari backend. Hapus setelah dicek.
        Log.d("ROLE", "role dari server = '$role'")

        binding.tvGreeting.text = "Halo, ${session.getUserName()}"
        binding.tvRole.text = roleLabel(role)

        buildTools(role)

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

    // Tile Alat ditentukan oleh role.
    private fun buildTools(role: String) {
        binding.gridTools.removeAllViews()
        when (role) {
            "worker", "pekerja" -> {
                addTool("Scan QR", "Lihat profil sapi") { openScan(ScanActivity.MODE_SCAN) }
                addTool("Timbang", "Catat berat badan") { openScan(ScanActivity.MODE_WEIGHT) }
                addTool("Pakan", "Catat pakan harian") { soon("Catat pakan", "Batch 4") }
                addTool("Lapor sakit", "Laporan darurat", danger = true) { openScan(ScanActivity.MODE_REPORT) }
            }
            "vet" -> {
                addTool("Scan QR", "Lihat profil sapi") { openScan(ScanActivity.MODE_SCAN) }
                addTool("Vaksin", "Jatuh tempo") { soon("Pengingat vaksin", "Batch 6") }
                addTool("Tindakan", "Catat vaksin / obat") { soon("Catat tindakan", "Batch 6") }
                addTool("Rekam medis", "Riwayat sapi") { soon("Rekam medis", "Batch 6") }
            }
            else -> {
                // Manager atau role lain: keputusan terbuka. Sementara hanya Scan QR.
                addTool("Scan QR", "Lihat profil sapi") { openScan(ScanActivity.MODE_SCAN) }
                binding.tvNote.text = "Fitur lengkap untuk Manager tersedia di aplikasi desktop."
                binding.tvNote.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun addTool(title: String, desc: String, danger: Boolean = false, onClick: () -> Unit) {
        val item = ItemToolBinding.inflate(layoutInflater, binding.gridTools, false)
        item.tvTitle.text = title
        item.tvDesc.text = desc
        if (danger) item.tvTitle.setTextColor(getColor(R.color.danger))
        item.root.setOnClickListener { onClick() }

        val margin = (6 * resources.displayMetrics.density).toInt()
        val lp = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
        ).apply {
            width = 0
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setMargins(margin, margin, margin, margin)
        }
        binding.gridTools.addView(item.root, lp)
    }

    private fun openScan(mode: String) {
        startActivity(Intent(this, ScanActivity::class.java).putExtra(ScanActivity.EXTRA_MODE, mode))
    }

    private fun soon(feature: String, batch: String) {
        Toast.makeText(this, "$feature dikerjakan di $batch.", Toast.LENGTH_SHORT).show()
    }

    private fun roleLabel(role: String) = when (role) {
        "manager" -> "Manager"
        "vet" -> "Dokter hewan"
        "worker", "pekerja" -> "Pekerja"
        else -> role
    }
}
