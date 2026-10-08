# Checkpoint Master Project & Dokumentasi (v10)
## Si-Sapi — Tugas Akhir SMK

**Tanggal Update:** 27 September 2026  
**Status Utama:** Fokus Backend & Desktop (Penyempurnaan & Pelengkapan CRUD), Mobile Ditunda

---

## 1. Identitas Project
| Item | Keterangan |
|---|---|
| Nama Aplikasi | Si-Sapi |
| Judul Laporan | "Si-Sapi: Implementasi Arsitektur Client-Server pada Aplikasi Pemantauan dan Pelacakan Ternak Sapi Potong Berbasis QR Code" |
| Studi Kasus | Peternakan Sapi Potong (Khusus sapi potong) |
| Model Pengerjaan | Solo |
| Keputusan Scope | Scope Penuh |

---

## 2. Status Pengerjaan Aplikasi

### A. Backend (Laravel API) — Status: Selesai & Dipenyempurnakan
- Total 19+ endpoint REST API aktif dan terverifikasi.
- Menerapkan pola Service Layer (`app/Services/`).
- **Update Terakhir:** Penambahan endpoint & validasi untuk fitur UPDATE (PUT/PATCH) dan DELETE pada modul Sapi (`livestocks`), Pakan (`feed_stocks`), Ras (`livestock_breeds`), dan Kandang (`cages`).
- **Integritas Data:** Penanganan Foreign Key Constraint (mencegah hapus data yang masih memiliki relasi transaksi aktif).

### B. Desktop (C# WPF) — Status: Selesai & Dipenyempurnakan
- Total 9 modul UI aktif (Login, MainShell, Dashboard, Kandang, Sapi, Pakan, QR Generator, Rekam Medis, FCR).
- Menerapkan pola Code-Behind + Service Layer (`SISapi_Desktop` namespace).
- **Update Terakhir:** Penyelarasan UX untuk operasi Edit dan Delete pada `FeedView`, `CageView`, dan `LivestockView`. Form edit disatukan secara efisien (reuse inline form/overlay) untuk menjaga konsistensi antarmuka tanpa menambah window baru.

### C. Mobile (Kotlin Android) — Status: Ditunda
- Dasar pondasi proyek sudah ada.
- Pengerjaan sengaja ditunda untuk memastikan ekosistem Backend dan Desktop tuntas 100% terlebih dahulu.

---

## 3. Rincian Penyempurnaan Modul Pakan (Feed Module)

1. **Backend (`FeedStockController.php`)**:
   - Method `update`: Mengubah data nama pakan dan unit.
   - Method `destroy`: Proteksi hapus jika pakan sudah memiliki riwayat pemakaian di `feed_consumptions`. Jika belum, menghapus record pakan beserta batch terkait di `feed_stock_batches`.

2. **Desktop Service (`FeedService.cs`)**:
   - Method `UpdateAsync(id, feedName, unit)` mengirim request `PUT`.
   - Method `DeleteAsync(id)` mengirim request `DELETE`.

3. **Desktop UI (`FeedView.xaml` & `FeedView.xaml.cs`)**:
   - Penambahan kolom **Aksi** (tombol Edit & Hapus) pada DataGrid `GridFeeds`.
   - Penambahan tombol **Batal** pada form atas untuk membatalkan mode edit.
   - Penanganan state form saat edit (mengisi data existing ke form, mengubah teks button menjadi "Simpan Perubahan", dan mengembalikan form ke mode tambah setelah selesai).

---

## 4. Status Dokumentasi Teknis & Laporan TA

| Dokumen / Bagian | Status | Keterangan |
|---|---|---|
| Use Case Diagram | Selesai | Terpisah untuk Desktop & Mobile |
| ERD | Selesai | 9 tabel + penjelasan |
| Class Diagram | Selesai | 9 kelas Model |
| Activity Diagram | Selesai | 13 diagram aktivitas |
| Sequence Diagram | Selesai | Detail alur View-Service-Controller-Model-DB |
| BAB I Pendahuluan | Selesai | Latar belakang, rumusan, batasan, tujuan |
| BAB II Landasan Teori | Selesai | Teori dasar, spek hardware & software |
| BAB III Perancangan | Sebagian Besar Selesai | Analisis kebutuhan & pemodelan selesai; menyisakan Wireframe/Mockup UI |
| BAB IV Implementasi & Testing | Pending | Menunggu pengujian sistematis & progres Mobile |
| BAB V Penutup | Pending | Menunggu seluruh bab tuntas |

---

## 5. Agenda Selanjutnya

1. Melakukan verifikasi akhir/smoke test pada fitur Update dan Delete di seluruh modul Desktop WPF.
2. Mempersiapkan materi screenshot UI dari aplikasi Desktop untuk melengkapi BAB III/IV Laporan TA.
3. Melanjutkan persiapan Sprint Mobile (Kotlin) setelah seluruh fitur Desktop dan Backend dipastikan stabil.