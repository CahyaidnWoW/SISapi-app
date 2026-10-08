package com.latihan.si_sapimobile.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.databinding.ActivityHistoryBinding
import com.latihan.si_sapimobile.databinding.ItemHistoryBinding
import com.latihan.si_sapimobile.helpers.BottomNav
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.HealthRecord
import com.latihan.si_sapimobile.viewmodels.HistoryViewModel

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by viewModels()

    companion object {
        const val EXTRA_INITIAL_TAG = "initial_tag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = HistoryAdapter()

        binding.btnSearch.setOnClickListener { doSearch() }
        binding.etTag.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch()
                true
            } else false
        }

        viewModel.state.observe(this) { state ->
            when (state) {
                null -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvMessage.visibility = View.GONE
                    binding.rvHistory.visibility = View.GONE
                }
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvMessage.visibility = View.GONE
                    binding.rvHistory.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvMessage.visibility = View.GONE
                    if (state.data.isEmpty()) {
                        binding.tvMessage.text = "Belum ada riwayat kesehatan untuk sapi ini."
                        binding.tvMessage.visibility = View.VISIBLE
                        binding.rvHistory.visibility = View.GONE
                    } else {
                        binding.rvHistory.visibility = View.VISIBLE
                        (binding.rvHistory.adapter as HistoryAdapter).submit(state.data)
                    }
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvHistory.visibility = View.GONE
                    binding.tvMessage.text = state.message
                    binding.tvMessage.visibility = View.VISIBLE
                }
            }
        }

        BottomNav.setup(this, BottomNav.TAB_HISTORY)
        // Auto-cari kalau dibuka dari profil sapi
        val initialTag = intent.getStringExtra(EXTRA_INITIAL_TAG)
        if (!initialTag.isNullOrBlank()) {
            binding.etTag.setText(initialTag)
            viewModel.load(initialTag)
        }
    }

    private fun doSearch() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etTag.windowToken, 0)
        viewModel.load(binding.etTag.text.toString())
    }
}

private class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.VH>() {
    private val items = mutableListOf<HealthRecord>()

    fun submit(newItems: List<HealthRecord>) {
        items.clear(); items.addAll(newItems); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class VH(val b: ItemHistoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(r: HealthRecord) {
            val (label, color) = when (r.type) {
                "vaccination" -> "Vaksinasi" to R.color.ok
                "treatment" -> "Pengobatan" to R.color.royal
                "emergency_report" -> "Darurat" to R.color.danger
                else -> (r.type ?: "-") to R.color.muted
            }
            b.tvType.text = label
            b.tvType.setTextColor(b.root.context.getColor(color))

            b.tvDate.text = r.date?.take(10) ?: "-"
            b.tvDiagnosis.text = r.diagnosis?.takeIf { it.isNotBlank() } ?: "—"
            val t = r.treatment?.takeIf { it.isNotBlank() }
            b.tvTreatment.text = t ?: ""
            b.tvTreatment.visibility = if (t == null) View.GONE else View.VISIBLE

            val due = r.nextDueDate?.take(10)
            if (due.isNullOrBlank()) {
                b.tvNextDue.visibility = View.GONE
            } else {
                b.tvNextDue.visibility = View.VISIBLE
                b.tvNextDue.text = "Jadwal berikutnya: $due"
            }
        }
    }
}