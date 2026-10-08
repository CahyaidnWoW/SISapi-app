package com.latihan.si_sapimobile.ui

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.databinding.ActivityWeightInputBinding
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.viewmodels.WeightLogViewModel
import java.util.Locale
import kotlin.math.abs

class WeightInputActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LAST_WEIGHT = "last_weight"
        const val EXTRA_LAST_DATE = "last_date"
        private const val BIG_CHANGE_RATIO = 0.25 // berubah lebih dari 25% -> minta konfirmasi
    }

    private lateinit var binding: ActivityWeightInputBinding
    private val viewModel: WeightLogViewModel by viewModels()
    private lateinit var tagNumber: String
    private var lastWeight = -1.0
    private val idLocale: Locale = Locale.forLanguageTag("id-ID")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeightInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tagNumber = intent.getStringExtra(ScanActivity.EXTRA_TAG_NUMBER).orEmpty()
        if (tagNumber.isBlank()) {
            Toast.makeText(this, "Nomor tag sapi tidak ditemukan.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        lastWeight = intent.getDoubleExtra(EXTRA_LAST_WEIGHT, -1.0)
        val lastDate = intent.getStringExtra(EXTRA_LAST_DATE)

        binding.tvTag.text = tagNumber
        binding.tvLastWeight.text = if (lastWeight > 0) {
            "Timbangan terakhir: ${fmt(lastWeight)} kg" + (if (lastDate.isNullOrBlank()) "" else " ($lastDate)")
        } else {
            "Belum ada timbangan sebelumnya"
        }

        binding.etWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tvError.visibility = View.GONE
                updateDelta(parse(s?.toString().orEmpty()))
            }
        })

        binding.btnSave.setOnClickListener { onSaveClicked() }

        viewModel.saveState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.tvError.visibility = View.GONE
                    setBusy(true)
                }
                is UiState.Success -> {
                    setBusy(false)
                    Toast.makeText(this, "Timbangan $tagNumber tersimpan.", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                is UiState.Error -> {
                    setBusy(false)
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun parse(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

    private fun fmt(d: Double) = "%.1f".format(idLocale, d)

    private fun updateDelta(w: Double?) {
        if (lastWeight <= 0 || w == null || w <= 0) {
            binding.tvDelta.visibility = View.GONE
            return
        }
        val diff = w - lastWeight
        val (text, color) = when {
            abs(diff) < 0.05 -> "Sama dengan timbangan terakhir" to R.color.muted
            diff > 0 -> "Naik ${fmt(diff)} kg dari timbangan terakhir" to R.color.ok
            else -> "Turun ${fmt(-diff)} kg dari timbangan terakhir" to R.color.danger
        }
        binding.tvDelta.text = text
        binding.tvDelta.setTextColor(getColor(color))
        binding.tvDelta.visibility = View.VISIBLE
    }

    private fun onSaveClicked() {
        val input = binding.etWeight.text.toString()
        val w = parse(input)
        val bigChange = w != null && w > 0 && lastWeight > 0 &&
            abs(w - lastWeight) / lastWeight > BIG_CHANGE_RATIO

        if (bigChange && w != null) {
            // Cegah salah ketik, mis. 3855 padahal maksudnya 385,5
            AlertDialog.Builder(this)
                .setTitle("Berat berubah banyak")
                .setMessage(
                    "${fmt(w)} kg berbeda ${fmt(abs(w - lastWeight))} kg dari timbangan terakhir " +
                        "(${fmt(lastWeight)} kg). Pastikan angkanya sudah benar."
                )
                .setNegativeButton("Periksa lagi", null)
                .setPositiveButton("Simpan") { _, _ -> viewModel.saveWeight(tagNumber, input) }
                .show()
        } else {
            viewModel.saveWeight(tagNumber, input)
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.progressBar.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !busy
        binding.etWeight.isEnabled = !busy
        binding.btnSave.text = if (busy) "Menyimpan..." else "Simpan timbangan"
    }
}
