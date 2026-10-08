package com.latihan.si_sapimobile.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapimobile.databinding.ActivityTreatmentBinding
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.viewmodels.HealthRecordViewModel
import java.time.LocalDate
import java.util.Calendar

class TreatmentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_INFO = "info"
    }

    private lateinit var binding: ActivityTreatmentBinding
    private val viewModel: HealthRecordViewModel by viewModels()
    private lateinit var tagNumber: String

    // "vaccination" atau "treatment"
    private var mode = "vaccination"

    // Jadwal berikutnya (hanya untuk vaksinasi). Default: 4 minggu dari hari ini.
    private var nextDueDate: LocalDate? = LocalDate.now().plusWeeks(4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTreatmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tagNumber = intent.getStringExtra(ScanActivity.EXTRA_TAG_NUMBER).orEmpty()
        if (tagNumber.isBlank()) {
            Toast.makeText(this, "Nomor tag sapi tidak ditemukan.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.tvTag.text = tagNumber
        val info = intent.getStringExtra(EXTRA_INFO)
        binding.tvInfo.text = info.orEmpty()
        binding.tvInfo.visibility = if (info.isNullOrBlank()) View.GONE else View.VISIBLE

        updateSegmented()
        updateDateLabel()

        binding.segVaccination.setOnClickListener { mode = "vaccination"; updateSegmented() }
        binding.segTreatment.setOnClickListener { mode = "treatment"; updateSegmented() }
        binding.btnPickDate.setOnClickListener { pickDate() }

        binding.btnSave.setOnClickListener {
            binding.tvError.visibility = View.GONE
            if (mode == "vaccination") {
                viewModel.sendVaccination(
                    tagNumber,
                    binding.etDiagnosis.text.toString(),
                    binding.etTreatment.text.toString(),
                    nextDueDate?.toString()
                )
            } else {
                viewModel.sendTreatment(
                    tagNumber,
                    binding.etDiagnosis.text.toString(),
                    binding.etTreatment.text.toString()
                )
            }
        }

        viewModel.reportState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> setBusy(true)
                is UiState.Success -> {
                    setBusy(false)
                    Toast.makeText(this, "Tindakan tersimpan.", Toast.LENGTH_SHORT).show()
                    AlertDialog.Builder(this)
                        .setTitle("Tindakan tersimpan")
                        .setMessage("Lihat riwayat kesehatan sapi $tagNumber sekarang?")
                        .setNegativeButton("Nanti") { _, _ ->
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                        .setPositiveButton("Lihat") { _, _ ->
                            startActivity(
                                Intent(this, HistoryActivity::class.java)
                                    .putExtra(HistoryActivity.EXTRA_INITIAL_TAG, tagNumber)
                            )
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                }
                is UiState.Error -> {
                    setBusy(false)
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateSegmented() {
        val isVaccine = mode == "vaccination"
        binding.segVaccination.setBackgroundResource(if (isVaccine) com.latihan.si_sapimobile.R.drawable.bg_input else 0)
        binding.segTreatment.setBackgroundResource(if (!isVaccine) com.latihan.si_sapimobile.R.drawable.bg_input else 0)
        binding.tvDiagnosisLabel.text = if (isVaccine) "Jenis vaksin" else "Diagnosis / penyakit"
        binding.tvDiagnosisHint.visibility = View.GONE
        binding.groupNextDue.visibility = if (isVaccine) View.VISIBLE else View.GONE
    }

    private fun pickDate() {
        val cal = Calendar.getInstance()
        val initial = nextDueDate ?: LocalDate.now().plusWeeks(4)
        cal.set(initial.year, initial.monthValue - 1, initial.dayOfMonth)
        DatePickerDialog(this, { _, y, m, d ->
            nextDueDate = LocalDate.of(y, m + 1, d)
            updateDateLabel()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateLabel() {
        val d = nextDueDate
        binding.tvNextDue.text = if (d == null) "Pilih tanggal" else d.toString()
    }

    private fun setBusy(busy: Boolean) {
        binding.progressBar.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !busy
        binding.btnSave.text = if (busy) "Menyimpan..." else "Simpan tindakan"
    }
}