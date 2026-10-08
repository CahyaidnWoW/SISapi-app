# Checkpoint Mobile (v4): Si-Sapi Android (Kotlin), proyek baru

**Tanggal update:** 4 Oktober 2026
**Menggantikan:** Checkpoint Mobile v3 (2 Okt 2026). Proyek lama **ditinggalkan**, dibangun ulang dari nol per batch.
**Status singkat:** Batch 1 selesai dan terbukti di HP asli (**Login berhasil ke Laravel, auto-login, logout**). Berikutnya: **Batch 2** (menu Alat, Scan QR, Profil sapi).

**Cara melanjutkan di chat baru:** tempel file ini, lalu tulis "lanjut Batch 2". Semua fakta backend di bagian 6 sudah diverifikasi dari kode Laravel, jadi tidak perlu ditebak ulang.

---

## 1. Identitas dan Lingkungan

| Hal | Nilai |
|---|---|
| Nama project | SI-SapiMobile |
| Lokasi | `D:\last_ride\SIsapiMobile` |
| **Package/namespace** | **`com.latihan.si_sapimobile`** (bukan `si_sapi`) |
| Template | Empty Views Activity, Kotlin, Min SDK 26 |
| Gradle | 9.4.1 (cache di `C:\Users\Lenovo\.gradle`) |
| compileSdk | 36.1 (lihat masalah #4 di bagian 10) |
| UI | XML + **ViewBinding** (`viewBinding = true`), Material Components |
| Arsitektur | MVVM: `Activity → ViewModel → Service → ApiService (Retrofit) → Laravel API` |
| Perangkat uji | HP asli (Samsung Galaxy A05s, Wi-Fi sama dengan laptop). Emulator bisa dengan `10.0.2.2` |
| Backend | Laravel + Sanctum (token Bearer), dijalankan `php artisan serve --host=0.0.0.0` |
| Format tag sapi | `SP-001` (contoh di mockup memakai `SPI-0042`, hanya contoh teks) |

**Kalau mengetik package di file baru:** selalu `com.latihan.si_sapimobile`. File lama di `/mnt/user-data/outputs/mobile/` (Timbangan, Pakan, Lapor sakit) masih memakai `com.latihan.si_sapi` dan harus diganti dulu (Ctrl+Shift+R, regex `com\.latihan\.si_sapi(?!mobile)` → `com.latihan.si_sapimobile`).

---

## 2. Status Pengerjaan

| Batch | Isi | Status |
|---|---|---|
| 1 | Setup project, Login, MainActivity placeholder, logout | **Selesai, terbukti di HP asli** |
| 2 | Menu Alat di MainActivity, Scan QR, Profil sapi | Belum |
| 3 | Input Timbangan | Belum (kode lama siap diadaptasi) |
| 4 | Catat Pemakaian Pakan (FIFO) | Belum (kode lama siap diadaptasi) |
| 5 | Lapor Sakit dengan foto | Belum (kode lama siap diadaptasi) |
| 6 | Sisi Dokter hewan: Pengingat vaksin, Catat tindakan, Riwayat kesehatan | Belum, opsional setelah 2 sampai 5 lolos uji |
| 7 | Poles: UI sesuai mockup, ikon, logo asli, font, bottom nav | Belum |

Aturan main: **satu batch, lalu uji di HP asli, baru batch berikutnya.** Jangan menempel banyak file sekaligus.

---

## 3. Aturan Kode (konvensi proyek)

1. **ViewBinding**, bukan `findViewById` (bug lama `btnMenuScan` tidak terjadi lagi).
2. **Satu pola** tiap fitur: `models → ApiService → Service → ViewModel → Activity → layout → Manifest → uji`.
3. **Service** mengembalikan `UiState<T>`, menangkap exception, dan tidak menyentuh UI.
4. **ViewModel** menyimpan `LiveData<UiState<...>>`, menjalankan coroutine di `viewModelScope`, dan memvalidasi input sebelum memanggil Service.
5. **Respons backend tidak dibungkus** `{success, message, data}`. POST memakai `Response<ResponseBody>`, GET memakai `Response<LivestockProfile>` atau `Response<List<...>>`. Jangan pakai `res.body()?.data`.
6. **Pesan error** selalu lewat `res.errorMessage("pesan default")` (membaca `message` dan `errors` Laravel). Jangan pakai `response.message()` (hanya teks status HTTP).
7. **Tanggal** dikirim `yyyy-MM-dd` lewat `todayString()`. `date` **wajib** di timbangan, pakan, dan laporan sakit.
8. Nama file = nama class. Semua Activity di package `.ui`.
9. Nama Intent extra tag sapi: **`"tag_number"`**.
10. `LOG_BODY = true` di `ApiClient` hanya untuk development (token dan password tercetak di Logcat). **Set `false` sebelum dikumpulkan.**

Kerangka Service:
```kotlin
class XxxService(context: Context) {
    private val api = ApiClient.getApiService(context)
    suspend fun doSomething(...): UiState<Unit> = try {
        val res = api.endpoint(...)
        if (res.isSuccessful) UiState.Success(Unit)
        else UiState.Error(res.errorMessage("Gagal ..."))
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }
}
```
Kerangka ViewModel:
```kotlin
class XxxViewModel(app: Application) : AndroidViewModel(app) {
    private val service = XxxService(app)
    private val _state = MutableLiveData<UiState<Unit>>()
    val state: LiveData<UiState<Unit>> = _state
    fun submit(...) {
        // validasi dulu -> _state.value = UiState.Error("...") lalu return
        _state.value = UiState.Loading
        viewModelScope.launch { _state.postValue(service.doSomething(...)) }
    }
}
```

---

## 4. Struktur Project (sekarang dan target)

Path dasar: `app/src/main/java/com/latihan/si_sapimobile/`

```
helpers/
├── UiState.kt            [ADA]
├── SessionManager.kt     [ADA]
├── ErrorParser.kt        [ADA]   errorMessage()
└── DateHelper.kt         [ADA]   todayString()
models/
├── User.kt               [ADA]   User, LoginRequest, LoginResponse
├── Livestock.kt          [BATCH 2]  Livestock, LivestockProfile, Cage, Breed
├── WeightLogs.kt         [BATCH 3]
└── FeedStock.kt          [BATCH 4]
network/
├── ApiClient.kt          [ADA]
└── ApiService.kt         [ADA: login, logout; endpoint lain ditambah per batch]
services/
├── AuthService.kt        [ADA]
├── LivestockService.kt   [BATCH 2]
├── WeightLogService.kt   [BATCH 3]
├── FeedConsumptionService.kt [BATCH 4]
└── HealthRecordService.kt [BATCH 5]
viewmodels/
├── AuthViewModel.kt      [ADA]
├── ScanViewModel.kt      [BATCH 2]
├── WeightLogViewModel.kt [BATCH 3]
├── FeedViewModel.kt      [BATCH 4]
└── HealthRecordViewModel.kt [BATCH 5]
ui/
├── LoginActivity.kt      [ADA]
├── MainActivity.kt       [ADA, perlu ditambah menu Alat di Batch 2]
├── ScanActivity.kt       [BATCH 2]
├── WeightInputActivity.kt [BATCH 3]
├── FeedInputActivity.kt  [BATCH 4]
└── EmergencyReportActivity.kt [BATCH 5]
res/layout/: activity_login.xml [ADA], activity_main.xml [ADA], activity_scan.xml, activity_weight_input.xml, activity_feed_input.xml, activity_emergency_report.xml
res/drawable/: bg_input.xml [ADA], bg_error.xml [ADA]; nanti bg_button*, bg_photo
res/values/: colors_sisapi.xml [ADA]
res/xml/: file_paths.xml [BATCH 5, untuk FileProvider]
```

---

## 5. Fungsi Tiap File yang Sudah Ada

| File | Fungsi |
|---|---|
| `UiState<T>` | Sealed class: `Loading`, `Success(data)`, `Error(message)`. |
| `SessionManager` | SharedPreferences `si_sapi_session`: `saveAuthToken`, `getToken`, `saveUser(name, role)`, `getUserName`, `getUserRole`, `clearSession`. |
| `ErrorParser.kt` | `Response<T>.errorMessage(default)`: ambil pesan pertama di `errors`, kalau tidak ada ambil `message`, kalau gagal parse pakai default. |
| `DateHelper.kt` | `todayString()` mengembalikan `yyyy-MM-dd`. |
| `User.kt` | `User(id, name, email, role, phone?)`, `LoginRequest(email, password)`, `LoginResponse(message?, accessToken, tokenType?, user)` dengan `@SerializedName("access_token")`. |
| `ApiClient` | Singleton Retrofit + OkHttp. `BASE_URL` (konstanta di atas file). Interceptor menambah `Accept: application/json` dan `Authorization: Bearer <token>`. Timeout 30 detik. Logging BODY lewat `LOG_BODY`. |
| `ApiService` | `login`, `logout`. |
| `AuthService` | `login()` menyimpan token + user ke SessionManager. `logout()` memanggil API, lalu **selalu** `clearSession()`. `IOException` menjadi pesan "Tidak bisa terhubung ke server...". |
| `AuthViewModel` | `loginState`, `logoutDone`. `login()` memvalidasi kosong, `logout()`. |
| `LoginActivity` | Auto-login jika token ada. Kolom email dan password, tombol Tampilkan/Sembunyikan password, error merah inline (`tvError`), tombol berubah "Memproses..." saat loading. |
| `MainActivity` | Menampilkan "Halo, {nama}" dan label role (`roleLabel()`: manager, vet, worker/pekerja). Tombol Keluar memakai dialog konfirmasi, lalu ke Login dengan `CLEAR_TASK`. Placeholder "Menu Alat ditambahkan di Batch 2". |

---

## 6. Kontrak Backend (TERVERIFIKASI dari kode Laravel)

Semua endpoint di bawah prefix `/api`. Kecuali `login`, semuanya butuh header `Authorization: Bearer <token>`. Error validasi berbentuk `{"message": "...", "errors": {"field": ["..."]}}` (HTTP 422).

| Endpoint | Method | Body / Respons | Status di app |
|---|---|---|---|
| `login` | POST | Body `{email, password}`. Respons `{message, access_token, token_type, user:{id,name,email,role,phone}}` | **Dipakai, terbukti** |
| `logout` | POST | Tanpa body | **Dipakai** |
| `me` | GET | Profil user | Belum dipakai (opsional) |
| `livestocks/{tagNumber}` | GET | Respons **objek mentah**: `{livestock:{...}, latest_weight, latest_weight_date, last_vaccine_date, next_vaccine_due}`. Tag tidak ada: **404** `{"message":"Sapi tidak ditemukan"}` | Batch 2 |
| `livestocks/{tagNumber}/weight-logs` | POST | Body `{weight, date}`. **Path `weight-logs`, bukan `weight`** | Batch 3 |
| `cages` | GET | List kandang (`id`, `name` diasumsikan) | Batch 4 |
| `feeds` | GET | List `{id, feed_name, unit, total_stock}` | Batch 4 |
| `cages/{id}/feed` | POST | Body **`{cage_id, feed_id, amount_used, date}`**. `cage_id` divalidasi dari **body**, bukan dari URL. `amount_used` min 0,01. Stok kurang: **422** `{"message":"..."}` (FIFO otomatis, app tidak memilih batch) | Batch 4 |
| `livestocks/{tagNumber}/health-report` | POST multipart | Field: `type` (`vaccination` / `treatment` / `emergency_report`), `diagnosis`, `treatment` (nullable), `date` (wajib), `photo` (nullable, image, **maks 5 MB**). Respons 201 berupa model mentah | Batch 5 |
| `health-records/due` | GET | Daftar vaksin jatuh tempo | Tercatat di routes, **belum diuji dari app** (Batch 6) |
| `livestocks/{tagNumber}/health-records` | GET | Riwayat kesehatan satu sapi | Tercatat di routes, **belum diuji dari app** (Batch 6) |

### Temuan penting yang pernah menjebak
1. Respons sukses **tidak** dibungkus `{success, message, data}` (hanya `FcrController` memakai `{status, data}`). `res.body()?.data` akan selalu `null`.
2. `LivestockProfile` **tidak** punya `weight_logs` maupun `health_records`.
3. `CreateFeedConsumptionRequest` tanpa `cage_id` ditolak 422.
4. `date` wajib di tiga endpoint POST. Selalu kirim.
5. Role dari backend berhuruf kecil (`manager`). Nilai persis untuk pekerja (`worker` atau `pekerja`) dan dokter hewan (`vet`) **belum dikonfirmasi**; cek di Logcat saat login atau di tabel `users`.
6. Foto di atas 5 MB ditolak, jadi kompres sebelum upload (sisi terpanjang 1280 px, JPEG 80).

### Target `ApiService.kt` (versi akhir setelah Batch 2 sampai 5)
```kotlin
interface ApiService {
    @POST("login")  suspend fun login(@Body body: LoginRequest): Response<LoginResponse>
    @POST("logout") suspend fun logout(): Response<ResponseBody>

    @GET("livestocks/{tagNumber}")
    suspend fun getLivestockByTag(@Path("tagNumber") tagNumber: String): Response<LivestockProfile>

    @POST("livestocks/{tagNumber}/weight-logs")
    suspend fun recordWeight(@Path("tagNumber") tagNumber: String,
                             @Body body: CreateWeightLogRequest): Response<ResponseBody>

    @GET("cages") suspend fun getCages(): Response<List<Cage>>
    @GET("feeds") suspend fun getFeeds(): Response<List<FeedStock>>

    @POST("cages/{id}/feed")
    suspend fun recordFeedConsumption(@Path("id") cageId: Int,
                                      @Body body: CreateFeedConsumptionRequest): Response<ResponseBody>

    @Multipart
    @POST("livestocks/{tagNumber}/health-report")
    suspend fun sendHealthReport(@Path("tagNumber") tagNumber: String,
                                 @Part("type") type: RequestBody,
                                 @Part("diagnosis") diagnosis: RequestBody,
                                 @Part("treatment") treatment: RequestBody?,
                                 @Part("date") date: RequestBody,
                                 @Part photo: MultipartBody.Part?): Response<ResponseBody>
}
```
Impor yang dibutuhkan: `okhttp3.MultipartBody`, `okhttp3.RequestBody`, `okhttp3.ResponseBody`, `retrofit2.Response`, `retrofit2.http.*`.

### Model target `Livestock.kt` (Batch 2)
```kotlin
data class LivestockProfile(
    val livestock: Livestock,
    @SerializedName("latest_weight") val latestWeight: Double?,
    @SerializedName("latest_weight_date") val latestWeightDate: String?,
    @SerializedName("last_vaccine_date") val lastVaccineDate: String?,
    @SerializedName("next_vaccine_due") val nextVaccineDue: String?
)
data class Livestock(
    val id: Int,
    @SerializedName("tag_number") val tagNumber: String,
    @SerializedName("cage_id") val cageId: Int?,
    @SerializedName("breed_id") val breedId: Int?,
    val gender: String?,
    @SerializedName("initial_weight") val initialWeight: Double?,
    val status: String?,
    val cage: Cage? = null,
    val breed: Breed? = null
)
data class Cage(val id: Int, val name: String)           // nama field persis perlu dicek di JSON
data class Breed(val id: Int, @SerializedName("breed_name") val name: String? = null) // idem
```
Nama field `Cage` dan `Breed` adalah **asumsi**; kalau scan menghasilkan nilai kosong, lihat JSON asli di Logcat (tag OkHttp) dan sesuaikan.

---

## 7. Desain UI/UX (sudah dirancang)

**Rujukan visual:** halaman rancangan 19 mockup di https://claude.ai/artifact/W8b9CUdr8GAUhTd8VmhB6o (file lokal `sisapi-mobile-ui.html`). Isinya Login, Beranda Pekerja/Vet, Scan, Profil sapi, Timbangan, Pakan, Lapor sakit, Pengingat, Catat tindakan, Riwayat, Profil + konfirmasi keluar, dan state (memuat, offline, tag tidak ditemukan, kosong).

### Peta layar → kode
| Layar | Class | ViewModel | Endpoint | Role |
|---|---|---|---|---|
| Login | `LoginActivity` | `AuthViewModel` | `POST login` | Semua |
| Beranda (menu Alat) | `MainActivity` | `AuthViewModel` | (Batch 6: `health-records/due`) | Semua |
| Scan QR | `ScanActivity` | `ScanViewModel` | `GET livestocks/{tag}` | Semua |
| Profil sapi | di ScanActivity (Batch 2), nanti dipisah | `ScanViewModel` | `GET livestocks/{tag}` | Semua |
| Catat timbangan | `WeightInputActivity` | `WeightLogViewModel` | `POST .../weight-logs` | Pekerja |
| Catat pakan | `FeedInputActivity` | `FeedViewModel` | `GET cages`, `GET feeds`, `POST cages/{id}/feed` | Pekerja |
| Lapor sakit | `EmergencyReportActivity` | `HealthRecordViewModel` | `POST .../health-report` | Pekerja (keputusan terbuka) |
| Pengingat vaksin, Catat tindakan, Riwayat | Batch 6 | `HealthRecordViewModel` | `health-records/due`, `health-report`, `health-records` | Vet |

**Satu `ScanActivity`, tiga mode** lewat Intent extra: scan biasa (buka profil), timbang (lanjut ke form timbangan), sakit (lanjut ke form laporan). Ada fallback **ketik nomor tag manual** untuk ear tag yang kotor atau rusak.

### Token desain
| Nama | Hex | Pemakaian |
|---|---|---|
| navy | `#1B1464` | judul |
| royal | `#2D5FAD` | tombol utama |
| sky | `#1890DB` | header, tombol Beranda |
| mist | `#EEF4FD` | latar layar |
| ink | `#10183A` | teks isi |
| muted | `#566384` | teks pendukung |
| line | `#8FA3CC` | border kolom |
| danger | `#C62828` | sakit, terlambat, keluar |
| ok | `#17784F` | berhasil |

Aturan ukuran: teks isi dan tombol **16sp**, teks pendukung minimal **14sp**, judul layar 24sp, angka berat dan pakan 32 sampai 40sp, area ketuk minimal **48dp**, sudut tombol dan kolom 12dp, margin layar 16dp. Font: Lexend (taruh di `res/font`).

### Perubahan dari desain awalmu (alasan)
1. Teks diperbesar (dipakai di kandang, sering di luar ruangan).
2. Dua panel Notifikasi/Alat yang saling membuka → **satu layar scroll**, Alat di atas, Pengingat di bawah (tanpa BottomSheet/MotionLayout).
3. **Notifikasi dari manager diganti Pengingat vaksin** (backend tidak punya endpoint notifikasi manager, tapi punya `health-records/due`). Pengumuman manager = pengembangan lanjutan, tulis di laporan.
4. Logout dipindah ke Profil/layar utama dengan **dialog konfirmasi** (sudah dibuat di MainActivity).
5. Bottom nav diberi label teks. Tombol Beranda di tengah tetap menonjol.
6. Kolom login: border lebih tegas, tombol lihat password, error inline (sudah dibuat).
7. "Worker" ditulis "Pekerja".
8. Tab Riwayat berarti **riwayat kesehatan per sapi** (tidak ada endpoint riwayat input milik pekerja, dan tidak ada endpoint riwayat berat).

### Keputusan terbuka (perlu kamu putuskan)
1. **Lapor sakit:** Pekerja saja (checkpoint lama, Tabel 3.1) atau Pekerja dan Vet (mockup)?
2. **Manager di mobile:** menu apa yang tampil? Pilihan: semua Alat, atau pesan "gunakan aplikasi desktop".
3. Ketersediaan **logo asli** dan font Lexend untuk Batch 7.
4. Apakah Batch 6 (sisi Vet) dikerjakan atau dicatat sebagai pengembangan lanjutan.

---

## 8. Rencana Batch Berikutnya (detail)

### Batch 2: Menu Alat + Scan QR + Profil sapi
**File:** `models/Livestock.kt`, tambah `getLivestockByTag` ke `ApiService`, `services/LivestockService.kt`, `viewmodels/ScanViewModel.kt`, `ui/ScanActivity.kt`, `res/layout/activity_scan.xml`, ubah `MainActivity` + `activity_main.xml` (4 tile Alat), daftarkan `ScanActivity` di manifest.
**Perilaku:**
- Tile Pekerja: Scan QR, Timbang, Pakan, Lapor sakit. Tile Vet: Scan QR, Vaksin, Tindakan, Rekam medis (yang belum jadi boleh dinonaktifkan).
- `ScanActivity` memakai `DecoratedBarcodeView` (ZXing, `zxing-android-embedded`), scan terus-menerus, scanner **di-pause saat request berjalan**, tombol **Scan Ulang**, input tag manual.
- **Izin kamera runtime** (`ActivityResultContracts.RequestPermission`), `barcodeScanner.resume()` di `onResume` hanya jika izin ada.
- Tag tidak ada (404) → pesan "Sapi tidak ditemukan", bukan layar kosong.
- Profil menampilkan: tag, ras, jenis kelamin, kandang, berat terakhir + tanggal, vaksin berikutnya. Tombol Timbang dan Lapor sakit membuka Activity tujuan dengan `putExtra("tag_number", ...)`.
**Uji penerimaan:** scan QR ear tag asli (dari aplikasi desktop) → profil terisi; ketik `SP-001` manual → sama; ketik `SP-9999` → pesan tidak ditemukan; tolak izin kamera → pesan jelas, app tidak crash.
**Yang perlu diverifikasi:** isi QR yang dibuat desktop (QRCoder) apakah persis nomor tag (misal `SP-001`) atau format lain.

### Batch 3: Timbangan
File lama: `WeightInputActivity`, `WeightLogViewModel`, `WeightLogService`, `WeightLogs.kt`, layout (di `/mnt/user-data/outputs/mobile/`, ganti package). Kirim `{weight, date}` (koma desimal diganti titik). **Uji:** simpan 358,5 kg → baris baru di tabel `weight_logs` (cek `worker_id` terisi) → scan ulang, berat terakhir berubah. Berat 0 atau huruf → pesan validasi di app, tidak menembak API.

### Batch 4: Pakan
File lama: `FeedInputActivity`, `FeedViewModel`, `FeedConsumptionService`, `FeedStock.kt`, layout. Dua Spinner (kandang, pakan), `etAmount`, petunjuk stok. **Uji:** pemakaian kecil → `total_stock` di `GET feeds` berkurang; jumlah melebihi stok → pesan 422 dari Laravel tampil di app dan stok **tidak** berubah. Daftar kosong → pesan arahan ke desktop.
**Cek:** nama field `Cage` (`name`?).

### Batch 5: Lapor sakit
File lama: `EmergencyReportActivity`, `HealthRecordViewModel`, `HealthRecordService`, layout, `res/xml/file_paths.xml`, `<provider>` FileProvider di manifest (authorities `${applicationId}.fileprovider`). Foto: izin kamera runtime → `TakePicture` → kompres (maks 1280 px, JPEG 80, putar sesuai EXIF). **Uji:** kirim dengan foto dan tanpa foto → baris di `health_records` (`type = emergency_report`, `photo_path` terisi hanya yang pakai foto). Kerjakan paling akhir karena multipart adalah bagian paling baru.

### Batch 6 (opsional): Sisi Vet
`ReminderListActivity` (`GET health-records/due`), `TreatmentActivity` (`type = vaccination/treatment`, `next_due_date` untuk vaksinasi), Riwayat per sapi. Verifikasi dulu nama field respons `health-records/due`.

### Batch 7: Poles
Terapkan mockup (header melengkung, kartu, bottom nav), logo asli, font Lexend, state memuat/offline/kosong sebagai layout `include` yang dipakai ulang, matikan `LOG_BODY`.

---

## 9. Setup dan Koneksi

**build.gradle.kts (:app):**
```kotlin
android { buildFeatures { viewBinding = true } }
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}
```
**AndroidManifest.xml:**
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
<application android:usesCleartextTraffic="true" ...>  <!-- wajib, server http -->
    <activity android:name=".ui.LoginActivity" android:exported="true"> ...LAUNCHER... </activity>
    <activity android:name=".ui.MainActivity" android:exported="false" />
    <!-- tambah per batch: ScanActivity, WeightInputActivity, FeedInputActivity, EmergencyReportActivity -->
</application>
```
**Koneksi ke Laravel dari HP asli (sudah terbukti jalan):**
1. Laptop: `php artisan serve --host=0.0.0.0` (tanpa `--host`, HP tidak bisa masuk).
2. `BASE_URL = "http://<IP-LAN-laptop>:8000/api/"` di `ApiClient.kt`. IP dari `ipconfig`, bagian **Wireless LAN adapter Wi-Fi** (hindari adapter VirtualBox/WSL/VPN).
3. HP dan laptop di Wi-Fi yang sama. Tes dulu lewat Chrome HP: `http://<IP>:8000` harus tampil.
4. Firewall (CMD Administrator): `netsh advfirewall firewall add rule name="Laravel 8000" dir=in action=allow protocol=TCP localport=8000`. Jaringan Wi-Fi diset **Private**.
5. **IP laptop berubah** kalau pindah jaringan atau hotspot. Setiap kali pindah, cek `ipconfig` dan ubah `BASE_URL`, lalu Run ulang.
6. Wi-Fi sekolah/kampus sering mengisolasi perangkat. Solusinya hotspot dari HP lain.
7. Emulator: pakai `http://10.0.2.2:8000/api/`.

---

## 10. Log Masalah yang Sudah Terjadi (dan solusinya)

| # | Masalah | Penyebab | Solusi |
|---|---|---|---|
| 1 | Zip tidak bisa diunduh | Pembatasan di perangkat | Pakai file `.md` satu berkas atau unduh ulang |
| 2 | Package tidak cocok (`si_sapi` vs `si_sapimobile`) | Nama project berbeda | Ctrl+Shift+R, regex `com\.latihan\.si_sapi(?!mobile)` → `com.latihan.si_sapimobile` |
| 3 | `Unresolved reference ActivityLoginBinding` | Folder `layout` masih kosong / belum Sync | Salin `activity_login.xml` dan `activity_main.xml`, Sync, lalu Clean + Rebuild. Pastikan `viewBinding = true` di dalam blok `android { }` |
| 4 | `androidx.core:core(-ktx):1.19.0` butuh compileSdk 37, project 36.1 | Library terlalu baru | Opsi A: turunkan `coreKtx` ke `1.16.0` di `gradle/libs.versions.toml`. Opsi B: install Android API 37 lalu naikkan compileSdk. (Catat opsi yang kamu pakai: ______) |
| 5 | `last-build.bin (Access is denied)` | Cache Gradle terkunci proses lama/antivirus | Tutup Android Studio, akhiri proses Java/OpenJDK, hapus `C:\Users\Lenovo\.gradle\caches\9.4.1\file-changes`, buka lagi. Exclusion Defender bila berulang |
| 6 | `ERR_CONNECTION_TIMED_OUT` dari HP | Firewall/profil jaringan/isolasi Wi-Fi/IP salah | Langkah koneksi bagian 9 |
| 7 | Terminal: `Invalid request (Unsupported SSL request)` | HP mengirim **https** ke server http | Pakai `http://` di Chrome dan di `BASE_URL` |
| 8 | Chrome HP bisa, login dari app gagal | `usesCleartextTraffic`/`BASE_URL`/app belum dipasang ulang | Cek manifest + `BASE_URL`, Run ulang. **Teratasi**, login berhasil |

---

## 11. Asumsi dan Hal Belum Terverifikasi

1. Nilai role pekerja dan vet dari backend (`worker`/`pekerja`/`vet`).
2. Nama field persis `Cage` (`name`) dan `Breed` (`breed_name`).
3. Isi QR dari aplikasi desktop (apakah sama dengan `tag_number`).
4. Respons dan field `health-records/due` dan `livestocks/{tag}/health-records`.
5. Apakah `GET cages` dan `GET feeds` mengembalikan list mentah (diasumsikan ya).
6. Kode Batch 3 sampai 5 yang lama **belum pernah dikompilasi**.
7. Jawaban `GET me` (belum dipakai).

---

## 12. Utang Dokumentasi Laporan

1. Narasi per sequence diagram (3.2.5, Gambar 3.18 sampai 3.30) dan narasi Use Case Mobile.
2. Tabel 3.1: baris 12 (Vet tidak lapor darurat atau sesuai keputusan terbuka no. 1), baris 2 dan 13 (Worker tidak punya Dashboard/reminder di mobile, atau diganti Pengingat), baris 14 (cek FCR untuk Vet).
3. Tabel 2.1: tambah Retrofit, Gson, ZXing, **ViewBinding**, LiveData + Coroutines, Material Components. Baris QRCoder = "pada aplikasi Desktop (C#)".
4. Tabel 3.2 Maintainability: tambah "ViewModel" untuk Mobile.
5. ~~Path timbangan di Gambar 3.22~~ **Tidak perlu diubah**: `weight-logs` memang benar.
6. Bab III 3.3 (wireframe/mockup): pakai 19 mockup dari halaman rancangan. Jelaskan perubahan dari desain awal (bagian 7).
7. Bab IV dan V belum dimulai. Screenshot aplikasi Mobile diambil dari HP asli setelah Batch 7.
8. Catat di "Pengembangan lanjutan": notifikasi/pengumuman dari manager, riwayat input per pekerja, riwayat berat di mobile, mode offline.

---

## 13. Checklist Sebelum Dikumpulkan

- [ ] `LOG_BODY = false` di `ApiClient.kt`
- [ ] `BASE_URL` sesuai lingkungan demo (IP laptop yang dipakai saat sidang)
- [ ] Semua Activity terdaftar di manifest
- [ ] Alur lengkap diuji di HP asli: Login → Scan → Timbang → Pakan → Lapor sakit → Logout
- [ ] Skenario error diuji: password salah, server mati, tag tidak ada, stok kurang, izin kamera ditolak
- [ ] Data demo: minimal satu sapi dengan QR yang bisa dipindai, stok pakan cukup
- [ ] APK debug terpasang di HP cadangan (opsional)
- [ ] Jadwal sidang dikonfirmasi (rencana awal akhir September sudah lewat; pastikan tanggal terbaru)

---

## 14. Langkah Berikutnya

1. Putuskan 4 keputusan terbuka di bagian 7 (terutama no. 1 dan 2).
2. Cek nilai role dari Logcat/tabel `users`, dan isi QR dari desktop.
3. Kerjakan **Batch 2**, uji di HP asli sesuai uji penerimaannya.
4. Lanjut Batch 3 → 4 → 5, satu per satu.
5. Catat setiap error baru di tabel bagian 10 supaya checkpoint berikutnya (v5) lengkap.
