package com.latihan.si_sapimobile.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.databinding.ActivityMainBinding
import com.latihan.si_sapimobile.databinding.ItemNotifBinding
import com.latihan.si_sapimobile.databinding.ItemToolBinding
import com.latihan.si_sapimobile.helpers.BottomNav
import com.latihan.si_sapimobile.helpers.NotificationHelper
import com.latihan.si_sapimobile.helpers.SessionManager
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.HealthRecord
import com.latihan.si_sapimobile.models.NotifItem
import com.latihan.si_sapimobile.models.WorkerTaskItem
import com.latihan.si_sapimobile.models.WorkerTaskResponse
import com.latihan.si_sapimobile.viewmodels.ReminderViewModel
import com.latihan.si_sapimobile.viewmodels.TaskViewModel
import com.latihan.si_sapimobile.workers.VetNotificationWorker
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        private const val NOTIF_PEEK_DP = 84
        private const val MANAGER_GAP_DP = 12
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var sheet: BottomSheetBehavior<View>
    private val reminderViewModel: ReminderViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()
    private var userRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        userRole = session.getUserRole().lowercase()
        val layered = userRole in setOf("worker", "pekerja", "vet")

        binding.tvGreeting.text = "Halo, ${session.getUserName()}"
        binding.tvRole.text = roleLabel(userRole)
        binding.tvDate.text = SimpleDateFormat("EEEE, d MMM yyyy", Locale.forLanguageTag("id-ID"))
            .format(Date())

        setupSheet(layered, savedInstanceState == null)
        buildTools(userRole)
        if (layered) {
            setupNotifications(userRole)
        }

        if (userRole == "vet") {
            VetNotificationWorker.schedule(this)
        }

        BottomNav.setup(this, BottomNav.TAB_HOME)
    }

    override fun onResume() {
        super.onResume()
        if (userRole == "vet") {
            reminderViewModel.loadVaccines()
            reminderViewModel.loadEmergencies()
            VetNotificationWorker.triggerImmediately(this)
        } else if (userRole in setOf("worker", "pekerja")) {
            taskViewModel.loadDailyTasks()
        }
    }

    private fun setupSheet(layered: Boolean, firstCreate: Boolean) {
        sheet = BottomSheetBehavior.from(binding.sheetUtil)
        val gap = if (layered) NOTIF_PEEK_DP else MANAGER_GAP_DP

        binding.stage.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            val target = (bottom - top) - dp(gap)
            if (target > 0 && binding.sheetUtil.minimumHeight != target) {
                binding.sheetUtil.minimumHeight = target
            }
        }

        if (!layered) {
            binding.layerNotif.visibility = View.GONE
            binding.sheetGrab.visibility = View.GONE
            binding.tvUtilTitle.text = "Ringkasan"
            sheet.isDraggable = false
            sheet.state = BottomSheetBehavior.STATE_EXPANDED
            return
        }

        if (firstCreate) sheet.state = BottomSheetBehavior.STATE_EXPANDED
        binding.headUtil.setOnClickListener { toggleSheet() }
        binding.headNotif.setOnClickListener { sheet.state = BottomSheetBehavior.STATE_COLLAPSED }
    }

    private fun toggleSheet() {
        sheet.state = if (sheet.state == BottomSheetBehavior.STATE_EXPANDED) {
            BottomSheetBehavior.STATE_COLLAPSED
        } else {
            BottomSheetBehavior.STATE_EXPANDED
        }
    }

    // ---------- notifikasi & tugas riil ----------

    private fun setupNotifications(role: String) {
        if (role == "vet") {
            binding.tvNotifTitle.text = "Laporan Darurat & Jadwal"
            reminderViewModel.vaccineState.observe(this) { updateVetNotifications() }
            reminderViewModel.emergencyState.observe(this) { updateVetNotifications() }
        } else {
            binding.tvNotifTitle.text = "Tugas Hari Ini"
            taskViewModel.tasksState.observe(this) { state ->
                when (state) {
                    is UiState.Loading -> {
                        if (binding.listNotif.childCount == 0) {
                            binding.tvNotifEmpty.text = "Memuat daftar tugas..."
                            binding.tvNotifEmpty.visibility = View.VISIBLE
                        }
                    }
                    is UiState.Success -> renderWorkerTasks(state.data)
                    is UiState.Error -> {
                        binding.tvNotifEmpty.text = state.message
                        binding.tvNotifEmpty.visibility = View.VISIBLE
                        binding.tvNotifBadge.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun renderWorkerTasks(data: WorkerTaskResponse) {
        binding.listNotif.removeAllViews()
        val tasks = data.tasks
        if (tasks.isEmpty()) {
            binding.tvNotifEmpty.text = "Belum ada tugas terjadwal hari ini."
            binding.tvNotifEmpty.visibility = View.VISIBLE
            binding.tvNotifBadge.visibility = View.GONE
            return
        }

        binding.tvNotifEmpty.visibility = View.GONE
        binding.tvNotifBadge.visibility = View.VISIBLE
        binding.tvNotifBadge.text = "${data.completedTasks}/${data.totalTasks} Selesai"

        tasks.forEach { task ->
            val item = ItemNotifBinding.inflate(layoutInflater, binding.listNotif, false)

            if (task.isCompleted) {
                item.tvItemTitle.text = "✓ ${task.title}"
                item.tvItemSub.text = task.detail ?: "Selesai dikerjakan"
                item.accent.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(getColor(R.color.ok))
                }
                item.root.setOnClickListener {
                    val person = task.completedBy ?: "Pekerja lain"
                    val time = task.completedAt?.let { " pada pukul $it" } ?: ""
                    Toast.makeText(this, "Tugas ini sudah diselesaikan oleh $person$time.", Toast.LENGTH_SHORT).show()
                }
            } else {
                item.tvItemTitle.text = "⏱ ${task.title}"
                item.tvItemSub.text = task.detail ?: "Belum dikerjakan"

                val accentColor = if (task.type == "monitor") R.color.danger else R.color.notif_warn
                item.accent.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(getColor(accentColor))
                }

                item.root.setOnClickListener {
                    when (task.action) {
                        "feed_input" -> {
                            startActivity(Intent(this, FeedInputActivity::class.java))
                        }
                        "scan_profile" -> {
                            startActivity(
                                Intent(this, ScanActivity::class.java)
                                    .putExtra(ScanActivity.EXTRA_MODE, ScanActivity.MODE_SCAN)
                                    .putExtra(ScanActivity.EXTRA_INITIAL_TAG, task.tagNumber)
                            )
                        }
                        else -> {
                            Toast.makeText(this, "Silakan selesaikan tugas ini di kandang.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            binding.listNotif.addView(item.root)
        }
    }

    private fun updateVetNotifications() {
        val vaccines = (reminderViewModel.vaccineState.value as? UiState.Success)?.data ?: emptyList()
        val emergencies = (reminderViewModel.emergencyState.value as? UiState.Success)?.data ?: emptyList()

        val items = mutableListOf<NotifItem>()
        val today = LocalDate.now()

        // 1. Laporan Darurat
        emergencies.forEach { r ->
            val tag = r.livestock?.tagNumber ?: "—"
            val diag = r.diagnosis?.takeIf { it.isNotBlank() } ?: "Kondisi darurat"
            val title = "🚨 Darurat: $tag"
            val sub = "$diag (${r.date?.take(10) ?: "-"})"
            items.add(NotifItem(title, sub, NotifItem.Level.DANGER, tag))

            if (tag != "—") {
                NotificationHelper.showEmergencyNotification(this, tag, diag)
            }
        }

        // 2. Jadwal Vaksinasi
        vaccines.forEach { r ->
            val tag = r.livestock?.tagNumber ?: "—"
            val due = r.nextDueDate?.take(10)
            val diag = r.diagnosis?.takeIf { it.isNotBlank() } ?: "Vaksinasi"

            val (level, subText) = if (due.isNullOrBlank()) {
                NotifItem.Level.INFO to "$diag (Jadwal belum ditentukan)"
            } else {
                try {
                    val d = LocalDate.parse(due)
                    val days = ChronoUnit.DAYS.between(today, d)
                    when {
                        days < 0 -> NotifItem.Level.DANGER to "$diag terlambat ${-days} hari ($due)"
                        days == 0L -> NotifItem.Level.DANGER to "$diag JATUH TEMPO HARI INI"
                        days <= 7 -> NotifItem.Level.WARN to "$diag jatuh tempo $days hari lagi ($due)"
                        else -> NotifItem.Level.INFO to "$diag jatuh tempo $due"
                    }
                } catch (e: Exception) {
                    NotifItem.Level.INFO to "$diag ($due)"
                }
            }

            val title = "💉 Jadwal: $tag"
            items.add(NotifItem(title, subText, level, tag))
        }

        renderVetNotifications(items)
    }

    private fun renderVetNotifications(items: List<NotifItem>) {
        binding.listNotif.removeAllViews()
        binding.tvNotifEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        binding.tvNotifBadge.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        binding.tvNotifBadge.text = items.size.toString()

        items.forEach { n ->
            val item = ItemNotifBinding.inflate(layoutInflater, binding.listNotif, false)
            item.tvItemTitle.text = n.title
            item.tvItemSub.text = n.subtitle
            item.accent.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(getColor(levelColor(n.level)))
            }
            item.root.setOnClickListener {
                startActivity(
                    Intent(this, ScanActivity::class.java)
                        .putExtra(ScanActivity.EXTRA_MODE, ScanActivity.MODE_TREATMENT)
                        .putExtra(ScanActivity.EXTRA_INITIAL_TAG, n.tagNumber)
                )
            }

            binding.listNotif.addView(item.root)
        }
    }

    @ColorRes
    private fun levelColor(level: NotifItem.Level) = when (level) {
        NotifItem.Level.INFO -> R.color.sky
        NotifItem.Level.WARN -> R.color.notif_warn
        NotifItem.Level.DANGER -> R.color.danger
        NotifItem.Level.OK -> R.color.ok
    }

    // ---------- utilitas (tile per role) ----------

    private fun buildTools(role: String) {
        binding.gridTools.removeAllViews()
        when (role) {
            "worker", "pekerja" -> {
                addTool("Scan QR", "Lihat profil sapi", R.drawable.ic_qr, R.color.tile_royal) {
                    openScan(ScanActivity.MODE_SCAN)
                }
                addTool("Timbang", "Catat berat badan", R.drawable.ic_scale, R.color.tile_sky) {
                    openScan(ScanActivity.MODE_WEIGHT)
                }
                addTool("Pakan", "Catat pakan harian", R.drawable.ic_feed, R.color.tile_ok) {
                    startActivity(Intent(this, FeedInputActivity::class.java))
                }
                addTool("Lapor sakit", "Laporan darurat", R.drawable.ic_alert, R.color.tile_danger, danger = true) {
                    openScan(ScanActivity.MODE_REPORT)
                }
            }
            "vet" -> {
                addTool("Scan QR", "Lihat profil sapi", R.drawable.ic_qr, R.color.tile_royal) {
                    openScan(ScanActivity.MODE_SCAN)
                }
                addTool("Vaksin", "Jatuh tempo", R.drawable.ic_syringe, R.color.tile_sky) {
                    startActivity(Intent(this, ReminderListActivity::class.java))
                }
                addTool("Tindakan", "Catat vaksin / obat", R.drawable.ic_clip, R.color.tile_ok) {
                    openScan(ScanActivity.MODE_TREATMENT)
                }
                addTool("Rekam medis", "Riwayat sapi", R.drawable.ic_history, R.color.tile_amber) {
                    startActivity(Intent(this, HistoryActivity::class.java))
                }
            }
            else -> {
                addTool("Dashboard", "Ringkasan peternakan", R.drawable.ic_dashboard, R.color.tile_royal) {
                    startActivity(Intent(this, DashboardActivity::class.java))
                }
                addTool("Scan QR", "Lihat profil sapi", R.drawable.ic_qr, R.color.tile_sky) {
                    openScan(ScanActivity.MODE_SCAN)
                }

            }
        }
    }

    private fun addTool(
        title: String,
        desc: String,
        @DrawableRes icon: Int,
        @ColorRes tint: Int,
        danger: Boolean = false,
        onClick: () -> Unit
    ) {
        val item = ItemToolBinding.inflate(layoutInflater, binding.gridTools, false)
        item.tvTitle.text = title
        item.ivIcon.setImageResource(icon)
        val themeColor = getColor(if (danger) R.color.danger else R.color.royal)
        item.root.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(24).toFloat()
            setColor(getColor(android.R.color.white))
            setStroke(dp(1), getColor(R.color.bento_stroke))
        }

        item.root.clipToOutline = true

        item.root.elevation = dp(4).toFloat()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            item.root.outlineSpotShadowColor = android.graphics.Color.parseColor("#33161B40")
            item.root.outlineAmbientShadowColor = android.graphics.Color.parseColor("#1F161B40")
        }


        item.ivIcon.imageTintList = ColorStateList.valueOf(themeColor)
        item.tvTitle.setTextColor(themeColor)
        item.iconBox.background = null

        item.root.setOnClickListener { onClick() }

        val margin = dp(8)
        val lp = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED),
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

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun roleLabel(role: String) = when (role) {
        "manager" -> "Manager"
        "vet" -> "Dokter hewan"
        "worker", "pekerja" -> "Pekerja"
        else -> role
    }
}
