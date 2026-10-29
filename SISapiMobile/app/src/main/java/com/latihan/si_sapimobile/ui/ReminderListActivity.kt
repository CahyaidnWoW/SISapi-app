package com.latihan.si_sapimobile.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.databinding.ActivityReminderListBinding
import com.latihan.si_sapimobile.databinding.ItemReminderBinding
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.HealthRecord
import com.latihan.si_sapimobile.viewmodels.ReminderViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReminderListActivity : AppCompatActivity() {

    companion object {
        private const val TAB_VACCINE = "vaccine"
        private const val TAB_EMERGENCY = "emergency"
    }

    private lateinit var binding: ActivityReminderListBinding
    private val viewModel: ReminderViewModel by viewModels()
    private var activeTab = TAB_VACCINE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRetry.setOnClickListener {
            if (activeTab == TAB_VACCINE) viewModel.loadVaccines() else viewModel.loadEmergencies()
        }
        binding.rvReminders.layoutManager = LinearLayoutManager(this)
        binding.rvReminders.adapter = ReminderAdapter { record ->
            val tag = record.livestock?.tagNumber
            if (!tag.isNullOrBlank()) {
                startActivity(
                    Intent(this, ScanActivity::class.java)
                        .putExtra(ScanActivity.EXTRA_MODE, ScanActivity.MODE_TREATMENT)
                        .putExtra(ScanActivity.EXTRA_INITIAL_TAG, tag)
                )
            }
        }

        binding.tabVaccine.setOnClickListener { switchTab(TAB_VACCINE) }
        binding.tabEmergency.setOnClickListener { switchTab(TAB_EMERGENCY) }

        viewModel.vaccineState.observe(this) { state ->
            if (activeTab == TAB_VACCINE) render(state, "Tidak ada vaksin jatuh tempo.\nPengingat muncul 7 hari sebelum jadwal.")
        }
        viewModel.emergencyState.observe(this) { state ->
            if (activeTab == TAB_EMERGENCY) render(state, "Tidak ada laporan darurat.")
        }

        switchTab(TAB_VACCINE)
    }

    private fun switchTab(tab: String) {
        activeTab = tab
        updateTabUI()
        val state = if (tab == TAB_VACCINE) viewModel.vaccineState.value else viewModel.emergencyState.value
        val empty = if (tab == TAB_VACCINE) "Tidak ada vaksin jatuh tempo.\nPengingat muncul 7 hari sebelum jadwal." else "Tidak ada laporan darurat."
        render(state, empty)
    }

    private fun updateTabUI() {
        val isVaccine = activeTab == TAB_VACCINE
        val royal = getColor(R.color.royal)
        val muted = getColor(R.color.muted)

        binding.tabVaccine.setBackgroundResource(if (isVaccine) R.drawable.bg_input else 0)
        binding.tabVaccine.setTextColor(if (isVaccine) royal else muted)
        binding.tabVaccine.setTypeface(null, if (isVaccine) Typeface.BOLD else Typeface.NORMAL)

        binding.tabEmergency.setBackgroundResource(if (!isVaccine) R.drawable.bg_input else 0)
        binding.tabEmergency.setTextColor(if (!isVaccine) royal else muted)
        binding.tabEmergency.setTypeface(null, if (!isVaccine) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun render(state: UiState<List<HealthRecord>>?, emptyMessage: String) {
        when (state) {
            null, is UiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.rvReminders.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
                binding.tvError.visibility = View.GONE
                binding.btnRetry.visibility = View.GONE
            }
            is UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.tvError.visibility = View.GONE
                binding.btnRetry.visibility = View.GONE
                if (state.data.isEmpty()) {
                    binding.rvReminders.visibility = View.GONE
                    binding.tvEmpty.text = emptyMessage
                    binding.tvEmpty.visibility = View.VISIBLE
                } else {
                    binding.rvReminders.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
                    (binding.rvReminders.adapter as ReminderAdapter).submit(state.data)
                }
            }
            is UiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.rvReminders.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
                binding.tvError.text = state.message
                binding.tvError.visibility = View.VISIBLE
                binding.btnRetry.visibility = View.VISIBLE
            }
        }
    }
}

private class ReminderAdapter(val onClick: (HealthRecord) -> Unit) :
    RecyclerView.Adapter<ReminderAdapter.VH>() {

    private val items = mutableListOf<HealthRecord>()

    fun submit(newItems: List<HealthRecord>) {
        items.clear(); items.addAll(newItems); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class VH(val b: ItemReminderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(r: HealthRecord) {
            val tag = r.livestock?.tagNumber ?: "—"
            val label = r.diagnosis?.takeIf { it.isNotBlank() } ?: when (r.type) {
                "vaccination" -> "Vaksin"
                "treatment" -> "Pengobatan"
                "emergency_report" -> "Laporan darurat"
                else -> "Catatan"
            }
            b.tvTitle.text = "$tag · $label"

            val due = r.nextDueDate?.take(10)
            val today = LocalDate.now()

            // Untuk laporan darurat tidak ada next_due_date, jadi sub pakai tanggal kejadian
            if (r.type == "emergency_report") {
                b.tvSub.text = "Dilaporkan: ${r.date?.take(10) ?: "-"}"
                b.tvSub.setTextColor(b.root.context.getColor(R.color.muted))
                val t = r.treatment?.takeIf { it.isNotBlank() }
                if (t.isNullOrBlank()) {
                    b.tvChip.visibility = View.GONE
                } else {
                    b.tvChip.visibility = View.VISIBLE
                    b.tvChip.text = t
                    b.tvChip.setTextColor(b.root.context.getColor(R.color.royal))
                }
            } else {
                val (chipText, color, subText) = if (due.isNullOrBlank()) {
                    Triple("", R.color.muted, "Jadwal belum diisi")
                } else try {
                    val d = LocalDate.parse(due)
                    val days = ChronoUnit.DAYS.between(today, d)
                    when {
                        days < 0 -> Triple("Terlambat ${-days} hari", R.color.danger, "Jatuh tempo $due")
                        days == 0L -> Triple("Hari ini", R.color.danger, "Jatuh tempo $due")
                        days <= 7 -> Triple("$days hari lagi", R.color.warn, "Jatuh tempo $due")
                        else -> Triple("$days hari lagi", R.color.muted, "Jatuh tempo $due")
                    }
                } catch (e: Exception) {
                    Triple("", R.color.muted, "Jatuh tempo $due")
                }
                b.tvSub.text = subText
                b.tvSub.setTextColor(b.root.context.getColor(color))
                if (chipText.isBlank()) {
                    b.tvChip.visibility = View.GONE
                } else {
                    b.tvChip.visibility = View.VISIBLE
                    b.tvChip.text = chipText
                    b.tvChip.setTextColor(b.root.context.getColor(color))
                }
            }
            b.root.setOnClickListener { onClick(r) }
        }
    }
}