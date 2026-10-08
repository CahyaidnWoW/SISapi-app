package com.latihan.si_sapimobile.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.latihan.si_sapimobile.databinding.ActivityEmergencyReportBinding
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.viewmodels.HealthRecordViewModel

class EmergencyReportActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_INFO = "info"
    }

    private lateinit var binding: ActivityEmergencyReportBinding
    private val viewModel: HealthRecordViewModel by viewModels()
    private lateinit var tagNumber: String

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) viewModel.processPhoto() else viewModel.discardRaw()
    }

    // CAMERA dideklarasikan di manifest, jadi izin runtime wajib sebelum membuka kamera
    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(this, "Izin kamera diperlukan untuk mengambil foto.", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyReportBinding.inflate(layoutInflater)
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

        binding.btnTakePhoto.setOnClickListener {
            if (hasCameraPermission()) launchCamera() else cameraPermission.launch(Manifest.permission.CAMERA)
        }
        binding.btnRemovePhoto.setOnClickListener { viewModel.clearPhoto() }
        binding.btnSend.setOnClickListener {
            binding.tvError.visibility = View.GONE
            viewModel.sendReport(
                tagNumber,
                binding.etSymptoms.text.toString(),
                binding.etFirstAction.text.toString()
            )
        }

        viewModel.photoState.observe(this) { state ->
            when (state) {
                null -> {
                    binding.ivPhotoPreview.setImageDrawable(null)
                    binding.tvPhotoPlaceholder.text = "Belum ada foto"
                    binding.tvPhotoPlaceholder.visibility = View.VISIBLE
                    binding.btnRemovePhoto.visibility = View.GONE
                    binding.btnTakePhoto.text = "Ambil foto"
                }
                is UiState.Loading -> {
                    binding.tvPhotoPlaceholder.text = "Memproses foto..."
                    binding.tvPhotoPlaceholder.visibility = View.VISIBLE
                    binding.ivPhotoPreview.setImageDrawable(null)
                }
                is UiState.Success -> {
                    binding.ivPhotoPreview.setImageURI(Uri.fromFile(state.data))
                    binding.tvPhotoPlaceholder.visibility = View.GONE
                    binding.btnRemovePhoto.visibility = View.VISIBLE
                    binding.btnTakePhoto.text = "Ambil ulang foto"
                }
                is UiState.Error -> {
                    binding.tvPhotoPlaceholder.text = "Belum ada foto"
                    binding.tvPhotoPlaceholder.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }

        viewModel.reportState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> setBusy(true)
                is UiState.Success -> {
                    setBusy(false)
                    Toast.makeText(this, "Laporan terkirim. Dokter hewan akan memeriksa.", Toast.LENGTH_LONG).show()
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

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun launchCamera() {
        try {
            val raw = viewModel.createRawFile()
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", raw)
            takePicture.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(
                this, "Kamera gagal dibuka: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.progressBar.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnSend.isEnabled = !busy
        binding.btnTakePhoto.isEnabled = !busy
        binding.btnRemovePhoto.isEnabled = !busy
        binding.btnSend.text = if (busy) "Mengirim..." else "Kirim laporan darurat"
    }
}
