# Si-Sapi Mobile, Batch 3: Input Timbangan

Package: `com.latihan.si_sapimobile`. Syarat: Batch 2 sudah jalan (scan QR berhasil).

## 1. Salin file
Isi `app/src/main/` disalin ke project.
**Timpa (Replace)** 3 file lama: `network/ApiService.kt`, `ui/ScanActivity.kt`, `res/layout/activity_scan.xml`.
**File baru:** `models/WeightLogs.kt`, `services/WeightLogService.kt`, `viewmodels/WeightLogViewModel.kt`, `ui/WeightInputActivity.kt`, `res/layout/activity_weight_input.xml`.

## 2. Daftarkan di AndroidManifest.xml
Di dalam `<application>`:
```xml
<activity android:name=".ui.WeightInputActivity" android:exported="false" />
```

## 3. Yang berubah
- **Mode Timbang:** scan (atau ketik tag) → setelah sapi ditemukan, form timbangan langsung terbuka. Setelah disimpan, kembali ke kamera yang siap memindai sapi berikutnya.
- **Mode Scan QR biasa:** tombol "Catat timbangan" di profil membuka form yang sama. Setelah disimpan, profil dimuat ulang sehingga "Berat terakhir" ikut berubah.
- **Form timbangan:** menampilkan tag dan timbangan terakhir, selisih langsung muncul saat mengetik ("Naik 16,5 kg ..."), dan ada konfirmasi kalau berat berubah lebih dari 25% (mencegah salah ketik, mis. 3855 padahal 385,5).
- **Profil sapi:** vaksin yang tanggalnya sudah lewat ditandai merah "(terlambat)". Status "Healthy" ditampilkan "Sehat".
- **Teks tombol** "Cari" dan "Catat timbangan" dipaksa putih.
- Kalau teks tombol **Masuk** di layar Login juga terlihat gelap, tambahkan `android:textColor="#FFFFFF"` pada `btnLogin` di `activity_login.xml`.

## 4. Uji (HP asli, urut)
1. Beranda → **Timbang** → scan QR `SP-001` → form timbangan terbuka sendiri, tag dan "Timbangan terakhir: 385,5 kg (2026-09-27)" tampil.
2. Ketik `390` → muncul "Naik 4,5 kg dari timbangan terakhir" (hijau). Ketik `380` → "Turun 5,5 kg" (merah).
3. Ketik `3855` → tekan Simpan → dialog "Berat berubah banyak" → pilih **Periksa lagi**. Perbaiki ke `385,5` atau `390`, lalu Simpan.
4. Setelah simpan: Toast "Timbangan SP-001 tersimpan.", kembali ke kamera yang siap scan.
5. **Cek database:** tabel `weight_logs` punya baris baru (berat, tanggal hari ini, `livestock_id` benar, `worker_id` terisi akun yang login).
6. Beranda → **Scan QR** → scan `SP-001` → "Berat terakhir" sekarang berisi angka yang baru kamu simpan. Tekan "Catat timbangan" dari profil → simpan lagi → profil ter-refresh.
7. Kolom kosong → Simpan → "Berat badan wajib diisi." Isi `0` → "Berat harus lebih dari 0 kg." (tanpa menembak server).
8. Matikan `php artisan serve`, Simpan → kotak merah "Tidak bisa terhubung ke server...". Nyalakan lagi, Simpan → berhasil.
9. Putar layar saat form timbangan terbuka di mode Timbang: form tidak terbuka dobel.

## 5. Kalau ada yang tidak sesuai
| Gejala | Penyebab / tindakan |
|---|---|
| Crash saat buka form timbangan | Lupa daftarkan `WeightInputActivity` di manifest |
| Kotak merah dengan pesan Laravel (mis. "The weight field must be ...") | Aturan validasi backend. Kirim pesannya, kita sesuaikan |
| 404 / "tidak ditemukan" padahal sapi ada | Path salah. Pastikan di `ApiService`: `livestocks/{tagNumber}/weight-logs` |
| Berhasil tapi "Berat terakhir" tidak berubah | Cek `weight_logs` di database. Kalau barisnya ada tapi profil lama, lihat JSON `GET livestocks/SP-001` di Logcat (filter `OkHttp`) |
| `Unresolved reference ActivityWeightInputBinding` | Belum Sync / file layout belum disalin. Sync lalu Rebuild |

## 6. Setelah lolos uji
Kirim hasil uji 5 dan 6 (baris di database dan profil yang ter-refresh), dan pesan error kalau ada. Setelah itu Batch 4 (Pakan).


---

# Kode lengkap (salin per file)

Setiap blok diawali path file di dalam project. Buat file dengan nama dan folder itu (atau timpa yang lama), lalu tempel isinya.

## `app/src/main/java/com/latihan/si_sapimobile/models/WeightLogs.kt`

```kotlin
package com.latihan.si_sapimobile.models

import com.google.gson.annotations.SerializedName

data class WeightLog(
    val id: Int? = null,
    @SerializedName("livestock_id") val livestockId: Int? = null,
    val weight: Double? = null,
    val date: String? = null
)

// Backend memakai field `weight` dan `date` (date wajib, format yyyy-MM-dd)
data class CreateWeightLogRequest(
    val weight: Double,
    val date: String
)
```

## `app/src/main/java/com/latihan/si_sapimobile/network/ApiService.kt`

```kotlin
package com.latihan.si_sapimobile.network

import com.latihan.si_sapimobile.models.CreateWeightLogRequest
import com.latihan.si_sapimobile.models.LivestockProfile
import com.latihan.si_sapimobile.models.LoginRequest
import com.latihan.si_sapimobile.models.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Backend mengembalikan JSON mentah (tidak dibungkus success/data).
interface ApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<ResponseBody>

    // Batch 2
    @GET("livestocks/{tagNumber}")
    suspend fun getLivestockByTag(@Path("tagNumber") tagNumber: String): Response<LivestockProfile>

    // Batch 3: path yang benar adalah weight-logs (bukan weight)
    @POST("livestocks/{tagNumber}/weight-logs")
    suspend fun recordWeight(
        @Path("tagNumber") tagNumber: String,
        @Body body: CreateWeightLogRequest
    ): Response<ResponseBody>
}
```

## `app/src/main/java/com/latihan/si_sapimobile/services/WeightLogService.kt`

```kotlin
package com.latihan.si_sapimobile.services

import android.content.Context
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.helpers.errorMessage
import com.latihan.si_sapimobile.helpers.todayString
import com.latihan.si_sapimobile.models.CreateWeightLogRequest
import com.latihan.si_sapimobile.network.ApiClient
import java.io.IOException

class WeightLogService(context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun recordWeight(tagNumber: String, weight: Double): UiState<Unit> = try {
        val res = api.recordWeight(tagNumber, CreateWeightLogRequest(weight, todayString()))
        when {
            res.isSuccessful -> UiState.Success(Unit)
            res.code() == 404 -> UiState.Error("Sapi dengan tag $tagNumber tidak ditemukan.")
            res.code() == 401 -> UiState.Error("Sesi berakhir. Keluar lalu masuk lagi.")
            else -> UiState.Error(res.errorMessage("Gagal menyimpan timbangan."))
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }
}
```

## `app/src/main/java/com/latihan/si_sapimobile/ui/ScanActivity.kt`

```kotlin
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
        const val EXTRA_TAG_NUMBER = "tag_number"
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

    // Hasil dari WeightInputActivity
    private val weightLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) onWeightSaved()
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

        binding.btnWeight.setOnClickListener { currentProfile?.let { openWeight(it) } }
        binding.btnReport.setOnClickListener {
            // Batch 5: startActivity(Intent(this, EmergencyReportActivity::class.java).putExtra(EXTRA_TAG_NUMBER, currentProfile?.livestock?.tagNumber))
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

    private fun onWeightSaved() {
        if (mode == MODE_WEIGHT) {
            // Siap menimbang sapi berikutnya
            scanAgain()
        } else {
            // Muat ulang profil supaya "berat terakhir" ikut berubah
            currentProfile?.livestock?.tagNumber?.let { search(it) }
        }
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

        binding.cardResult.visibility = View.VISIBLE
        binding.btnScanAgain.visibility = View.VISIBLE

        // Mode Timbang: setelah sapi ditemukan, langsung ke form timbangan (sekali per sapi)
        if (mode == MODE_WEIGHT && routedForTag != cow.tagNumber) {
            routedForTag = cow.tagNumber
            openWeight(p)
        }
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
```

## `app/src/main/java/com/latihan/si_sapimobile/ui/WeightInputActivity.kt`

```kotlin
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
```

## `app/src/main/java/com/latihan/si_sapimobile/viewmodels/WeightLogViewModel.kt`

```kotlin
package com.latihan.si_sapimobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.services.WeightLogService
import kotlinx.coroutines.launch

class WeightLogViewModel(app: Application) : AndroidViewModel(app) {
    private val service = WeightLogService(app)

    private val _saveState = MutableLiveData<UiState<Unit>>()
    val saveState: LiveData<UiState<Unit>> = _saveState

    fun saveWeight(tagNumber: String, weightText: String) {
        val weight = weightText.trim().replace(',', '.').toDoubleOrNull()
        if (weightText.isBlank()) { _saveState.value = UiState.Error("Berat badan wajib diisi."); return }
        if (weight == null) { _saveState.value = UiState.Error("Berat harus berupa angka."); return }
        if (weight <= 0.0) { _saveState.value = UiState.Error("Berat harus lebih dari 0 kg."); return }
        if (_saveState.value is UiState.Loading) return // cegah ketuk ganda
        _saveState.value = UiState.Loading
        viewModelScope.launch { _saveState.postValue(service.recordWeight(tagNumber, weight)) }
    }
}
```

## `app/src/main/res/layout/activity_scan.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/mist"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Scan QR"
            android:textColor="@color/navy"
            android:textSize="24sp"
            android:textStyle="bold" />

        <com.journeyapps.barcodescanner.DecoratedBarcodeView
            android:id="@+id/barcodeScanner"
            android:layout_width="match_parent"
            android:layout_height="300dp"
            android:layout_marginTop="12dp" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="Atau ketik nomor tag"
            android:textColor="@color/navy"
            android:textSize="14sp"
            android:textStyle="bold" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="6dp"
            android:orientation="horizontal">

            <EditText
                android:id="@+id/etTag"
                android:layout_width="0dp"
                android:layout_height="52dp"
                android:layout_weight="1"
                android:background="@drawable/bg_input"
                android:hint="Contoh: SP-001"
                android:imeOptions="actionSearch"
                android:importantForAutofill="no"
                android:inputType="textCapCharacters|textNoSuggestions"
                android:maxLines="1"
                android:paddingStart="14dp"
                android:paddingEnd="14dp"
                android:textColor="@color/ink"
                android:textColorHint="#6B7799"
                android:textSize="16sp" />

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnSearch"
                android:layout_width="wrap_content"
                android:layout_height="52dp"
                android:layout_marginStart="8dp"
                android:text="Cari"
                android:textAllCaps="false"
                android:textColor="#FFFFFF"
                android:textSize="16sp"
                android:textStyle="bold"
                app:backgroundTint="@color/royal"
                app:cornerRadius="12dp" />
        </LinearLayout>

        <ProgressBar
            android:id="@+id/progressBar"
            style="?android:attr/progressBarStyleSmall"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:layout_marginTop="16dp"
            android:visibility="gone" />

        <TextView
            android:id="@+id/tvMessage"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:background="@drawable/bg_error"
            android:padding="12dp"
            android:textColor="@color/danger"
            android:textSize="14sp"
            android:visibility="gone" />

        <LinearLayout
            android:id="@+id/cardResult"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:background="@drawable/bg_card"
            android:orientation="vertical"
            android:padding="16dp"
            android:visibility="gone">

            <TextView
                android:id="@+id/tvTag"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="@color/navy"
                android:textSize="28sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/tvInfo"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="2dp"
                android:textColor="@color/muted"
                android:textSize="16sp" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="14dp"
                android:orientation="horizontal">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Kandang"
                    android:textColor="@color/muted"
                    android:textSize="14sp" />

                <TextView
                    android:id="@+id/tvCage"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:gravity="end"
                    android:textColor="@color/ink"
                    android:textSize="16sp"
                    android:textStyle="bold" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="10dp"
                android:orientation="horizontal">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Status"
                    android:textColor="@color/muted"
                    android:textSize="14sp" />

                <TextView
                    android:id="@+id/tvStatus"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:gravity="end"
                    android:textColor="@color/ink"
                    android:textSize="16sp"
                    android:textStyle="bold" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="10dp"
                android:orientation="horizontal">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Berat terakhir"
                    android:textColor="@color/muted"
                    android:textSize="14sp" />

                <TextView
                    android:id="@+id/tvWeight"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1.4"
                    android:gravity="end"
                    android:textColor="@color/ink"
                    android:textSize="16sp"
                    android:textStyle="bold" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="10dp"
                android:orientation="horizontal">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Vaksin berikutnya"
                    android:textColor="@color/muted"
                    android:textSize="14sp" />

                <TextView
                    android:id="@+id/tvVaccine"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:gravity="end"
                    android:textColor="@color/ink"
                    android:textSize="16sp"
                    android:textStyle="bold" />
            </LinearLayout>

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnWeight"
                android:layout_width="match_parent"
                android:layout_height="52dp"
                android:layout_marginTop="18dp"
                android:text="Catat timbangan"
                android:textAllCaps="false"
                android:textColor="#FFFFFF"
                android:textSize="16sp"
                android:textStyle="bold"
                app:backgroundTint="@color/royal"
                app:cornerRadius="12dp" />

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnReport"
                style="@style/Widget.Material3.Button.OutlinedButton"
                android:layout_width="match_parent"
                android:layout_height="52dp"
                android:layout_marginTop="8dp"
                android:text="Lapor sakit"
                android:textAllCaps="false"
                android:textColor="@color/danger"
                android:textSize="16sp"
                android:textStyle="bold"
                app:cornerRadius="12dp"
                app:strokeColor="@color/danger" />
        </LinearLayout>

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnScanAgain"
            style="@style/Widget.Material3.Button.OutlinedButton"
            android:layout_width="match_parent"
            android:layout_height="52dp"
            android:layout_marginTop="12dp"
            android:text="Scan ulang"
            android:textAllCaps="false"
            android:textColor="@color/royal"
            android:textSize="16sp"
            android:textStyle="bold"
            android:visibility="gone"
            app:cornerRadius="12dp"
            app:strokeColor="@color/royal" />
    </LinearLayout>
</ScrollView>
```

## `app/src/main/res/layout/activity_weight_input.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/mist"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Catat timbangan"
        android:textColor="@color/navy"
        android:textSize="24sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/tvTag"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:textColor="@color/ink"
        android:textSize="20sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/tvLastWeight"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="2dp"
        android:textColor="@color/muted"
        android:textSize="14sp" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="Berat badan (kg)"
        android:textColor="@color/navy"
        android:textSize="14sp"
        android:textStyle="bold" />

    <EditText
        android:id="@+id/etWeight"
        android:layout_width="match_parent"
        android:layout_height="72dp"
        android:layout_marginTop="6dp"
        android:background="@drawable/bg_input"
        android:hint="0"
        android:importantForAutofill="no"
        android:inputType="numberDecimal"
        android:maxLength="8"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:textColor="@color/navy"
        android:textColorHint="#9AA6C4"
        android:textSize="32sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/tvDelta"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:textSize="14sp"
        android:visibility="gone" />

    <TextView
        android:id="@+id/tvError"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:background="@drawable/bg_error"
        android:padding="10dp"
        android:textColor="@color/danger"
        android:textSize="14sp"
        android:visibility="gone" />

    <View
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <ProgressBar
        android:id="@+id/progressBar"
        style="?android:attr/progressBarStyleSmall"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center_horizontal"
        android:visibility="gone" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnSave"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:layout_marginTop="8dp"
        android:text="Simpan timbangan"
        android:textAllCaps="false"
        android:textColor="#FFFFFF"
        android:textSize="16sp"
        android:textStyle="bold"
        app:backgroundTint="@color/royal"
        app:cornerRadius="12dp" />
</LinearLayout>
```
