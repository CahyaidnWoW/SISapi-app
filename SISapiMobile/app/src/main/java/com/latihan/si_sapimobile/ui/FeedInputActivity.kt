package com.latihan.si_sapimobile.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.databinding.ActivityFeedInputBinding
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.Cage
import com.latihan.si_sapimobile.models.FeedStock
import com.latihan.si_sapimobile.viewmodels.FeedViewModel
import java.util.Locale

class FeedInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedInputBinding
    private val viewModel: FeedViewModel by viewModels()

    private var cages: List<Cage> = emptyList()
    private var feeds: List<FeedStock> = emptyList()
    private var selectedCageId: Int? = null
    private var selectedFeedId: Int? = null
    private var loaded = false
    private var saving = false
    private val idLocale: Locale = Locale.forLanguageTag("id-ID")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.spCage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, position: Int, id: Long) {
                selectedCageId = cages.getOrNull(position)?.id
            }

            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        binding.spFeed.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, position: Int, id: Long) {
                selectedFeedId = feeds.getOrNull(position)?.id
                updateStockInfo()
            }

            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tvError.visibility = View.GONE
                updateStockInfo()
            }
        })

        binding.btnSave.setOnClickListener {
            viewModel.save(selectedCageId, selectedFeedId, binding.etAmount.text.toString())
        }
        binding.btnRetry.setOnClickListener { viewModel.loadOptions() }

        viewModel.optionsState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                    refreshButtons()
                }
                is UiState.Success -> {
                    loaded = true
                    binding.progressBar.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE
                    bindOptions(state.data.cages, state.data.feeds)
                    refreshButtons()
                }
                is UiState.Error -> {
                    loaded = false
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.VISIBLE
                    refreshButtons()
                }
            }
        }

        viewModel.saveState.observe(this) { state ->
            when (state) {
                null -> {}
                is UiState.Loading -> {
                    saving = true
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                    refreshButtons()
                }
                is UiState.Success -> {
                    saving = false
                    Toast.makeText(this, "Pemakaian pakan tersimpan.", Toast.LENGTH_SHORT).show()
                    binding.etAmount.text?.clear()
                    viewModel.clearSaveState()
                    viewModel.loadOptions() // muat ulang supaya "Stok tersedia" ikut berkurang
                }
                is UiState.Error -> {
                    saving = false
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                    refreshButtons()
                }
            }
        }
    }

    private fun bindOptions(newCages: List<Cage>, newFeeds: List<FeedStock>) {
        cages = newCages
        feeds = newFeeds

        binding.spCage.adapter = adapterOf(cages.map { it.name?.takeIf { n -> n.isNotBlank() } ?: "Kandang ${it.id}" })
        binding.spFeed.adapter = adapterOf(feeds.map { it.feedName })

        // Pertahankan pilihan terakhir setelah data dimuat ulang
        selectedCageId?.let { id ->
            val index = cages.indexOfFirst { it.id == id }
            if (index >= 0) binding.spCage.setSelection(index)
        }
        selectedFeedId?.let { id ->
            val index = feeds.indexOfFirst { it.id == id }
            if (index >= 0) binding.spFeed.setSelection(index)
        }
        updateStockInfo()
    }

    private fun adapterOf(items: List<String>) =
        ArrayAdapter(this, R.layout.item_spinner, R.id.tvItem, items)

    private fun parse(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

    private fun fmt(d: Double) = "%.1f".format(idLocale, d)

    private fun updateStockInfo() {
        val feed = feeds.firstOrNull { it.id == selectedFeedId }
        if (feed == null) {
            binding.tvStock.visibility = View.GONE
            binding.tvRemain.visibility = View.GONE
            return
        }
        val unit = feed.unit?.takeIf { it.isNotBlank() } ?: "kg"
        val stock = feed.totalStock ?: 0.0

        binding.tvAmountLabel.text = "Jumlah dipakai ($unit)"

        if (stock <= 0.0) {
            binding.tvStock.text = "Stok ${feed.feedName} kosong. Tambah stok lewat aplikasi desktop."
            binding.tvStock.setTextColor(getColor(R.color.danger))
        } else {
            binding.tvStock.text = "Stok tersedia: ${fmt(stock)} $unit"
            binding.tvStock.setTextColor(getColor(R.color.muted))
        }
        binding.tvStock.visibility = View.VISIBLE

        val amount = parse(binding.etAmount.text.toString())
        if (amount != null && amount > 0) {
            if (amount > stock) {
                binding.tvRemain.text = "Melebihi stok yang tersedia (${fmt(stock)} $unit)"
                binding.tvRemain.setTextColor(getColor(R.color.danger))
            } else {
                binding.tvRemain.text = "Sisa stok setelah ini: ${fmt(stock - amount)} $unit"
                binding.tvRemain.setTextColor(getColor(R.color.muted))
            }
            binding.tvRemain.visibility = View.VISIBLE
        } else {
            binding.tvRemain.visibility = View.GONE
        }
    }

    private fun refreshButtons() {
        binding.btnSave.isEnabled = loaded && !saving
        binding.btnSave.text = if (saving) "Menyimpan..." else "Simpan pemakaian"
    }
}
