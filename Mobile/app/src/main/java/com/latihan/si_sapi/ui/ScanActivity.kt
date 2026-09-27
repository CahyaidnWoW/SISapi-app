package com.latihan.si_sapi.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.latihan.si_sapi.R
import com.latihan.si_sapi.helpers.UiState
import com.latihan.si_sapi.viewmodels.ScanViewModel

class ScanActivity : AppCompatActivity() {

    private val viewModel: ScanViewModel by viewModels()

    private lateinit var barcodeScanner: DecoratedBarcodeView
    private lateinit var etManualTag: EditText
    private lateinit var btnSearchManual: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvScanResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        initViews()
        observeViewModel()
        setupScanner()

        btnSearchManual.setOnClickListener {
            val tag = etManualTag.text.toString().trim()
            if (tag.isNotEmpty()) {
                viewModel.fetchLivestockByTag(tag)
            } else {
                Toast.makeText(this, "Masukkan nomor tag terlebih dahulu!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews() {
        barcodeScanner = findViewById(R.id.barcodeScanner)
        etManualTag = findViewById(R.id.etManualTag)
        btnSearchManual = findViewById(R.id.btnSearchManual)
        progressBar = findViewById(R.id.progressBar)
        tvScanResult = findViewById(R.id.tvScanResult)
    }

    private fun setupScanner() {
        barcodeScanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.text?.let { tagNumber ->
                    barcodeScanner.pause() // Pause scanner agar tidak mentrigger API berkali-kali
                    viewModel.fetchLivestockByTag(tagNumber)
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.scanState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvScanResult.text = ""
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    val profile = state.data
                    val cow = profile.livestock

                    tvScanResult.text = """
                        --- PROFIL SAPI ---
                        Nomor Tag    : ${cow.tagNumber}
                        Jenis Ras    : ${cow.breed?.breedName ?: "-"}
                        Kandang      : ${cow.cage?.name ?: "-"}
                        Jenis Kelamin: ${cow.gender}
                        Berat Terakhir: ${profile.latestWeight ?: cow.initialWeight} kg
                        Status Sapi  : ${cow.status}
                    """.trimIndent()
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    barcodeScanner.resume() // Resume scanner jika query gagal/sapi tidak ditemukan
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeScanner.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeScanner.pause()
    }
}