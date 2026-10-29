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
import com.latihan.si_sapimobile.databinding.ActivityDashboardBinding
import com.latihan.si_sapimobile.databinding.ItemReminderBinding
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.UpcomingMedicalItem
import com.latihan.si_sapimobile.viewmodels.DashboardViewModel
import java.util.Locale

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
        binding.rvDue.adapter = UpcomingMedicalAdapter { item ->
            if (item.cattleTag.isNotBlank() && item.cattleTag != "-") {
                startActivity(
                    Intent(this, ScanActivity::class.java)
                        .putExtra(ScanActivity.EXTRA_MODE, ScanActivity.MODE_TREATMENT)
                        .putExtra(ScanActivity.EXTRA_INITIAL_TAG, item.cattleTag)
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

                    val data = state.data

                    // 1. Total Populasi
                    binding.tvTotalCattle.text = data.totalCattle.toString()

                    // 2. Rata-rata ADG 30 Hari
                    binding.tvAvgAdg.text = String.format(Locale.US, "%.2f kg", data.avgAdg30d)

                    // 3. Siap Jual (>= 450kg)
                    binding.tvReadyToSell.text = data.readyToSell.toString()

                    // 4. Kondisi Kritis / Sakit
                    binding.tvSickCattle.text = data.sickCattle.toString()

                    // 5. Pakan Kritis
                    binding.tvCriticalFeed.text = "${data.criticalFeedCount} jenis pakan"

                    // 6. Jadwal Penanganan Medis 7 Hari
                    if (data.upcomingMedicals.isEmpty()) {
                        binding.tvDueEmpty.visibility = View.VISIBLE
                        binding.rvDue.visibility = View.GONE
                    } else {
                        binding.tvDueEmpty.visibility = View.GONE
                        binding.rvDue.visibility = View.VISIBLE
                        (binding.rvDue.adapter as UpcomingMedicalAdapter).submit(data.upcomingMedicals)
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

private class UpcomingMedicalAdapter(val onClick: (UpcomingMedicalItem) -> Unit) :
    RecyclerView.Adapter<UpcomingMedicalAdapter.VH>() {

    private val items = mutableListOf<UpcomingMedicalItem>()

    fun submit(newItems: List<UpcomingMedicalItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class VH(val b: ItemReminderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: UpcomingMedicalItem) {
            b.tvTitle.text = "${item.cattleTag} · ${item.treatmentType}"
            b.tvSub.text = "Rencana pelaksanaan: ${item.scheduledDate}"
            b.tvChip.visibility = View.VISIBLE
            b.tvChip.text = item.status
            b.root.setOnClickListener { onClick(item) }
        }
    }
}
