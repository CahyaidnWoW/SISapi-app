# Checkpoint Perencanaan Project (v3)
## Si-Sapi — Tugas Akhir SMK

---

## 1. Informasi Umum
| Item | Keterangan |
|---|---|
| Nama Aplikasi/Ekosistem | **Si-Sapi** |
| Judul Laporan TA | *"Si-Sapi: Implementasi Arsitektur Client-Server pada Aplikasi Pemantauan dan Pelacakan Ternak Sapi Potong Berbasis QR Code"* |
| Studi Kasus | Peternakan Sapi Potong (khusus, bukan sapi perah) |
| Tujuan | Tugas Akhir/Skripsi SMK — membuktikan kelayakan nilai besar lewat scope berat yang **selesai utuh** |
| Model Pengerjaan | Solo |
| Durasi | 8 minggu |
| Arsitektur | Client-Server (1 backend, 2 client independen) |

---

## 2. Tech Stack Final
| Layer | Teknologi | Keterangan |
|---|---|---|
| Backend/API | Laravel + MySQL | Autentikasi via Sanctum, REST API, pola Service Layer |
| Desktop | C# WPF (konsumsi API) | Untuk Manager & kerja administratif Vet |
| Mobile | Kotlin (Android Native) | Retrofit/OkHttp, untuk Worker & kerja reaktif Vet di lapangan |

---

## 3. Batasan Masalah
- Sistem **hanya untuk sapi potong**, sapi perah di luar scope (dicatat sebagai saran pengembangan di BAB 5)
- FCR dihitung **per kandang**, didukung fakta praktik kandang koloni Indonesia yang cenderung homogen per kandang

---

## 4. Role & Hak Akses
| Role | Platform | Akses |
|---|---|---|
| Manager/Pemilik | Desktop | Full akses: dashboard, stok, analitik, manajemen user |
| Dokter Hewan (Vet) | Desktop & Mobile | Desktop: administratif/terjadwal. Mobile: reaktif/lapangan |
| Pekerja Kandang | Mobile | Input pakan harian, timbang sapi, lapor gejala sakit |

---

## 5. ERD — 9 Tabel (Final)
`users` · `cages` · `livestocks` · `livestock_breeds` · `weight_logs` · `health_records` · `feed_stocks` · `feed_stock_batches` · `feed_consumptions`

*(Detail kolom & relasi: lihat dokumen `erd-smart-livestock.md`)*

---

## 6. Dokumentasi Pendukung yang Sudah Dibuat
- [x] ERD lengkap (`erd-smart-livestock.md`)
- [x] Sequence Diagram 8 fitur utama, format `.docx` (`sequence-diagram-smart-livestock.docx`)
- [ ] Use Case Diagram — belum dibuat
- [ ] Flowchart per modul — belum dibuat
- [ ] ERD versi gambar (image) untuk lampiran — belum di-render terpisah dari markdown

---

## 7. List Fitur per Platform (dengan Prioritas)

### 🔴 WAJIB
**Backend:** Auth + role-based access · CRUD API livestocks/cages/feed_stocks · Endpoint scan by tag_number · Endpoint POST weight & feed consumption
**Desktop:** Login · CRUD Sapi/Kandang/Pegawai/Pakan · Dashboard grafik dasar · QR Code generator
**Mobile:** Login · Scanner QR → profil sapi · Input timbangan · Input pakan harian

### 🟡 PENTING TAPI BISA DISEDERHANAKAN DULU
FCR (rumus sederhana per kandang) · FIFO (batch sederhana → disempurnakan) · Dashboard (1-2 grafik dasar dulu)

### 🟢 BOLEH DIKORBANKAN DULUAN
Cron reminder vaksinasi otomatis · Foto di pelaporan darurat · Riwayat medis detail per individu · Manajemen user lengkap di desktop

---

## 8. Progres Saat Ini (per 22 Agustus 2026)

### Sprint 1 — Backend Foundation ✅ SELESAI
- Migration 9 tabel, model & relasi, seeder, Auth Sanctum — semua lulus test
- Master Data API (Cages, Breeds, Feeds) — semua lulus test

### Sprint 2 — Transaksi Utama 🔄 SEDANG BERJALAN
- [x] `LivestockService` + `LivestockController` — CRUD sapi, scan by tag_number, auto-generate tag & update populasi kandang — **lulus test**
- [x] `FeedConsumptionService` + `FeedConsumptionController` — logic FIFO potong stok otomatis lintas batch — **dibuat, siap ditest**
- [ ] `HealthRecordService` + `HealthRecordController` — belum dimulai
- [ ] `WeightLogController` — belum dimulai
- [ ] `FcrService` — belum dimulai

**Pola arsitektur:** Service Layer diterapkan mulai Sprint 2 — controller tipis, logic bisnis di `app/Services/`.

---

## 9. Roadmap 8 Minggu

| Minggu | Fokus | Status |
|---|---|---|
| 1 | Backend: Fondasi | ✅ Selesai |
| 2 | Backend: Core API + Transaksi | 🔄 Berjalan |
| 3-4 | Desktop (WPF) | ⏳ Belum mulai |
| 5-6 | Mobile (Kotlin) | ⏳ Belum mulai |
| 7 | Mobile lanjutan + polish | ⏳ Belum mulai |
| 8 | Integrasi & Deployment | ⏳ Belum mulai |

---

## 10. Agenda Selanjutnya
1. Testing Postman `FeedConsumptionController` (skenario: normal, lintas 2 batch, stok kurang)
2. `HealthRecordService` + `HealthRecordController`
3. `WeightLogController`
4. `FcrService`
5. Setelah Sprint 2 selesai → mulai Sprint 3 (Desktop WPF)

---

## 11. Status Checkpoint
- [x] Minggu 1 selesai
- [ ] Minggu 2 selesai (in progress)
- [ ] Minggu 3-4 selesai
- [ ] Minggu 5-6 selesai
- [ ] Minggu 7 selesai
- [ ] Minggu 8 selesai
