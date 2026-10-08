# Si-Sapi Mobile, Batch 2: Menu Alat + Scan QR + Profil sapi

Package: `com.latihan.si_sapimobile`. Syarat: Batch 1 sudah jalan (login berhasil).

## 1. Salin file
Isi `app/src/main/` disalin ke project. **Timpa** 3 file lama (pilih Replace):
`network/ApiService.kt`, `ui/MainActivity.kt`, `res/layout/activity_main.xml`.
File baru: `models/Livestock.kt`, `services/LivestockService.kt`, `viewmodels/ScanViewModel.kt`, `ui/ScanActivity.kt`, `res/layout/activity_scan.xml`, `res/layout/item_tool.xml`, `res/drawable/bg_card.xml`.

## 2. Daftarkan ScanActivity di AndroidManifest.xml
Di dalam `<application>`, di bawah MainActivity:
```xml
<activity android:name=".ui.ScanActivity" android:exported="false" />
```
Izin `INTERNET` dan `CAMERA` sudah ada dari Batch 1. Kalau belum, tambahkan:
```xml
<uses-permission android:name="android.permission.CAMERA" />
```
Pastikan dependency `com.journeyapps:zxing-android-embedded:4.3.0` ada di `build.gradle.kts` (sudah di Batch 1). Sync, lalu Run.

## 3. Yang berubah
- **Layar utama** menampilkan tile Alat sesuai role: Pekerja (Scan QR, Timbang, Pakan, Lapor sakit), Dokter hewan (Scan QR, Vaksin, Tindakan, Rekam medis), role lain (hanya Scan QR + catatan).
- Tile yang fiturnya belum dikerjakan memunculkan Toast "dikerjakan di Batch N". Ini disengaja.
- **ScanActivity** memindai QR terus-menerus, ada input ketik nomor tag, tombol Scan ulang, dan kartu profil sapi.
- Satu baris log sementara di MainActivity: `Log.d("ROLE", ...)`. Dipakai untuk memastikan nilai role asli dari backend (hapus setelah dicek).

## 4. Uji (di HP asli, urut)
1. **Role:** login, lalu buka Logcat, filter `ROLE`. Catat isinya (`worker`? `pekerja`? `vet`?). Kalau layar utama hanya menampilkan "Scan QR" padahal akunmu pekerja, berarti nilai role-nya belum dikenali. Kirim nilainya, nanti disesuaikan.
2. **Tile:** akun pekerja → 4 tile. Tile Pakan menampilkan Toast Batch 4.
3. **Scan QR:** tekan Scan QR → izin kamera muncul → izinkan → arahkan ke QR ear tag dari aplikasi desktop. Profil sapi muncul (tag, ras, kandang, status, berat terakhir, vaksin berikutnya).
4. **Ketik manual:** ketik `SP-001` (atau tag yang ada di databasemu) → Cari → profil sama.
5. **Tag tidak ada:** ketik `SP-9999` → kotak merah "Sapi dengan tag SP-9999 tidak ditemukan." + tombol Scan ulang.
6. **Izin ditolak:** cabut izin kamera di Pengaturan app, buka Scan → muncul Toast, ketik manual masih bisa.
7. **Scan ulang:** setelah profil muncul, tekan Scan ulang → kamera aktif lagi, kartu hilang.
8. **Putar layar** saat profil tampil: kartu tetap ada, scanner tidak jalan sendiri.

## 5. Kalau ada yang tidak sesuai
| Gejala | Penyebab / tindakan |
|---|---|
| Kamera hitam | Izin kamera belum diberikan. Buka Pengaturan app → Izin → Kamera |
| QR terbaca tapi "tidak ditemukan" | Isi QR bukan persis `tag_number`. Lihat isi QR: ketik hasil scan di kolom tag (atau lihat di Logcat `OkHttp` path `livestocks/...`) |
| Kandang atau ras tampil `-` | Nama field `Cage.name` / `Breed.breed_name` beda. Lihat JSON di Logcat (filter `OkHttp`, cari `"cage"` dan `"breed"`), sesuaikan `@SerializedName` di `Livestock.kt` |
| Berat terakhir "Belum ada" padahal ada | Field `latest_weight` null di respons. Cek JSON di Logcat |
| `Unresolved reference ItemToolBinding` / `ActivityScanBinding` | Belum Sync / file layout belum disalin. Sync lalu Rebuild |
| Crash saat buka Scan | Kirim baris merah pertama di Logcat (filter `AndroidRuntime`) |

## 6. Setelah lolos uji
Kirim: nilai `ROLE`, hasil uji 3 sampai 5, dan isi QR yang terbaca (cukup teksnya). Setelah itu Batch 3 (Timbangan).


---

# Kode lengkap (salin per file)

Setiap blok diawali path file di dalam project. Buat file dengan nama dan folder itu (atau timpa yang lama), lalu tempel isinya.

## `app/src/main/java/com/latihan/si_sapimobile/models/Livestock.kt`

```kotlin
package com.latihan.si_sapimobile.models

import com.google.gson.annotations.SerializedName

// Respons asli GET /api/livestocks/{tagNumber} (objek mentah, TIDAK dibungkus success/data):
// { "livestock": {...}, "latest_weight": 342, "latest_weight_date": "2026-09-28",
//   "last_vaccine_date": "...", "next_vaccine_due": "..." }
data class LivestockProfile(
    val livestock: Livestock,
    @SerializedName("latest_weight") val latestWeight: Double? = null,
    @SerializedName("latest_weight_date") val latestWeightDate: String? = null,
    @SerializedName("last_vaccine_date") val lastVaccineDate: String? = null,
    @SerializedName("next_vaccine_due") val nextVaccineDue: String? = null
)

data class Livestock(
    val id: Int = 0,
    @SerializedName("tag_number") val tagNumber: String = "",
    @SerializedName("cage_id") val cageId: Int? = null,
    @SerializedName("breed_id") val breedId: Int? = null,
    val gender: String? = null,
    @SerializedName("initial_weight") val initialWeight: Double? = null,
    val status: String? = null,
    val cage: Cage? = null,
    val breed: Breed? = null
)

// Nama field Cage dan Breed adalah ASUMSI. Kalau di profil tampil "-", lihat JSON asli
// di Logcat (filter: OkHttp) lalu sesuaikan @SerializedName di bawah.
data class Cage(
    val id: Int = 0,
    val name: String? = null
)

data class Breed(
    val id: Int = 0,
    @SerializedName("breed_name") val name: String? = null
)
```

## `app/src/main/java/com/latihan/si_sapimobile/network/ApiService.kt`

```kotlin
package com.latihan.si_sapimobile.network

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
}
```

## `app/src/main/java/com/latihan/si_sapimobile/services/LivestockService.kt`

```kotlin
package com.latihan.si_sapimobile.services

import android.content.Context
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.helpers.errorMessage
import com.latihan.si_sapimobile.models.LivestockProfile
import com.latihan.si_sapimobile.network.ApiClient
import java.io.IOException

class LivestockService(context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun getLivestockByTag(tag: String): UiState<LivestockProfile> = try {
        val res = api.getLivestockByTag(tag)
        val body = res.body()
        when {
            res.isSuccessful && body != null -> UiState.Success(body)
            res.code() == 404 -> UiState.Error("Sapi dengan tag $tag tidak ditemukan.")
            res.code() == 401 -> UiState.Error("Sesi berakhir. Keluar lalu masuk lagi.")
            else -> UiState.Error(res.errorMessage("Gagal memuat data sapi."))
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }
}
```

## `app/src/main/java/com/latihan/si_sapimobile/ui/MainActivity.kt`

```kotlin
package com.latihan.si_sapimobile.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.databinding.ActivityMainBinding
import com.latihan.si_sapimobile.databinding.ItemToolBinding
import com.latihan.si_sapimobile.helpers.SessionManager
import com.latihan.si_sapimobile.viewmodels.AuthViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        val role = session.getUserRole().lowercase()

        // Untuk memastikan nilai role asli dari backend. Hapus setelah dicek.
        Log.d("ROLE", "role dari server = '$role'")

        binding.tvGreeting.text = "Halo, ${session.getUserName()}"
        binding.tvRole.text = roleLabel(role)

        buildTools(role)

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Keluar dari Si-Sapi?")
                .setMessage("Kamu perlu masuk lagi untuk memakai aplikasi.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Keluar") { _, _ -> viewModel.logout() }
                .show()
        }

        viewModel.logoutDone.observe(this) { done ->
            if (done) {
                startActivity(
                    Intent(this, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
        }
    }

    // Tile Alat ditentukan oleh role.
    private fun buildTools(role: String) {
        binding.gridTools.removeAllViews()
        when (role) {
            "worker", "pekerja" -> {
                addTool("Scan QR", "Lihat profil sapi") { openScan(ScanActivity.MODE_SCAN) }
                addTool("Timbang", "Catat berat badan") { openScan(ScanActivity.MODE_WEIGHT) }
                addTool("Pakan", "Catat pakan harian") { soon("Catat pakan", "Batch 4") }
                addTool("Lapor sakit", "Laporan darurat", danger = true) { openScan(ScanActivity.MODE_REPORT) }
            }
            "vet" -> {
                addTool("Scan QR", "Lihat profil sapi") { openScan(ScanActivity.MODE_SCAN) }
                addTool("Vaksin", "Jatuh tempo") { soon("Pengingat vaksin", "Batch 6") }
                addTool("Tindakan", "Catat vaksin / obat") { soon("Catat tindakan", "Batch 6") }
                addTool("Rekam medis", "Riwayat sapi") { soon("Rekam medis", "Batch 6") }
            }
            else -> {
                // Manager atau role lain: keputusan terbuka. Sementara hanya Scan QR.
                addTool("Scan QR", "Lihat profil sapi") { openScan(ScanActivity.MODE_SCAN) }
                binding.tvNote.text = "Fitur lengkap untuk Manager tersedia di aplikasi desktop."
                binding.tvNote.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun addTool(title: String, desc: String, danger: Boolean = false, onClick: () -> Unit) {
        val item = ItemToolBinding.inflate(layoutInflater, binding.gridTools, false)
        item.tvTitle.text = title
        item.tvDesc.text = desc
        if (danger) item.tvTitle.setTextColor(getColor(R.color.danger))
        item.root.setOnClickListener { onClick() }

        val margin = (6 * resources.displayMetrics.density).toInt()
        val lp = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
        ).apply {
            width = 0
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setMargins(margin, margin, margin, margin)
        }
        binding.gridTools.addView(item.root, lp)
    }

    private fun openScan(mode: String) {
        startActivity(Intent(this, ScanActivity::class.java).putExtra(ScanActivity.EXTRA_MODE, mode))
    }

    private fun soon(feature: String, batch: String) {
        Toast.makeText(this, "$feature dikerjakan di $batch.", Toast.LENGTH_SHORT).show()
    }

    private fun roleLabel(role: String) = when (role) {
        "manager" -> "Manager"
        "vet" -> "Dokter hewan"
        "worker", "pekerja" -> "Pekerja"
        else -> role
    }
}
```

## `app/src/main/java/com/latihan/si_sapimobile/ui/ScanActivity.kt`

```kotlin
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
```

## `app/src/main/java/com/latihan/si_sapimobile/viewmodels/ScanViewModel.kt`

```kotlin
package com.latihan.si_sapimobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapimobile.helpers.UiState
import com.latihan.si_sapimobile.models.LivestockProfile
import com.latihan.si_sapimobile.services.LivestockService
import kotlinx.coroutines.launch

class ScanViewModel(app: Application) : AndroidViewModel(app) {
    private val service = LivestockService(app)

    // null = belum ada pencarian (layar siap memindai)
    private val _scanState = MutableLiveData<UiState<LivestockProfile>?>()
    val scanState: LiveData<UiState<LivestockProfile>?> = _scanState

    fun fetchLivestockByTag(tag: String) {
        val t = tag.trim()
        if (t.isEmpty()) {
            _scanState.value = UiState.Error("Nomor tag wajib diisi.")
            return
        }
        if (_scanState.value is UiState.Loading) return // abaikan scan ganda saat request berjalan
        _scanState.value = UiState.Loading
        viewModelScope.launch { _scanState.postValue(service.getLivestockByTag(t)) }
    }

    fun reset() {
        _scanState.value = null
    }
}
```

## `app/src/main/res/drawable/bg_card.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#FFFFFF" />
    <stroke android:width="1dp" android:color="#CBD6EA" />
    <corners android:radius="16dp" />
</shape>
```

## `app/src/main/res/layout/activity_main.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/mist"
    android:orientation="vertical">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/sky"
        android:orientation="vertical"
        android:paddingStart="20dp"
        android:paddingTop="40dp"
        android:paddingEnd="20dp"
        android:paddingBottom="24dp">

        <TextView
            android:id="@+id/tvGreeting"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="#FFFFFF"
            android:textSize="24sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvRole"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:textColor="#FFFFFF"
            android:textSize="14sp" />
    </LinearLayout>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="10dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_margin="6dp"
                android:text="Alat"
                android:textColor="@color/navy"
                android:textSize="18sp"
                android:textStyle="bold" />

            <GridLayout
                android:id="@+id/gridTools"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:columnCount="2" />

            <TextView
                android:id="@+id/tvNote"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_margin="6dp"
                android:textColor="@color/muted"
                android:textSize="14sp"
                android:visibility="gone" />
        </LinearLayout>
    </ScrollView>

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnLogout"
        style="@style/Widget.Material3.Button.OutlinedButton"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:layout_margin="16dp"
        android:text="Keluar"
        android:textAllCaps="false"
        android:textColor="@color/danger"
        android:textSize="16sp"
        android:textStyle="bold"
        app:cornerRadius="12dp"
        app:strokeColor="@color/danger" />
</LinearLayout>
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

## `app/src/main/res/layout/item_tool.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@drawable/bg_card"
    android:clickable="true"
    android:focusable="true"
    android:foreground="?attr/selectableItemBackground"
    android:minHeight="104dp"
    android:orientation="vertical"
    android:padding="14dp">

    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@color/navy"
        android:textSize="18sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/tvDesc"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:textColor="@color/muted"
        android:textSize="14sp" />
</LinearLayout>
```
