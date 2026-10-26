# 🐄 Rekapitulasi Lengkap Proyek Si-Sapi (Dari Awal Hingga Terkini)
> **Dokumen Transisi & Konteks Komprehensif**  
> Dibuat untuk melanjutkan pengerjaan proyek, konsultasi, dan pengembangan bersama model AI baru (Gemini).

---

## 1. Identitas Proyek & Profil Pengembang

* **Nama Proyek / Aplikasi:** **Si-Sapi** (Smart Livestock Management System)
* **Judul Tugas Akhir / Ujikom:**  
  *"Si-Sapi: Implementasi Arsitektur Client-Server pada Aplikasi Pemantauan dan Pelacakan Ternak Sapi Potong Berbasis QR Code"*
* **Pengembang / Penulis:** Cahya Muhamad Rifan (Kelas XII RPL 1, NISN: 0096396686)
* **Sekolah:** SMKN 1 Sumedang (Tahun Ajaran 2026/2027)
* **Studi Kasus:** Peternakan Sapi Potong (khusus penggemukan/potong, bukan sapi perah)
* **Model Pengerjaan:** Solo Developer
* **Repository GitHub:** [`https://github.com/CahyaidnWoW/SISapi-app`](https://github.com/CahyaidnWoW/SISapi-app) (Branch: `main`)

---

## 2. Arsitektur Sistem & Tech Stack

Sistem dibangun dengan arsitektur **Client-Server sejati**: **1 Backend API melayani 2 Klien Independen**.

```
┌─────────────────────────────────────────────────────────────┐
│                     ARSITEKTUR SI-SAPI                      │
│                                                             │
│  ┌──────────────┐    HTTP / REST    ┌────────────────────┐  │
│  │ Desktop WPF  │◄─────────────────►│                    │  │
│  │  (C# .NET)   │                   │  Backend Laravel   │  │
│  │  Manager,Vet │                   │  + MySQL Database  │  │
│  └──────────────┘                   │  (Sanctum Token)   │  │
│                                     │                    │  │
│  ┌──────────────┐    HTTP / REST    │                    │  │
│  │Mobile Kotlin │◄─────────────────►│                    │  │
│  │  (Android)   │                   └────────────────────┘  │
│  │ Worker, Vet  │                                           │
│  └──────────────┘                                           │
└─────────────────────────────────────────────────────────────┘
```

### Rincian Peran & Layer:
1. **Backend API (`backend-peternakan-app`):**
   * **Framework:** Laravel + Sanctum (Token-based Authentication).
   * **Database:** MySQL (9 tabel utama).
   * **Pola Desain:** Service Layer Pattern (`app/Services/`) untuk memisahkan logika bisnis dari Controller.
   * **URL Server:** `http://sisapi.smkn1sumedang.sch.id/api/` (atau `http://localhost:8000/api/` saat pengujian lokal).

2. **Client Desktop (`SISapi_Desktop`):**
   * **Teknologi:** C# WPF (.NET Framework / .NET).
   * **Pengguna Utama:** Manager (Pemilik Peternakan) & Dokter Hewan (Vet).
   * **Fitur Utama:** Master data (Sapi, Kandang, Pakan), Cetak Label QR Ear Tag, Grafik & Ringkasan Dashboard, Rekam Medis & Jadwal Vaksinasi, Evaluasi Laporan FCR (Feed Conversion Ratio).

3. **Client Mobile (`SISapiMobile`):**
   * **Teknologi:** Kotlin Android Native (MVVM, Retrofit, OkHttp, ViewBinding).
   * **Pengguna Utama:** Pekerja Kandang (Worker) & Dokter Hewan (Vet).
   * **Fitur Utama:** Scan QR Ear Tag (ZXing) $\rightarrow$ Profil Sapi Instan, Input Timbangan Cepat, Input Pakan Harian per Kandang, Laporan Darurat Sapi Sakit dengan Kamera/Foto, Pengingat Vaksinasi.

---

## 3. Hak Akses & Role Pengguna

| Role | Platform Login | Tanggung Jawab & Hak Akses |
|---|---|---|
| **Manager** | Desktop | Hak akses penuh: Dashboard analitik, CRUD Master data sapi/kandang/pakan, QR generator, Laporan FCR. |
| **Dokter Hewan (Vet)** | Desktop & Mobile | Mengelola jadwal vaksinasi, mencatat diagnosis & resep/tindakan medis, merespons laporan darurat. |
| **Pekerja (Worker)** | Mobile | Operasional lapangan: Scan QR tag, timbang sapi berkala, catat pakan harian, lapor kondisi darurat berfoto. |

---

## 4. Kronologi & Perjalanan yang Telah Dilakukan

### Tahap 1: Re-Onboarding & Refresh Pemahaman Proyek
* Pengembang kembali mengerjakan proyek setelah vakum selama lebih dari 2 minggu.
* Dilakukan pemetaan ulang mengenai gambaran besar proyek, peran aktor, alur data, dan lokasi file source code.

### Tahap 2: Audit Kode Menyeluruh (Deep Code Review)
Dilakukan pembacaan terhadap **seluruh baris kode** di Backend, Desktop, Mobile, serta dokumen pendukung. Dari audit ini ditemukan beberapa fakta penting:
1. **Fitur Upload Foto Laporan Darurat:**
   * Di database, kolom `photo_path` di tabel `health_records` **sudah ada**.
   * Di backend, validasi `'photo' => 'nullable|image|max:5120'` dan logic upload `$photo->store('health-reports', 'public')` **sudah ada**.
   * Di Android, fungsi pemotretan kamera dan pengiriman Multipart form-data **sudah ada**.
2. **Bug Mismatch Edit Sapi (Livestock Update):**
   * *Method Mismatch:* Desktop memanggil HTTP `PUT`, tetapi Laravel sebelumnya hanya menerima `PATCH` (mengakibatkan `405 Method Not Allowed`).
   * *Schema Mismatch:* Desktop mengirimkan string nama ras (`breed = "Limousin"`), enum status bahasa Indonesia (`aktif`, `terjual`, `mati`), dan gender (`male`/`female`). Sedangkan backend sebelumnya memvalidasi `breed` sebagai teks kolom langsung (padahal kolom tabel MySQL adalah `breed_id`), status enum `healthy`/`sick`, dan gender `jantan`/`betina`.
   * *Syntax Quirks:* Terdapat penulisan `route::` huruf kecil di `routes/api.php`.
3. **Ketiadaan Penampil Foto di Desktop:**
   * Pekerja kandang sudah bisa mengirim foto sapi sakit dari HP Android, tetapi Dokter Hewan/Manager di aplikasi Desktop sebelumnya **tidak memiliki tombol atau antarmuka untuk melihat foto tersebut**.

### Tahap 3: Keputusan Strategis (Projek vs Laporan)
* **Pertanyaan Pengembang:** Apakah sebaiknya menyelesaikan projek dulu baru laporan, atau laporan dulu baru projek?
* **Keputusan:** Menggunakan metode **"Test & Harvest"**.
  * Selesaikan sisa perbaikan kode integrasi terlebih dahulu (cepat).
  * Jalankan sistem secara nyata. Sambil melakukan pengujian blackbox, **langsung tangkap tangkapan layar (screenshot) setiap layar aplikasi**.
  * Screenshot dan data pengujian nyata ini langsung menjadi isi utama untuk **BAB IV (Implementasi)** dan **BAB V (Hasil Pengujian)** pada laporan tugas akhir.

### Tahap 4: Eksekusi Perbaikan Kode & Push ke GitHub
Seluruh perbaikan teknis telah diimplementasikan dan dikomit langsung oleh pengembang ke GitHub:
* **Commit:** `253de3d`
* **Pesan Commit:** `"big revisi untuk update livestock, backend dan pemantapan android"`
* **Branch:** `main` (sudah ter-push ke `origin/main`).

---

## 5. Rincian Perubahan Kode pada Commit Terakhir (`253de3d`)

| File | Komponen | Perubahan Konkret |
|---|---|---|
| [`routes/api.php`](file:///d:/last_ride/backend-peternakan-app/routes/api.php) | Backend | Standarisasi `Route::` dan dukungan method ganda: `Route::match(['put', 'patch'], '/livestocks/{id}', [LivestockController::class, 'update'])`. |
| [`LivestockController.php`](file:///d:/last_ride/backend-peternakan-app/app/Http/Controllers/Api/LivestockController.php) | Backend | Fungsi `update()` kini memiliki `$genderMap` (normalisasi gender), `$statusMap` (normalisasi status), dan pencarian otomatis `breed_id` jika dikirim string nama ras. |
| [`HealthRecord.php`](file:///d:/last_ride/backend-peternakan-app/app/Models/HealthRecord.php) | Backend | Penambahan accessor `photo_url` (`$appends = ['photo_url']`) yang mengembalikan URL publik lengkap foto. |
| [`HealthRecord.cs`](file:///d:/last_ride/SISapi_Desktop/Models/HealthRecord.cs) | Desktop | Penambahan properti `[JsonProperty("photo_url")] public string PhotoUrl { get; set; }`. |
| [`HealthRecordView.xaml`](file:///d:/last_ride/SISapi_Desktop/Views/HealthRecordView.xaml) | Desktop | Penambahan kolom DataGrid **"Foto Bukti"** dengan tombol **"Lihat Foto"** yang otomatis muncul jika ada foto. |
| [`HealthRecordView.xaml.cs`](file:///d:/last_ride/SISapi_Desktop/Views/HealthRecordView.xaml.cs) | Desktop | Penambahan event handler `BtnViewPhoto_Click` untuk menampilkan jendela pop-up foto kondisi sapi darurat. |
| [`HealthRecord.kt`](file:///d:/last_ride/SISapiMobile/app/src/main/java/com/latihan/si_sapimobile/models/HealthRecord.kt) | Mobile | Penambahan properti `@SerializedName("photo_url") val photoUrl: String?`. |

---

## 6. Logika Bisnis Kunci yang Wajib Diingat

1. **Auto-Generate Nomor Tag Sapi (`SP-001`, `SP-002`, dst.):**
   * Diatur di `LivestockService.php`. Saat sapi didaftarkan, sistem otomatis mencari nomor urut terakhir dan menambahkan prefix `SP-`. Tag inilah yang dicetak ke QR code fisik di telinga sapi (Ear Tag).
2. **Logika FIFO Stok Pakan (First In, First Out):**
   * Diatur di `FeedConsumptionService.php`.
   * Pakan masuk dicatat per batch (`feed_stock_batches`) dengan tanggal kedatangan (`entry_date`).
   * Saat pakan digunakan, sistem secara otomatis memotong stok dari batch yang tanggal masuknya paling lama terlebih dahulu. Jika stok tidak mencukupi, sistem melempar exception `422`.
3. **Kalkulasi FCR (Feed Conversion Ratio):**
   * Diatur di `FcrService.php`.
   * Rumus: $\text{FCR} = \frac{\text{Total Pakan Dikonsumsi (kg)}}{\text{Total Kenaikan Berat Badan (kg)}}$
   * FCR mengukur efisiensi: semakin kecil nilainya, semakin sedikit pakan yang terbuang untuk menghasilkan daging.
4. **Proteksi Integritas Data (Industrial Guard):**
   * Sapi yang sudah memiliki riwayat timbangan atau rekam medis tidak boleh dihapus (`delete`), melainkan diubah statusnya menjadi `terjual` atau `mati`.
   * Pakan yang sudah pernah digunakan tidak boleh dihapus dari master data.

---

## 7. Status Dokumen Laporan Tugas Akhir

Panduan laporan mengikuti standar SMKN 1 Sumedang (`FORMAT LAPORAN TUGAS AKHIR UJIKOM.docx`):
* **BAB I (Pendahuluan):** ✅ Selesai & matang (Latar Belakang, 5 Rumusan Masalah, Batasan Masalah, 6 Tujuan, Manfaat).
* **BAB II (Landasan Teori):** ✅ Selesai (Spesifikasi teknologi, teori FCR & FIFO, spek laptop/perangkat).
* **BAB III (Analisis & Perancangan):** ✅ 80% Selesai:
  * Kebutuhan Fungsional (15 fitur) & Non-fungsional sudah ada.
  * Dokumen Diagram UML sudah lengkap di folder `dokumen pendukung/dokumen/`:
    * [`usecase-diagram-sisapi.docx`](file:///d:/last_ride/dokumen%20pendukung/dokumen/detail/usecase-diagram-sisapi.docx)
    * [`erd-sisapi.docx`](file:///d:/last_ride/dokumen%20pendukung/dokumen/erd-sisapi.docx) (9 tabel Crow's Foot)
    * [`class-diagram-sisapi.docx`](file:///d:/last_ride/dokumen%20pendukung/dokumen/class-diagram-sisapi.docx) (9 Model Eloquent)
    * [`activity-diagram-sisapi.docx`](file:///d:/last_ride/dokumen%20pendukung/dokumen/activity-diagram-sisapi.docx) (13 alur aktivitas)
    * [`sequence-diagram-detail-sisapi.docx`](file:///d:/last_ride/dokumen%20pendukung/dokumen/detail/sequence-diagram-detail-sisapi.docx) (8 alur detail)
  * *Tersisa:* Penyusunan screenshot antarmuka (wireframe/mockup).
* **BAB IV (Implementasi):** ⏳ Menunggu penyisipan tangkapan layar kode program kunci.
* **BAB V (Hasil Pengujian & Pembahasan):** ⏳ Menunggu pengisian tabel Blackbox Testing dan screenshot hasil uji coba fitur.
* **BAB VI (Kesimpulan & Saran):** ⏳ Menunggu Bab IV & V selesai.

---

## 8. Kredensial Akun & Cara Menjalankan Aplikasi

### Kredensial Uji Coba (Database Seeder):
| Role | Email | Password |
|---|---|---|
| **Manager** | `manager@farm.com` | `password` |
| **Dokter Hewan (Vet)** | `vet@farm.com` | `password` |
| **Pekerja (Worker)** | `worker@farm.com` | `password` |

### Cara Menjalankan:
1. **Backend:**
   ```powershell
   cd d:\last_ride\backend-peternakan-app
   php artisan serve --host=0.0.0.0 --port=8000
   ```
2. **Desktop WPF:**
   Buka file `SISapi_Desktop.slnx` di Visual Studio $\rightarrow$ Tekan **F5**.
3. **Mobile Android:**
   Buka folder `SISapiMobile` di Android Studio $\rightarrow$ Jalankan ke HP Android fisik (via USB/Wi-Fi) atau Emulator.
   *(Catatan URL: Jika menggunakan backend lokal di laptop, sesuaikan `BASE_URL` di `ApiClient.kt` ke IP laptop Anda, atau `10.0.2.2` untuk emulator).*

---

## 9. Panduan Langkah Selanjutnya (Untuk Sesi Gemini Baru)

Jika Anda memulai chat baru di Gemini, Anda cukup memberikan file ini atau meminta Gemini melakukan:
1. **Uji Coba Alur Utama (Smoke Testing):**
   * Skenario: Login $\rightarrow$ Tambah Sapi di Desktop $\rightarrow$ Cetak QR $\rightarrow$ Scan QR di Mobile $\rightarrow$ Input Timbang & Pakan $\rightarrow$ Lapor Darurat Berfoto $\rightarrow$ Lihat Foto & Laporan FCR di Desktop.
2. **Penyusunan BAB IV & V Laporan:**
   * Membuat tabel pengujian Blackbox (skenario input, hasil yang diharapkan, status valid).
   * Menempelkan screenshot hasil pengujian ke berkas laporan Word.
3. **Kompilasi Naskah Akhir Laporan:**
   * Menggabungkan BAB I sampai BAB VI ke template resmi SMKN 1 Sumedang.

---
*Dokumen ini mencakup 100% fakta teknis dan status kode riil di repository per 18 Oktober 2026.*
