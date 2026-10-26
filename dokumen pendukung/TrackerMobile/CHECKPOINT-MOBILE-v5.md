# Checkpoint Mobile (v5): Si-Sapi Android (Kotlin)

**Tanggal update:** 26 Oktober 2026  
**Menggantikan:** Checkpoint Mobile v4 (4 Okt 2026).  
**Status Singkat:** Seluruh fitur fungsional (Batch 1 s.d. Batch 6) + **Shared Daily Tasks Worker** & **Background Notification Vet** sudah **100% tuntas dan teruji di HP asli**.  
**Fokus Selanjutnya:** Pemolesan UI/UX dan estetika visual (diserahkan ke Argon).

---

## 1. Identitas & Lingkungan Pengembangan

| Komponen | Spesifikasi / Nilai |
|---|---|
| Nama Project | `SISapiMobile` |
| Lokasi Folder | `D:\last_ride\SISapiMobile` |
| Package / Namespace | `com.latihan.si_sapimobile` |
| SDK Target | Min SDK 26, Compile SDK 36.1 |
| Arsitektur | MVVM (`Activity` $\rightarrow$ `ViewModel` $\rightarrow$ `Service` $\rightarrow$ `ApiService` $\rightarrow$ Laravel Sanctum) |
| UI Framework | XML + ViewBinding (`viewBinding = true`), Material Components |
| Background Service | Android WorkManager (`androidx.work:work-runtime-ktx:2.9.0`) |
| Perangkat Uji | HP Fisik via USB Debugging (`adb reverse tcp:8000 tcp:8000`) |
| URL Endpoint Lokal | `http://localhost:8000/api/` (atau IP LAN saat menggunakan Wi-Fi) |
| URL Server Online | `http://sisapi.smkn1sumedang.sch.id/api/` |

---

## 2. Status Pengerjaan Fitur (Status Lengkap)

| Fitur / Batch | Deskripsi | Status |
|---|---|---|
| **Batch 1: Auth & Sesi** | Login Sanctum, auto-login SharedPreferences, logout | **Selesai & Teruji** |
| **Batch 2: QR & Profil** | Scan QR Ear Tag (ZXing), tampil profil sapi instan, fallback input manual | **Selesai & Teruji** |
| **Batch 3: Timbangan** | Input berat badan berkala, konfirmasi lonjakan bobot > 25% | **Selesai & Teruji** |
| **Batch 4: Pakan (FIFO)** | Input pemakaian pakan per kandang, validasi sisa stok gudang | **Selesai & Teruji** |
| **Batch 5: Lapor Sakit** | Kamera fisik (FileProvider), upload foto multipart (maks 5MB), gejala klinis | **Selesai & Teruji** |
| **Batch 6: Dokter Hewan** | Reminder vaksin (`due`), catat tindakan medis (`treatment`), riwayat kesehatan | **Selesai & Teruji** |
| **Fitur Baru: Shared Tasks** | To-Do List Harian pekerja, progress sinkron real-time antar-pekerja (`daily-tasks`) | **Selesai & Teruji** |
| **Fitur Baru: Background Notif** | WorkManager 15 menit, dual channel notif lokal (Darurat & Vaksin) tanpa Firebase | **Selesai & Teruji** |
| **Batch 7: Pemolesan UI** | Estetika visual, tipografi, konsistensi warna, transisi halus | *Tahap Pengerjaan (Argon)* |

---

## 3. Detail Dua Fitur Baru yang Terintegrasi Hari Ini

### A. Shared Daily Tasks (Pekerja / Worker)
* **Endpoint Backend:** `GET /api/worker/daily-tasks` (`WorkerTaskController.php`).
* **Logika Kerja:**
  * Backend menghitung konsumsi pakan hari ini (`feed_consumptions` where `date = today`).
  * Jika kandang **sudah** diberi pakan: Status otomatis `is_completed = true` (nama pekerja, jam, dan bobot tercatat).
  * Jika kandang **belum** diberi pakan: Status `is_completed = false` dengan jumlah populasi sapi.
  * Kartu belum selesai diklik $\rightarrow$ **Otomatis membuka form input pakan**.
  * Sapi sakit yang belum ditangani $\rightarrow$ Muncul sebagai tugas pantau beraksen merah, diklik **otomatis membuka profil sapi**.
* **Keunggulan:** Zero double-entry. Begitu Pekerja A menginput pakan, layar Pekerja B yang membuka aplikasi langsung melihat tugas tersebut tercentang hijau.

### B. Background Notification & Quick Routing (Dokter Hewan / Vet)
* **Worker Background:** `VetNotificationWorker.kt` berbasis Android `CoroutineWorker`.
* **Mekanisme Mandiri:** Berjalan otomatis tiap 15 menit mengecek `health-records/emergency` dan `health-records/due`.
* **Penyaring Anti-Spam:** Menyimpan ID laporan yang sudah pernah dinotifikasikan di `SharedPreferences` agar tidak membunyikan notifikasi berulang.
* **Dual Channel:** Channel Darurat (High Priority, suara/getar) & Channel Vaksinasi (Default Priority).
* **Quick Jump:** Klik notifikasi darurat langsung menuju ke `ScanActivity` dengan nomor tag sapi yang sudah terisi otomatis (`EXTRA_INITIAL_TAG`).

---

## 4. Struktur File Terbaru Mobile (`SISapiMobile`)

```
app/src/main/java/com/latihan/si_sapimobile/
├── helpers/
│   ├── BottomNav.kt
│   ├── DateHelper.kt
│   ├── ErrorParser.kt         (Top-level function: Response<T>.errorMessage())
│   ├── ImageHelper.kt
│   ├── NotificationHelper.kt   (Dual channel: emergency & vaccine)
│   ├── SessionManager.kt
│   └── UiState.kt
├── models/
│   ├── FeedStock.kt
│   ├── HealthRecord.kt        (photo_url support)
│   ├── Livestock.kt
│   ├── NotifItem.kt
│   ├── User.kt
│   ├── WeightLogs.kt
│   └── WorkerTask.kt          (Model response shared daily tasks)
├── network/
│   ├── ApiClient.kt           (BASE_URL & Bearer interceptor)
│   └── ApiService.kt          (Retrofit interfaces lengkap)
├── services/
│   ├── AuthService.kt         (Constructor: private val context: Context)
│   ├── DashboardSevice.kt
│   ├── FeedConsumptionService.kt
│   ├── HealthRecordService.kt
│   ├── LivestockService.kt
│   ├── TaskService.kt         (Service getDailyTasks)
│   └── WeightLogService.kt
├── viewmodels/
│   ├── AuthViewModel.kt
│   ├── DashboardViewModel.kt
│   ├── FeedViewModel.kt
│   ├── HealthRecordViewModel.kt
│   ├── HistoryViewModel.kt
│   ├── ReminderViewModel.kt
│   ├── ScanViewModel.kt
│   ├── TaskViewModel.kt       (State management daily tasks)
│   └── WeightLogViewModel.kt
├── workers/
│   └── VetNotificationWorker.kt (WorkManager background checker untuk Vet)
└── ui/
    ├── DashboardActivity.kt
    ├── EmergencyReportActivity.kt
    ├── FeedInputActivity.kt
    ├── HistoryActivity.kt
    ├── LoginActivity.kt
    ├── MainActivity.kt        (Pusat navigasi BottomSheet: Worker Tasks vs Vet Notif)
    ├── ProfileActivity.kt
    ├── ReminderListActivity.kt
    ├── ScanActivity.kt
    ├── TreatmentActivity.kt
    └── WeightInputActivity.kt
```

---

## 5. Log Masalah & Solusi Teknis Terkini (Troubleshooting Reference)

| # | Masalah | Penyebab | Solusi |
|---|---|---|---|
| 1 | `Unresolved reference 'ErrorParser'` di `TaskService` | `errorMessage` adalah fungsi bebas (*top-level*), bukan class | Ubah import menjadi `import com.latihan.si_sapimobile.helpers.errorMessage` |
| 2 | `Function invocation 'context(...)' expected` di `AuthService` | Parameter constructor tanpa `val` tidak bisa diakses di fungsi `logout()` | Ubah baris 12 menjadi `class AuthService(private val context: Context)` |
| 3 | `No value passed for parameter 'tint'` & `ic_sky` di `MainActivity` | Salah ketik nama drawable dan parameter tint hilang | Ubah baris 332 menjadi `addTool("Scan QR", "Lihat profil sapi", R.drawable.ic_qr, R.color.tile_sky)` |
| 4 | Kartu Utilitas memanjang ke bawah & tombol lain hilang | Baris GridLayout diberi bobot vertikal `GridLayout.spec(UNDEFINED, 1f)` | Hapus bobot vertikal: `GridLayout.spec(GridLayout.UNDEFINED)` |
| 5 | Tampilan tugas menampilkan *"Unauthenticated."* | Token login lama di HP tidak cocok dengan database MySQL lokal | Lakukan Logout di HP lalu Login ulang dengan akun seeder lokal |
| 6 | `adb : The term 'adb' is not recognized` di PowerShell | ADB SDK belum masuk ke PATH sistem Windows | Jalankan dengan path lengkap: `& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8000 tcp:8000` |

---

## 6. Prosedur Uji Coba Cepat (Cheatsheet)

1. **Jalankan Backend:**
   ```powershell
   cd d:\last_ride\backend-peternakan-app
   php artisan serve --port=8000
   ```
2. **Reverse Port USB:**
   ```powershell
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8000 tcp:8000
   ```
3. **Akun Uji Coba:**
   * **Pekerja:** `worker@farm.com` | `password`
   * **Dokter Hewan:** `vet@farm.com` | `password`
   * **Manager:** `manager@farm.com` | `password`

---

## 7. Catatan Serah Terima untuk Pemolesan UI (Handover ke Argon)

1. **Komponen Layar Beranda (`MainActivity`):**
   * Panel atas: Header salam, nama, chip role, tanggal.
   * Panel tengah/bawah (BottomSheet):
     * Tab Pekerja: Daftar **Tugas Hari Ini** (kartu tugas selesai berwarna hijau, tugas pending berwarna kuning/oranye, tugas darurat merah).
     * Tab Vet: Daftar **Laporan Darurat & Jadwal Vaksin**.
   * Grid Utilitas: 4 tombol alat (formasi 2 x 2).
2. **Fokus Estetika:**
   * Perapian *elevation*, sudut membulat (*corner radius* 16-24dp), dan padding teks.
   * Penerapan palet warna resmi Si-Sapi (`colors_sisapi.xml`: `navy #1B1464`, `royal #2D5FAD`, `sky #1890DB`, `ok #17784F`, `danger #C62828`).
