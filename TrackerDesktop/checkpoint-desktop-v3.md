# Checkpoint Desktop Project (v3 — TUNTAS)
## Si-Sapi — Sprint 3 (C# WPF)

**Tanggal Update:** 6 September 2026
**Status:** ✅ SEMUA FITUR WAJIB SELESAI (development level — belum testing sistematis)
**Selesai:** 10 hari lebih cepat dari jadwal (target 16 Sep, selesai 6 Sep)

---

## 1. Ringkasan Pencapaian
Seluruh 9 modul Desktop selesai dikerjakan dalam waktu sangat singkat berkat pola yang konsisten (Model → Service → UserControl) yang terbukti sejak `CageView`. Semua modul mengonsumsi endpoint backend yang sudah 100% lulus test sebelumnya.

**Level status:** "Developing — build sukses, fitur utama berfungsi" (smoke test), BUKAN "lulus test sistematis" seperti backend. Testing menyeluruh per skenario (input invalid, edge case, dsb) belum dilakukan — direncanakan sebagai bagian dari Sprint Integrasi (Minggu 4).

---

## 2. Keputusan Arsitektur (Final)
- Tanpa MVVM — logic UI langsung di code-behind
- Service Layer tetap konsisten di semua modul
- Semua fitur berbentuk **UserControl**, dirender lewat `ContentControl` di `MainShellView`
- Namespace resmi: **`SISapi_Desktop`** (sudah distandarkan di semua file)

---

## 3. Modul Selesai — Detail Lengkap

| # | Modul | Fitur | Endpoint API Dipakai |
|---|---|---|---|
| 1 | Login | Form login, validasi, error handling, simpan token | `POST /login` |
| 2 | MainShellView | Sidebar, routing UserControl, role-based menu, logout | `GET /me` (implisit via SessionManager) |
| 3 | CageView | List + tambah kandang | `GET/POST /cages` |
| 4 | LivestockView | List + tambah sapi (dropdown kandang & ras) | `GET/POST /livestocks`, `GET /cages`, `GET /breeds` |
| 5 | FeedView | List jenis pakan + total stok, tambah jenis, tambah batch FIFO | `GET/POST /feeds`, `POST /feeds/batches` |
| 6 | QrGeneratorView | Generate QR dari tag_number, preview, simpan PNG massal ke folder | `GET /livestocks` (data untuk generate) |
| 7 | HealthRecordView | Muat riwayat medis by tag, tambah record baru (vaksin/treatment/darurat) | `GET/POST /livestocks/{tag}/health-report`, `GET /livestocks/{tag}/health-records` |
| 8 | FcrReportView | Pilih kandang, tampilkan FCR + komponen perhitungan | `GET /cages/{id}/fcr` |
| 9 | DashboardView | Card ringkasan (total sapi, kandang, populasi), panel vaksin jatuh tempo | Gabungan `GetAllAsync()` dari Livestock, Cage, HealthRecord service |

---

## 4. Log Debugging Sprint 3 (Lengkap — Bahan Lampiran BAB Implementasi)

| # | Masalah | Penyebab | Solusi |
|---|---|---|---|
| 1 | Login selalu gagal meski kredensial benar | Field JSON API `access_token`, model C# mengharapkan `token` | Sesuaikan `[JsonProperty]` di `LoginResponse` |
| 2 | 8x error CS0246 untuk semua UserControl | Namespace `Views` tidak konsisten (`SiSapi.Desktop` vs `SiSapi_Desktop` vs `SISapi_Desktop`) | Samakan semua ke `SISapi_Desktop` via Find & Replace |
| 3 | `XDG-000: resource SidebarButton could not be resolved` | `<Window.Resources>` diletakkan setelah `<Grid>` | Pindahkan ke posisi child pertama `<Window>` |
| 4 | `SetupMenuByRole` & `BtnLogout_Click` tidak ditemukan | Method dipanggil tapi belum didefinisikan (kelewat saat copy-paste) | Tambahkan definisi method yang hilang |
| 5 | CS8370: `using var` tidak didukung | Project pakai C# 7.3, `using var` butuh C# 8+ | Ganti ke `using (...) { }` gaya lama |
| 6 | CS0234: namespace `Forms` tidak ditemukan | `System.Windows.Forms` belum ditambahkan sebagai reference | Add Reference → Assemblies → Framework → centang System.Windows.Forms |
| 7 | 401 Unauthorized di endpoint FCR (Postman) | Typo header: `Bearer15|...` tanpa spasi setelah "Bearer" | Tambahkan spasi: `Bearer 15|...` (bukan bug kode) |
| 8 | FCR di Desktop menampilkan data salah/kosong | Response API dibungkus `{status, data: {...}}` dengan nama field berbeda (`total_feed_consumed_kg` dll), model C# tidak cocok | Buat `FcrReportWrapper`, sesuaikan `[JsonProperty]`, ambil `.Data` di service |

**Pelajaran utama Sprint 3:** mayoritas bug bukan soal logic, tapi soal **kecocokan kontrak data** (nama field JSON vs model C#) dan **konsistensi penamaan** (namespace). Kedua hal ini penting dicek di awal tiap modul baru sebelum lanjut coding fitur.

---

## 5. Fitur yang Disederhanakan (Potensi Pengembangan Lanjutan)
Dicatat sebagai kandidat "Saran Pengembangan" BAB 5 laporan, atau dikerjakan di buffer waktu Minggu 4 kalau sempat:

- **FCR:** perbandingan dengan `ideal_fcr_range` per breed, grafik tren dari waktu ke waktu, filter rentang tanggal, export PDF
- **Dashboard:** grafik tren berat badan (LiveCharts/OxyPlot) — saat ini baru card angka + panel reminder
- **HealthRecord:** belum ada tombol edit/update untuk melengkapi diagnosis pada laporan darurat yang masuk dari mobile (endpoint PATCH sudah ada di backend, tinggal UI-nya)
- **QR Generator:** cetak langsung ke printer (saat ini generate PNG untuk diprint manual)

---

## 6. Struktur Direktori Final (Aktual)

```
SISapi_Desktop/
├── Models/ (User, Cage, Livestock, LivestockBreed, FeedStock, HealthRecord, FcrReport)
├── Services/ (ApiClient, AuthService, CageService, LivestockService, BreedService,
│              FeedService, HealthRecordService, FcrService, DashboardService)
├── Views/ (LoginView, MainShellView, DashboardView, LivestockView, CageView,
│           FeedView, HealthRecordView, QrGeneratorView, FcrReportView)
├── Helpers/ (SessionManager, RoleHelper, QrCodeGenerator)
└── SISapi_Desktop.csproj
```

---

## 7. Agenda Selanjutnya
1. Mulai perencanaan **Sprint Mobile (Kotlin)** — target awal Minggu 3 (17-23 Sep), tapi bisa mulai lebih awal mengikuti pola sebelumnya
2. Testing sistematis Desktop (opsional, bisa digabung ke sesi integrasi Minggu 4) — atau dikerjakan sekarang selagi momentum bagus
3. Pertimbangkan mengisi salah satu item "pengembangan lanjutan" di atas kalau ada waktu sisa sebelum Mobile dimulai

---

## 8. Pengingat
- **Backend selesai 4 Sep, Desktop selesai 6 Sep** — kedua sprint besar tuntas jauh lebih cepat dari rencana pemulihan awal
- Sisa waktu ekstra ini SANGAT BERHARGA — pertimbangkan alokasikan ke: (a) testing lebih dalam, (b) buffer kalau Mobile ternyata lebih sulit dari perkiraan, atau (c) mulai cicil laporan TA dari sekarang
- Jangan lengah — Mobile (Kotlin) adalah platform yang belum pernah disentuh sama sekali di project ini, risiko technical debt baru lebih tinggi dibanding Desktop yang sudah punya banyak pola siap pakai
