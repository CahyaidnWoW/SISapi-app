package com.latihan.si_sapimobile.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.databinding.ActivityScanBinding
import com.latihan.si_sapimobile.helpers.SessionManager
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.LivestockProfile
import com.latihan.si_sapimobile.viewmodels.ScanViewModel
import java.time.LocalDate
import java.util.Locale

class ScanActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "mode"
        const val MODE_SCAN = "scan"
        const val MODE_WEIGHT = "weight"
        const val MODE_REPORT = "report"
        const val MODE_TREATMENT = "treatment"
        const val EXTRA_TAG_NUMBER = "tag_number"
        const val EXTRA_INITIAL_TAG = "initial_tag"
        private const val STATE_ROUTED_TAG = "routed_tag"
    }

    private lateinit var binding: ActivityScanBinding
    private val viewModel: ScanViewModel by viewModels()

    private var mode = MODE_SCAN

    // true selama request berjalan atau hasil sedang ditampilkan: scanner dijaga tetap berhenti
    private var scanLocked = false
    private var currentProfile: LivestockProfile? = null

    // Tag yang sudah otomatis diteruskan ke form. Mencegah form terbuka dua kali saat layar diputar.
    private var routedForTag: String? = null

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

    private val weightLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) onWeightSaved()
    }

    private val reportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) onReportSent()
    }

    private val treatmentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) onTreatmentSaved()
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

        routedForTag = savedInstanceState?.getString(STATE_ROUTED_TAG)
        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_SCAN

        binding.tvTitle.text = when (mode) {
            MODE_WEIGHT -> "Timbang sapi"
            MODE_REPORT -> "Lapor sakit"
            MODE_TREATMENT -> "Catat tindakan"
            else -> "Scan QR"
        }

        binding.btnTreatment.setOnClickListener { currentProfile?.let { openTreatment(it) } }
        binding.btnTreatment.visibility =
            if (SessionManager(this).getUserRole().lowercase() == "vet") View.VISIBLE else View.GONE

        binding.btnHistory.setOnClickListener {
            currentProfile?.livestock?.tagNumber?.let { tag ->
                startActivity(
                    Intent(this, HistoryActivity::class.java)
                        .putExtra(HistoryActivity.EXTRA_INITIAL_TAG, tag)
                )
            }
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

        val role = SessionManager(this).getUserRole().lowercase()
        binding.btnWeight.visibility = if (role == "manager") View.GONE else View.VISIBLE
        binding.btnReport.visibility = if (role == "vet") View.GONE else View.VISIBLE

        binding.btnWeight.setOnClickListener { currentProfile?.let { openWeight(it) } }
        binding.btnReport.setOnClickListener { currentProfile?.let { openReport(it) } }

        viewModel.scanState.observe(this) { state ->
            when (state) {
                null -> showIdle()
                is UiState.Loading -> showLoading()
                is UiState.Success -> showProfile(state.data)
                is UiState.Error -> showError(state.message)
            }
        }

        // Auto-cari tag kalau dibuka dari Pengingat vaksin
        val initialTag = intent.getStringExtra(EXTRA_INITIAL_TAG)
        if (!initialTag.isNullOrBlank() && viewModel.scanState.value == null) {
            binding.etTag.setText(initialTag)
            search(initialTag)
        } else if (!hasCameraPermission()) {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_ROUTED_TAG, routedForTag)
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

    // ---------- alur ke Activity lain ----------

    private fun openWeight(p: LivestockProfile) {
        val intent = Intent(this, WeightInputActivity::class.java)
            .putExtra(EXTRA_TAG_NUMBER, p.livestock.tagNumber)
            .putExtra(WeightInputActivity.EXTRA_LAST_WEIGHT, p.latestWeight ?: -1.0)
            .putExtra(WeightInputActivity.EXTRA_LAST_DATE, p.latestWeightDate?.take(10))
        weightLauncher.launch(intent)
    }

    private fun openReport(p: LivestockProfile) {
        val cow = p.livestock
        val info = listOf(cow.breed?.name, genderLabel(cow.gender), cow.cage?.name)
            .filter { !it.isNullOrBlank() }
            .joinToString(" · ")
        val intent = Intent(this, EmergencyReportActivity::class.java)
            .putExtra(EXTRA_TAG_NUMBER, cow.tagNumber)
            .putExtra(EmergencyReportActivity.EXTRA_INFO, info)
        reportLauncher.launch(intent)
    }

    private fun openTreatment(p: LivestockProfile) {
        val cow = p.livestock
        val info = listOf(cow.breed?.name, genderLabel(cow.gender), cow.cage?.name)
            .filter { !it.isNullOrBlank() }
            .joinToString(" · ")
        val intent = Intent(this, TreatmentActivity::class.java)
            .putExtra(EXTRA_TAG_NUMBER, cow.tagNumber)
            .putExtra(TreatmentActivity.EXTRA_INFO, info)
        treatmentLauncher.launch(intent)
    }

    private fun onReportSent() {
        if (mode == MODE_REPORT) scanAgain()
    }

    private fun onWeightSaved() {
        if (mode == MODE_WEIGHT) {
            scanAgain()
        } else {
            currentProfile?.livestock?.tagNumber?.let { search(it) }
        }
    }

    private fun onTreatmentSaved() {
        if (mode == MODE_TREATMENT) finish()
    }

    // ---------- tampilan ----------

    private fun showIdle() {
        scanLocked = false
        routedForTag = null
        currentProfile = null
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
        currentProfile = p
        binding.progressBar.visibility = View.GONE
        binding.tvMessage.visibility = View.GONE

        val cow = p.livestock
        binding.tvTag.text = cow.tagNumber
        binding.tvInfo.text = listOf(cow.breed?.name, genderLabel(cow.gender))
            .filter { !it.isNullOrBlank() }
            .joinToString(" · ")
            .ifEmpty { "-" }
        binding.tvCage.text = cow.cage?.name ?: "-"
        binding.tvStatus.text = statusLabel(cow.status)
        binding.tvWeight.text = p.latestWeight?.let {
            val w = "%.1f kg".format(Locale.forLanguageTag("id-ID"), it)
            val d = p.latestWeightDate?.take(10)
            if (d.isNullOrBlank()) w else "$w ($d)"
        } ?: "Belum ada"

        val due = p.nextVaccineDue?.take(10)?.takeIf { it.isNotBlank() }
        when {
            due == null -> {
                binding.tvVaccine.text = "-"
                binding.tvVaccine.setTextColor(getColor(R.color.ink))
            }
            isPast(due) -> {
                binding.tvVaccine.text = "$due (terlambat)"
                binding.tvVaccine.setTextColor(getColor(R.color.danger))
            }
            else -> {
                binding.tvVaccine.text = due
                binding.tvVaccine.setTextColor(getColor(R.color.ink))
            }
        }

        

        // Mode Timbang / Lapor sakit / Catat tindakan: setelah sapi ditemukan, langsung ke form tujuan
        if ((mode == MODE_WEIGHT || mode == MODE_REPORT || mode == MODE_TREATMENT)
            && routedForTag != cow.tagNumber
        ) {
            routedForTag = cow.tagNumber
            when (mode) {
                MODE_WEIGHT -> openWeight(p)
                MODE_REPORT -> openReport(p)
                MODE_TREATMENT -> openTreatment(p)
            }

            return
        }

        binding.cardResult.visibility = View.VISIBLE
        binding.btnScanAgain.visibility = View.VISIBLE
    }

    private fun genderLabel(g: String?) = when (g?.lowercase()) {
        "male", "jantan", "m" -> "Jantan"
        "female", "betina", "f" -> "Betina"
        else -> g
    }

    private fun statusLabel(s: String?) = when (s?.lowercase()) {
        null, "" -> "-"
        "healthy" -> "Sehat"
        "sick" -> "Sakit"
        else -> s?.replaceFirstChar { it.uppercase() } ?: "-"
    }

    private fun isPast(date: String): Boolean = try {
        LocalDate.parse(date).isBefore(LocalDate.now())
    } catch (e: Exception) {
        false
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etTag.windowToken, 0)
    }
}