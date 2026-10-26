# 📋 Checkpoint Proyek: Modul Desktop Si-Sapi Management System

Dokumen ini mendokumentasikan rekam jejak teknis, arsitektur, seluruh kendala (*troubleshooting*), dan solusi yang diimplementasikan pada aplikasi **Si-Sapi Desktop (WPF .NET Framework 4.8 / C# 7.3)** serta integrasinya dengan **Backend API Laravel 12**.

---

## 🏗️ 1. Arsitektur & Teknologi

- **Frontend Desktop**: WPF C# (.NET Framework 4.8, C# Language Version 7.3)
- **Komunikasi Data**: RESTful API via `ApiClient` kustom (didukung `Newtonsoft.Json`, otentikasi Bearer Token `SessionManager`)
- **Visualisasi Data**: `LiveCharts.Wpf` (CartesianChart, LineSeries)
- **Backend API**: Laravel 12 (Sanctum Authentication, DomPDF)
- **Database**: MySQL (Tabel: `users`, `cages`, `livestocks`, `weight_logs`, `health_records`, `feed_stocks`, `feed_consumptions`, `activity_logs`)

---

## 🛠️ 2. Rekam Masalah & Solusi dari Awal hingga Akhir

### 1. Kompatibilitas Versi C# (CS8370 - Target-typed `new()`)
- **Masalah**: Visual Studio memunculkan error kompilasi:
  ```text
  CS8370: Feature 'target-typed object creation' is not available in C# 7.3.
  ```
  Terjadi pada inisialisasi properti DTO seperti:
  ```csharp
  public List<UpcomingMedicalDto> UpcomingMedicals { get; set; } = new();
  ```
- **Penyebab**: Project berjalan pada spesifikasi C# 7.3 bawaan .NET Framework 4.8, di mana fitur target-typed new (`new()`) baru diperkenalkan pada C# 9.0+.
- **Solusi**: Mengganti semua inisialisasi instansi menjadi eksplisit:
  ```csharp
  public List<UpcomingMedicalDto> UpcomingMedicals { get; set; } = new List<UpcomingMedicalDto>();
  ```

---

### 2. Inkonsistensi Library JSON & HTTP Client
- **Masalah**: Service sempat dirancang memakai `System.Text.Json` dan menginstansiasi `HttpClient` manual dengan parameter URL dan token terpisah.
- **Penyebab**: Bertentangan dengan konvensi proyek yang sudah menyediakan abstraksi `ApiClient.cs` berbasis `Newtonsoft.Json` dan `SessionManager.Token`.
- **Solusi**: 
  - Merefaktor [DashboardService.cs](file:///d:/last_ride/SISapi_Desktop/Services/DashboardService.cs) agar menginstansiasi parameterless `_apiClient = new ApiClient();`.
  - Menggunakan atribut `[JsonProperty("...")]` pada Model [Dashboard.cs](file:///d:/last_ride/SISapi_Desktop/Models/Dashboard.cs) untuk memetakan snake_case dari Laravel ke PascalCase di C#.

---

### 3. Syntax Typo & XAML Invalid Markup
- **Masalah**: Muncul error `CS1022: Type or namespace definition, or end-of-file expected` dan `XLS0503: A value of type 'String' cannot be added to a collection of type 'UIElementCollection'`.
- **Penyebab**: 
  - Pada [Dashboard.cs](file:///d:/last_ride/SISapi_Desktop/Models/Dashboard.cs) baris 6 terdapat karakter kurung siku terselip (`{]`).
  - Pada [DashboardView.xaml](file:///d:/last_ride/SISapi_Desktop/Views/DashboardView.xaml) baris 14 terdapat karakter sama dengan (`=`) yang tertinggal di bawah definisi baris grid.
- **Solusi**: Menghapus kedua karakter typo tersebut sehingga parsing XAML dan kompilasi C# berhasil sepenuhnya.

---

### 4. Penyelarasan Model & Skema Database Backend
- **Masalah**: API merespons status 500 dengan pesan:
  ```text
  Class "App\Models\Cattle" not found at DashboardController.php:19
  ```
- **Penyebab**: Controller backend menggunakan asumsi model `Cattle` dan `Vaccination`. Struktur database sebenarnya menggunakan:
  - Model ternak: [Livestock.php](file:///d:/last_ride/backend-peternakan-app/app/Models/Livestock.php) (`status`: healthy, sick, sold, dead).
  - Model rekam medis/vaksin: [HealthRecord.php](file:///d:/last_ride/backend-peternakan-app/app/Models/HealthRecord.php) (`type`: vaccination, treatment, emergency_report).
  - Timbangan bobot: [WeightLog.php](file:///d:/last_ride/backend-peternakan-app/app/Models/WeightLog.php) (kolom tanggal: `date`).
  - Stok pakan: [FeedStock.php](file:///d:/last_ride/backend-peternakan-app/app/Models/FeedStock.php) yang dihitung dari relasi `batches` (`quantity_remaining`).
- **Solusi**: Menyelaraskan seluruh logika query di [DashboardController.php](file:///d:/last_ride/backend-peternakan-app/app/Http/Controllers/Api/DashboardController.php) menggunakan model dan relasi Eloquent yang sebenarnya.

---

### 5. Penyelarasan Tampilan UI Tabel (Design System Consistency)
- **Masalah**: Tabel pada Dashboard dan Rekam Medis awalnya menggunakan gaya default WPF (header abu-abu kaku dengan border tebal, tinggi baris sempit, seleksi warna biru kontras).
- **Solusi**: Mengadopsi design system dari [CageView.xaml](file:///d:/last_ride/SISapi_Desktop/Views/CageView.xaml):
  - **Header Tabel**: Latar `#F1F5F9`, teks tebal `#1E293B`, border bawah `#CBD5E1`.
  - **Baris Data**: Tinggi dinaikkan menjadi `RowHeight="42"`, garis pembatas horizontal `#E2E8F0`, padding sel rapi.
  - **Warna Seleksi**: Menggunakan biru lembut `#E0F2FE` dengan teks `#0284C7`.
  - **Container Card**: Sudut melengkung `CornerRadius="10"`, border tipis `#CBD5E1`, dan bayangan halus `DropShadowEffect`.

---

### 6. Optimasi Grafik Bobot Sapi (Mengatasi Dual-Axis Clash)
- **Masalah**: Grafik gabungan antara rata-rata bobot (Line) dan ADG (Bar) sulit dibaca karena batang ADG menabrak dan menutupi garis bobot. Selain itu, bulan pertama turun ke 0 sehingga sumbu Y tertarik ke angka `-100`.
- **Solusi (Opsi 2)**:
  - Grafik difokuskan murni untuk **Tren Pertumbuhan Bobot Sapi** (Line biru melengkung halus dengan arsiran lembut di bawahnya).
  - Menambahkan garis putus-putus merah horizontal sebagai **Target Siap Jual (450 kg)**.
  - Sumbu Y dibatasi dengan `MinValue="0"`.
  - Pada backend, jika belum ada penimbangan di bulan awal, sistem fallback ke `initial_weight` sehingga tidak terjadi drop ke 0.
  - Metrik ADG tetap dipantau secara jelas pada KPI Card atas.

---

### 7. Sistem Audit Trail / Log Aktivitas Multi-Platform
- **Kebutuhan**: Mencatat seluruh operasi perubahan data (Create, Update, Delete) yang dilakukan oleh Desktop (Manager) maupun Mobile (Pekerja Lapangan).
- **Solusi**:
  - **Backend**:
    - Dibuatkan tabel [create_activity_logs_table.php](file:///d:/last_ride/backend-peternakan-app/database/migrations/2026_10_23_000000_create_activity_logs_table.php) dan model [ActivityLog.php](file:///d:/last_ride/backend-peternakan-app/app/Models/ActivityLog.php).
    - Middleware [LogActivity.php](file:///d:/last_ride/backend-peternakan-app/app/Http/Middleware/LogActivity.php) otomatis mencegat request metode `POST`, `PUT`, `PATCH`, dan `DELETE`.
    - Otomatis mendeteksi platform: jika role user adalah `worker`, log ditandai sebagai `mobile` tanpa perlu merombak aplikasi mobile.
  - **Desktop**:
    - Dibuatkan view tersendiri [ActivityLogView.xaml](file:///d:/last_ride/SISapi_Desktop/Views/ActivityLogView.xaml) & [ActivityLogView.xaml.cs](file:///d:/last_ride/SISapi_Desktop/Views/ActivityLogView.xaml.cs) lengkap dengan badge platform dan pencarian real-time.

---

### 8. Perbaikan Status Tombol Sidebar (Active State Bug)
- **Masalah**: Tombol "Log Aktivitas" di sidebar menyala terus bersamaan dengan tombol menu lain yang sedang aktif.
- **Penyebab**: Objek tombol `BtnActivityLog` belum didaftarkan ke dalam array pembersihan tombol pada method `ResetSidebarButtons()` di [MainShellView.xaml.cs](file:///d:/last_ride/SISapi_Desktop/Views/MainShellView.xaml.cs).
- **Solusi**: Menambahkan `BtnActivityLog` ke dalam daftar array `buttons`.

---

### 9. Fitur Ekspor Laporan & Resolusi Format Dokumen
- **Dilema UX Dropdown**: Menaruh dropdown periode di samping tombol ekspor pada header dashboard berpotensi membuat user mengira itu filter data dashboard di layar.
  - *Solusi*: Header dashboard hanya diberi satu tombol bersih **`[ Ekspor Laporan ]`** yang membuka modal [ExportReportsDialog.xaml](file:///d:/last_ride/SISapi_Desktop/Views/ExportReportsDialog.xaml).
- **Tabel PDF Terpotong**: Tabel laporan PDF terpotong di margin kanan.
  - *Solusi*: Memisahkan CSS ke [styles.blade.php](file:///d:/last_ride/backend-peternakan-app/resources/views/reports/partials/styles.blade.php), menerapkan `table-layout: fixed`, `word-wrap: break-word`, dan menetapkan persentase lebar kolom tepat 100%.
- **Kop Surat Resmi**: Laporan dilengkapi kop surat resmi dengan garis ganda, alamat lengkap peternakan, lembar tanda tangan basah, dan slot logo kanan-kiri yang aman dengan fallback placeholder otomatis.
- **Resolusi Format Excel vs PDF**:
  - Format CSV memiliki keterbatasan alami (plain text tanpa gaya, kolom berdempetan, dan ketergantungan regional setting Windows).
  - Diputuskan **fokus 100% pada Dokumen PDF Resmi**, menyederhanakan modal dialog menjadi khusus cetak PDF, serta membersihkan method dan route Excel yang tidak terpakai dari [ReportController.php](file:///d:/last_ride/backend-peternakan-app/app/Http/Controllers/Api/ReportController.php) dan [routes/api.php](file:///d:/last_ride/backend-peternakan-app/routes/api.php).

---

## 📂 3. Daftar File Kunci Modul Desktop & Terkait

| Bagian | Path File | Keterangan |
| :--- | :--- | :--- |
| **View Utama** | [DashboardView.xaml](file:///d:/last_ride/SISapi_Desktop/Views/DashboardView.xaml) | Dashboard KPI, tren bobot, dan jadwal vaksin |
| **Code-Behind** | [DashboardView.xaml.cs](file:///d:/last_ride/SISapi_Desktop/Views/DashboardView.xaml.cs) | Logika pemanggilan async & rendering LiveCharts |
| **Model DTO** | [Dashboard.cs](file:///d:/last_ride/SISapi_Desktop/Models/Dashboard.cs) | Data Transfer Object ringkasan dashboard |
| **Log View** | [ActivityLogView.xaml](file:///d:/last_ride/SISapi_Desktop/Views/ActivityLogView.xaml) | Tampilan monitoring aktivitas audit trail |
| **Modal Ekspor** | [ExportReportsDialog.xaml](file:///d:/last_ride/SISapi_Desktop/Views/ExportReportsDialog.xaml) | Dialog pemilihan periode laporan PDF |
| **HTTP Client** | [ApiClient.cs](file:///d:/last_ride/SISapi_Desktop/Services/ApiClient.cs) | Gateway REST API dan download biner byte array |
| **Service Data** | [DashboardService.cs](file:///d:/last_ride/SISapi_Desktop/Services/DashboardService.cs) | Service penghubung API dashboard & report |
| **Backend API** | [ReportController.php](file:///d:/last_ride/backend-peternakan-app/app/Http/Controllers/Api/ReportController.php) | Controller generate PDF via DomPDF |
| **Template PDF**| [periodic.blade.php](file:///d:/last_ride/backend-peternakan-app/resources/views/reports/periodic.blade.php) | Layout kertas laporan A4 ber-kop surat resmi |
