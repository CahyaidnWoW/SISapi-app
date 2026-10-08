package com.latihan.si_sapimobile.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.databinding.ActivityMainBinding
import com.latihan.si_sapimobile.databinding.ItemToolBinding
import com.latihan.si_sapimobile.helpers.BottomNav
import com.latihan.si_sapimobile.helpers.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        val role = session.getUserRole().lowercase()

        binding.tvGreeting.text = "Halo, ${session.getUserName()}"
        binding.tvRole.text = roleLabel(role)
        binding.tvDate.text = SimpleDateFormat("EEEE, d MMM yyyy", Locale.forLanguageTag("id-ID"))
            .format(Date())

        buildTools(role)

        BottomNav.setup(this, BottomNav.TAB_HOME)
    }

    private fun buildTools(role: String) {
        binding.gridTools.removeAllViews()
        when (role) {
            "worker", "pekerja" -> {
                addTool("Scan QR", "Lihat profil sapi", R.drawable.ic_qr) { openScan(ScanActivity.MODE_SCAN) }
                addTool("Timbang", "Catat berat badan", R.drawable.ic_scale) { openScan(ScanActivity.MODE_WEIGHT) }
                addTool("Pakan", "Catat pakan harian", R.drawable.ic_feed) {
                    startActivity(Intent(this, FeedInputActivity::class.java))
                }
                addTool("Lapor sakit", "Laporan darurat", R.drawable.ic_alert, danger = true) {
                    openScan(ScanActivity.MODE_REPORT)
                }
            }
            "vet" -> {
                addTool("Scan QR", "Lihat profil sapi", R.drawable.ic_qr) { openScan(ScanActivity.MODE_SCAN) }
                addTool("Vaksin", "Jatuh tempo", R.drawable.ic_syringe) {
                    startActivity(Intent(this, ReminderListActivity::class.java))
                }
                addTool("Tindakan", "Catat vaksin / obat", R.drawable.ic_clip) {
                    openScan(ScanActivity.MODE_TREATMENT)
                }
                addTool("Rekam medis", "Riwayat sapi", R.drawable.ic_history) {
                    startActivity(Intent(this, HistoryActivity::class.java))
                }
            }
            else -> {
                addTool("Dashboard", "Ringkasan peternakan", R.drawable.ic_dashboard) {
                    startActivity(Intent(this, DashboardActivity::class.java))
                }
                addTool("Scan QR", "Lihat profil sapi", R.drawable.ic_qr) { openScan(ScanActivity.MODE_SCAN) }
            }
        }
    }

    private fun addTool(
        title: String,
        desc: String,
        @DrawableRes icon: Int,
        danger: Boolean = false,
        onClick: () -> Unit
    ) {
        val item = ItemToolBinding.inflate(layoutInflater, binding.gridTools, false)
        item.tvTitle.text = title
        item.tvDesc.text = desc
        item.ivIcon.setImageResource(icon)
        if (danger) {
            item.iconBox.setBackgroundResource(R.drawable.bg_icon_chip_danger)
            item.ivIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.danger))
            item.tvTitle.setTextColor(getColor(R.color.danger))
        } else {
            item.ivIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.royal))
        }
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

    private fun roleLabel(role: String) = when (role) {
        "manager" -> "Manager"
        "vet" -> "Dokter hewan"
        "worker", "pekerja" -> "Pekerja"
        else -> role
    }
}