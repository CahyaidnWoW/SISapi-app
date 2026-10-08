# Checkpoint Desktop Project (v2)
## Si-Sapi — Sprint 3 (C# WPF)

**Tanggal Update:** 6 September 2026
**Status:** Fondasi + 1 modul CRUD selesai, pola sudah terbukti jalan
**Prasyarat:** Backend (Sprint 1 & 2) 100% selesai, 19 endpoint aktif & lulus test

---

## 1. Keputusan Arsitektur (Tetap Berlaku)
- Tanpa MVVM — logic UI langsung di code-behind (`.xaml.cs`)
- Tetap pakai Service Layer — semua komunikasi API lewat `Services/`
- Semua fitur dibuat sebagai **UserControl** (bukan Window/Page), di-render lewat `ContentControl` di `MainShellView`

### ⚠️ Catatan Kritis: Konsistensi Namespace
Selama development ditemukan berkali-kali error akibat namespace tidak konsisten (`SiSapi.Desktop` vs `SiSapi_Desktop` vs `SISapi_Desktop`, `Helper` vs `Helpers`). **Namespace resmi yang dipakai project ini: `SISapi_Desktop`** (huruf besar semua di awal, underscore, sesuai nama project asli di Visual Studio). Semua file baru WAJIB ikut pola ini persis, cek ulang sebelum menambah file baru.

---

## 2. Struktur Direktori (Aktual Berjalan)

```
SISapi_Desktop/
│
├── App.xaml / App.xaml.cs
│
├── Models/
│   ├── User.cs                  ✅ (termasuk LoginRequest, LoginResponse)
│   └── Cage.cs                  ✅ (termasuk CreateCageRequest)
│
├── Services/
│   ├── ApiClient.cs             ✅ GetAsync<T>, PostAsync<T>, auto-inject Bearer Token
│   ├── AuthService.cs           ✅ LoginAsync
│   └── CageService.cs           ✅ GetAllAsync, CreateAsync
│
├── Views/
│   ├── LoginView.xaml(.cs)      ✅ Selesai & jalan
│   ├── MainShellView.xaml(.cs)  ✅ Selesai & jalan — sidebar, routing, role-based menu, logout
│   ├── DashboardView.xaml(.cs)  ⏳ Skeleton saja
│   ├── LivestockView.xaml(.cs)  ⏳ Skeleton saja
│   ├── CageView.xaml(.cs)       ✅ SELESAI — CRUD kandang penuh (list + tambah)
│   ├── FeedView.xaml(.cs)       ⏳ Skeleton saja
│   ├── HealthRecordView.xaml(.cs) ⏳ Skeleton saja
│   ├── QrGeneratorView.xaml(.cs) ⏳ Skeleton saja
│   └── FcrReportView.xaml(.cs)  ⏳ Skeleton saja
│
├── Helpers/
│   ├── SessionManager.cs        ✅ Token & CurrentUser
│   └── RoleHelper.cs            ✅ IsManager, IsVet, IsWorker
│
└── SISapi_Desktop.csproj
```

---

## 3. Modul Selesai — Detail

### Login ✅
- Form email/password, validasi kosong, error handling koneksi
- Fix: field API `access_token` (bukan `token`) — sudah disesuaikan di `LoginResponse`
- Setelah sukses → buka `MainShellView`, tutup `LoginView`

### MainShellView (Shell Utama) ✅
- Sidebar kiri + `ContentControl` kanan untuk menampilkan UserControl aktif
- `SetupMenuByRole()` — Vet tidak melihat menu Kandang/Pakan/QR/FCR
- Header menampilkan nama & role user yang login
- Logout mengembalikan ke `LoginView` & clear session

### CageView (CRUD Kandang) ✅ — Jadi Pola Contoh
- List kandang dari `GET /api/cages` tampil di DataGrid
- Form tambah kandang (nama, kapasitas, lokasi) → `POST /api/cages`
- Auto-refresh tabel setelah tambah berhasil
- Error handling ditampilkan di UI (TextBlock merah)
- **UI/UX masih dasar** — akan dipoles belakangan (prioritas fungsional dulu)

---

## 4. Log Debugging Sprint 3 (Bahan Lampiran BAB Implementasi)

| # | Masalah | Penyebab | Solusi |
|---|---|---|---|
| 1 | Login selalu gagal meski kredensial benar | Field JSON API `access_token`, model C# mengharapkan `token` | Sesuaikan `[JsonProperty]` di `LoginResponse` |
| 2 | 8x error CS0246 "type/namespace not found" untuk semua UserControl | Namespace `Views` tidak konsisten (`SiSapi.Desktop` vs `SiSapi_Desktop` vs `SISapi_Desktop`) | Samakan semua ke `SISapi_Desktop` via Find & Replace (Entire Solution) |
| 3 | `XDG-000: resource SidebarButton could not be resolved` | `<Window.Resources>` diletakkan di bawah `<Grid>`, padahal dipakai duluan di atas | Pindahkan `Window.Resources` ke posisi child pertama `<Window>` |
| 4 | `SetupMenuByRole` & `BtnLogout_Click` tidak ditemukan | Method dipanggil di constructor tapi belum didefinisikan di file (kelewat saat copy-paste) | Tambahkan definisi method yang hilang |

**Pelajaran untuk modul selanjutnya:** SELALU cek nama namespace persis sebelum membuat file baru, dan pastikan urutan elemen XAML (Resources harus duluan) benar dari awal.

---

## 5. Urutan Pengerjaan — Update Status

| # | Modul | Status |
|---|---|---|
| 1 | Setup project WPF + struktur folder | ✅ Selesai |
| 2 | `ApiClient.cs` + `AuthService.cs` | ✅ Selesai |
| 3 | `LoginView` | ✅ Selesai |
| 4 | `SessionManager` + `MainShellView` | ✅ Selesai |
| 5 | `CageView` + `CageService` | ✅ Selesai |
| 6 | `LivestockView` + `LivestockService` | ⏳ **Berikutnya** |
| 7 | `FeedView` + `FeedService` | ⏳ Belum |
| 8 | `QrGeneratorView` + `QrCodeGenerator` helper | ⏳ Belum |
| 9 | `HealthRecordView` + `HealthRecordService` | ⏳ Belum |
| 10 | `FcrReportView` + `FcrService` | ⏳ Belum |
| 11 | `DashboardView` (terakhir, butuh data modul lain) | ⏳ Belum |
| 12 | Polish UI/UX seluruh halaman | ⏳ Belum (setelah semua fungsional selesai) |

---

## 6. Agenda Selanjutnya
1. `Models/Livestock.cs` + `Models/LivestockBreed.cs`
2. `Services/LivestockService.cs` — GetAll, Create, GetByTag
3. `Views/LivestockView` — tabel sapi + form tambah (dengan dropdown kandang & ras, ambil dari `CageService`/`BreedService` yang sudah ada)
4. Tombol cetak QR per baris (bisa nyusul setelah `QrGeneratorView` dasar jalan)

---

## 7. Pengingat
- Target Sprint 3 (Desktop): **10-16 September 2026** — saat ini masih on track, bahkan mulai lebih awal dari jadwal
- Pola `CageView` (Model → Service → View dengan DataGrid + form) dipakai ulang persis untuk `Livestock`, `Feed`, dll — tidak perlu reinvent, tinggal adaptasi
- UI/UX polish di-defer ke akhir — jangan tergoda memperindah tampilan sebelum semua modul fungsional selesai
