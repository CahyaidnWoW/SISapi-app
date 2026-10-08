package com.latihan.si_sapimobile.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.latihan.si_sapimobile.databinding.ActivityScanBinding
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.LivestockProfile
import com.latihan.si_sapimobile.viewmodels.ScanViewModel
import java.util.Locale

class ScanActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "mode"
        const val MODE_SCAN = "scan"
        const val MODE_WEIGHT = "weight"
        const val MODE_REPORT = "report"
        const val EXTRA_TAG_NUMBER = "tag_number"
    }

    private lateinit var binding: ActivityScanBinding
    private val viewModel: ScanViewModel by viewModels()

    // true selama request berjalan atau hasil sedang ditampilkan: scanner dijaga tetap berhenti
    private var scanLocked = false
    private var currentTag: String? = null

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            resumeScanner()
        } else {
            Toast.makeText(
                this, "Izin kamera ditolak. Kamu masih bisa mengetik nomor tag.", Toast.LENGTH_LONG
            ).show()
        }
    }

    private val scanCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            val text = result?.text ?: return
            if (scanLocked) return
            search(text)
        }

        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = when (intent.getStringExtra(EXTRA_MODE)) {
            MODE_WEIGHT -> "Timbang sapi"
            MODE_REPORT -> "Lapor sakit"
            else -> "Scan QR"
        }

        binding.barcodeScanner.setStatusText("Arahkan kamera ke QR di ear tag")
        binding.barcodeScanner.decodeContinuous(scanCallback)

        binding.btnSearch.setOnClickListener { searchFromInput() }
        binding.etTag.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFromInput()
                true
            } else false
        }
        binding.btnScanAgain.setOnClickListener { scanAgain() }

        // Dihubungkan ke Activity tujuan di batch berikutnya
        binding.btnWeight.setOnClickListener {
            // Batch 3: startActivity(Intent(this, WeightInputActivity::class.java).putExtra(EXTRA_TAG_NUMBER, currentTag))
            Toast.makeText(this, "Input timbangan dikerjakan di Batch 3.", Toast.LENGTH_SHORT).show()
        }
        binding.btnReport.setOnClickListener {
            // Batch 5: startActivity(Intent(this, EmergencyReportActivity::class.java).putExtra(EXTRA_TAG_NUMBER, currentTag))
            Toast.makeText(this, "Lapor sakit dikerjakan di Batch 5.", Toast.LENGTH_SHORT).show()
        }

        viewModel.scanState.observe(this) { state ->
            when (state) {
                null -> showIdle()
                is UiState.Loading -> showLoading()
                is UiState.Success -> showProfile(state.data)
                is UiState.Error -> showError(state.message)
            }
        }

        if (!hasCameraPermission()) cameraPermission.launch(Manifest.permission.CAMERA)
    }

    override fun onResume() {
        super.onResume()
        resumeScanner()
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeScanner.pause()
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun resumeScanner() {
        if (hasCameraPermission() && !scanLocked) binding.barcodeScanner.resume()
    }

    private fun searchFromInput() {
        hideKeyboard()
        search(binding.etTag.text.toString())
    }

    private fun search(tag: String) {
        scanLocked = true
        binding.barcodeScanner.pause()
        viewModel.fetchLivestockByTag(tag)
    }

    private fun scanAgain() {
        binding.etTag.text?.clear()
        viewModel.reset() // memicu showIdle()
    }

    // ---------- tampilan ----------

    private fun showIdle() {
        scanLocked = false
        binding.progressBar.visibility = View.GONE
        binding.tvMessage.visibility = View.GONE
        binding.cardResult.visibility = View.GONE
        binding.btnScanAgain.visibility = View.GONE
        resumeScanner()
    }

    private fun showLoading() {
        scanLocked = true
        binding.barcodeScanner.pause()
        binding.progressBar.visibility = View.VISIBLE
        binding.tvMessage.visibility = View.GONE
        binding.cardResult.visibility = View.GONE
        binding.btnScanAgain.visibility = View.GONE
    }

    private fun showError(message: String) {
        scanLocked = true
        binding.progressBar.visibility = View.GONE
        binding.cardResult.visibility = View.GONE
        binding.tvMessage.text = message
        binding.tvMessage.visibility = View.VISIBLE
        binding.btnScanAgain.visibility = View.VISIBLE
    }

    private fun showProfile(p: LivestockProfile) {
        scanLocked = true
        binding.progressBar.visibility = View.GONE
        binding.tvMessage.visibility = View.GONE

        val cow = p.livestock
        currentTag = cow.tagNumber
        binding.tvTag.text = cow.tagNumber
        binding.tvInfo.text = listOf(cow.breed?.name, genderLabel(cow.gender))
            .filter { !it.isNullOrBlank() }
            .joinToString(" · ")
            .ifEmpty { "-" }
        binding.tvCage.text = cow.cage?.name ?: "-"
        binding.tvStatus.text = cow.status?.replaceFirstChar { it.uppercase() } ?: "-"
        binding.tvWeight.text = p.latestWeight?.let {
            val w = "%.1f kg".format(Locale.forLanguageTag("id-ID"), it)
            val d = p.latestWeightDate?.take(10)
            if (d.isNullOrBlank()) w else "$w ($d)"
        } ?: "Belum ada"
        binding.tvVaccine.text = p.nextVaccineDue?.take(10)?.takeIf { it.isNotBlank() } ?: "-"

        binding.cardResult.visibility = View.VISIBLE
        binding.btnScanAgain.visibility = View.VISIBLE
    }

    private fun genderLabel(g: String?) = when (g?.lowercase()) {
        "male", "jantan", "m" -> "Jantan"
        "female", "betina", "f" -> "Betina"
        else -> g
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etTag.windowToken, 0)
    }
}
