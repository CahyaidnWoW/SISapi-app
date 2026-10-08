package com.latihan.si_sapimobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.databinding.ActivityDashboardBinding
import com.latihan.si_sapimobile.databinding.ItemReminderBinding
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.HealthRecord
import com.latihan.si_sapimobile.viewmodels.DashboardViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRetry.setOnClickListener { viewModel.load() }
        binding.rvDue.layoutManager = LinearLayoutManager(this)
        binding.rvDue.adapter = DueAdapter { record ->
            val tag = record.livestock?.tagNumber
            if (!tag.isNullOrBlank()) {
                startActivity(
                    Intent(this, ScanActivity::class.java)
                        .putExtra(ScanActivity.EXTRA_MODE, ScanActivity.MODE_SCAN)
                        .putExtra(ScanActivity.EXTRA_INITIAL_TAG, tag)
                )
            }
        }

        viewModel.state.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.content.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE
                    binding.content.visibility = View.VISIBLE

                    binding.tvTotalLivestock.text = state.data.totalLivestock.toString()
                    binding.tvTotalCages.text = state.data.totalCages.toString()
                    binding.tvTotalDue.text = state.data.dueVaccinations.size.toString()

                    if (state.data.dueVaccinations.isEmpty()) {
                        binding.tvDueEmpty.visibility = View.VISIBLE
                        binding.rvDue.visibility = View.GONE
                    } else {
                        binding.tvDueEmpty.visibility = View.GONE
                        binding.rvDue.visibility = View.VISIBLE
                        (binding.rvDue.adapter as DueAdapter).submit(state.data.dueVaccinations)
                    }
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.content.visibility = View.GONE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.VISIBLE
                }
            }
        }
    }
}

private class DueAdapter(val onClick: (HealthRecord) -> Unit) :
    RecyclerView.Adapter<DueAdapter.VH>() {

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
            val label = r.diagnosis?.takeIf { it.isNotBlank() } ?: "Vaksin"
            b.tvTitle.text = "$tag · $label"

            val due = r.nextDueDate?.take(10)
            val today = LocalDate.now()
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
            b.root.setOnClickListener { onClick(r) }
        }
    }
}