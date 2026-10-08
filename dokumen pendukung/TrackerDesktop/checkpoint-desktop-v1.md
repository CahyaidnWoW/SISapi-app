# Checkpoint Desktop Project (v1)
## Si-Sapi — Sprint 3 (C# WPF)

**Tanggal Dibuat:** 4 September 2026
**Status:** Perencanaan struktur selesai, siap mulai coding
**Prasyarat:** Backend (Sprint 1 & 2) 100% selesai, 19 endpoint aktif & lulus test

---

## 1. Keputusan Arsitektur
- **Konsep:** Simpel tapi tetap mudah dirawat
- **Tanpa MVVM** — logic UI langsung di code-behind (`.xaml.cs`), tidak perlu ViewModel terpisah
- **Tetap pakai Service Layer** — semua komunikasi ke REST API terpusat di `Services/`, konsisten dengan pola yang sudah diterapkan di backend Laravel

### Aturan Main
1. `.xaml.cs` boleh langsung mengisi data ke UI, tapi **tidak boleh** `new HttpClient()` langsung — selalu lewat `Services/`
2. Logic yang dipakai di lebih dari satu halaman (format tanggal, validasi input, dll) taruh di `Helpers/`, jangan duplikat di tiap `.xaml.cs`
3. `SessionManager` adalah satu-satunya sumber kebenaran untuk data user yang login & token — jangan simpan token di variable lokal per halaman

---

## 2. Struktur Direktori Final

```
SiSapi.Desktop/
│
├── App.xaml / App.xaml.cs
│
├── Models/                      ← POCO, mengikuti struktur JSON API
│   ├── User.cs
│   ├── Cage.cs
│   ├── Livestock.cs
│   ├── LivestockBreed.cs
│   ├── WeightLog.cs
│   ├── HealthRecord.cs
│   ├── FeedStock.cs
│   └── FeedConsumption.cs
│
├── Services/                    ← komunikasi ke REST API
│   ├── ApiClient.cs             ← HttpClient dasar, base URL, inject Bearer Token
│   ├── AuthService.cs
│   ├── LivestockService.cs
│   ├── CageService.cs
│   ├── FeedService.cs
│   ├── HealthRecordService.cs
│   ├── WeightLogService.cs
│   └── FcrService.cs
│
├── Views/                       ← XAML + code-behind (otomatis berpasangan)
│   ├── LoginView.xaml(.cs)
│   ├── MainShellView.xaml(.cs)  ← sidebar + area konten
│   ├── DashboardView.xaml(.cs)
│   ├── LivestockView.xaml(.cs)
│   ├── CageView.xaml(.cs)
│   ├── FeedView.xaml(.cs)
│   ├── HealthRecordView.xaml(.cs)
│   ├── QrGeneratorView.xaml(.cs)
│   └── FcrReportView.xaml(.cs)
│
├── Helpers/
│   ├── SessionManager.cs        ← simpan token & data user login (static)
│   ├── RoleHelper.cs            ← cek permission per role untuk tampilan menu
│   └── QrCodeGenerator.cs       ← wrapper library QRCoder
│
├── Assets/                      ← logo, ikon, gambar
│
└── SiSapi.Desktop.csproj
```

---

## 3. Rancangan Halaman & Fitur

| Halaman | Role | Fitur Utama |
|---|---|---|
| Login | Semua | Form login, validasi, simpan token, redirect sesuai role |
| Dashboard | Manager (utama), Vet (ringkas) | Card ringkasan (total sapi, kandang, populasi), grafik tren berat |
| Sapi | Manager | Tabel data + search, tambah/edit sapi, tombol cetak QR per baris, detail (tab: info/riwayat berat/riwayat medis) |
| Kandang | Manager | Card grid per kandang (populasi vs kapasitas), tambah kandang, lihat isi kandang |
| Pakan | Manager | Tabel jenis pakan + stok, tambah jenis pakan, tambah batch stok |
| Rekam Medis | Manager (lihat), Vet (kelola) | Tabel laporan medis dengan badge tipe, input diagnosis/treatment, jadwal vaksin baru |
| QR Generator | Manager | Pilih sapi (multi-select), preview QR, cetak/export label massal |
| Laporan FCR | Manager | Pilih kandang, tampilkan FCR + komponen perhitungan (total pakan, kenaikan berat) |

**Navigasi:** Sidebar kiri (menu berbeda per role) + area konten kanan (`MainShellView` sebagai host).

---

## 4. Referensi Endpoint API yang Akan Dikonsumsi

| Endpoint | Dipakai di Halaman |
|---|---|
| `POST /api/login` | Login |
| `GET /api/me` | Login (ambil profil setelah login) |
| `GET /api/cages`, `POST /api/cages`, `GET /api/cages/{id}` | Kandang |
| `GET /api/breeds`, `POST /api/breeds` | Sapi (dropdown ras), Master Data |
| `GET /api/feeds`, `POST /api/feeds/batches` | Pakan |
| `GET /api/livestocks`, `POST /api/livestocks`, `GET /api/livestocks/{tagNumber}` | Sapi, QR Generator |
| `POST /api/cages/{id}/feed` | (Tidak dipakai langsung di Desktop — ini fitur Mobile) |
| `POST /api/livestocks/{tagNumber}/health-report`, `GET /api/livestocks/{tagNumber}/health-records`, `PATCH /api/health-records/{id}`, `GET /api/health-records/due` | Rekam Medis |
| `POST /api/livestocks/{tagNumber}/weight` | (Tidak dipakai langsung di Desktop — ini fitur Mobile) |
| `GET /api/cages/{id}/fcr` | Laporan FCR |

---

## 5. Urutan Pengerjaan (Prioritas)
1. Setup project WPF baru + struktur folder
2. `ApiClient.cs` + `AuthService.cs` — fondasi koneksi ke backend
3. `LoginView` — validasi alur login end-to-end sebelum lanjut ke halaman lain
4. `SessionManager` + `MainShellView` (sidebar + routing antar halaman)
5. `CageView` + `CageService` (CRUD paling sederhana, cocok jadi pola contoh)
6. `LivestockView` + `LivestockService`
7. `FeedView` + `FeedService`
8. `QrGeneratorView` + `QrCodeGenerator` helper
9. `HealthRecordView` + `HealthRecordService`
10. `FcrReportView` + `FcrService`
11. `DashboardView` (dikerjakan terakhir karena butuh data dari semua modul lain untuk ringkasan)

---

## 6. Progres Saat Ini
- [x] Struktur direktori & arsitektur disepakati
- [x] Rancangan halaman & fitur per role
- [ ] Setup project WPF — belum dimulai
- [ ] `ApiClient.cs` + `AuthService.cs` — belum dimulai
- [ ] Semua Views — belum dimulai

---

## 7. Pengingat
- Target Sprint 3 (Desktop): **10-16 September 2026**
- Jangan mulai halaman baru sebelum halaman sebelumnya bisa konek ke API dengan benar (terutama Login harus tuntas dulu sebelum lanjut)
- Checkpoint ini akan di-update (v2, v3, dst) seiring progres coding
