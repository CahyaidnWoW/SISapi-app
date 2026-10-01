# Checkpoint Project & Development Reference (v7 — Keseluruhan)
## Si-Sapi — Tugas Akhir SMK

**Tanggal Update:** 6 September 2026
**Status Backend:** ✅ 100% Selesai (Sprint 1 & 2)
**Status Desktop:** 🔄 Sedang Berjalan (Sprint 3) — Login, Shell, dan 1 modul CRUD selesai
**Status Mobile:** ⏳ Belum Dimulai

---

## 1. Judul & Identitas Project
| Item | Keterangan |
|---|---|
| Nama Aplikasi | **Si-Sapi** |
| Judul Laporan TA | *"Si-Sapi: Implementasi Arsitektur Client-Server pada Aplikasi Pemantauan dan Pelacakan Ternak Sapi Potong Berbasis QR Code"* |
| Studi Kasus | Peternakan Sapi Potong (khusus, bukan sapi perah) |
| Model Pengerjaan | Solo |
| Keputusan Scope | **Tetap penuh, tidak dipotong** — meski sempat molor jadwal karena kesibukan sekolah |

---

## 2. Status Roadmap 4 Minggu (Rencana Pemulihan)

| Minggu | Periode | Fokus | Status |
|---|---|---|---|
| 1 | 3–9 Sep | Backend: Tuntaskan seluruh API & Transaksi | ✅ SELESAI (4 Sep, 5 hari lebih cepat) |
| 2 | 10–16 Sep | Desktop (C# WPF) | 🔄 **Sedang berjalan, sudah mulai lebih awal (6 Sep)** |
| 3 | 17–23 Sep | Mobile (Kotlin) | ⏳ Belum Mulai |
| 4 | 24–30 Sep | Integrasi & Polishing + Laporan TA | ⏳ Belum Mulai |

---

## 3. Tech Stack & Arsitektur
| Layer | Teknologi | Pola Arsitektur |
|---|---|---|
| Backend/API | Laravel + MySQL | Service Layer (Controller tipis, logic di `app/Services`) |
| Desktop | C# WPF | UserControl + Service Layer (tanpa MVVM), `SISapi_Desktop` namespace |
| Mobile | Kotlin (Android Native) | Belum ditentukan detail, rencana Retrofit/OkHttp |

---

## 4. Backend — Ringkasan Final (Sprint 1 & 2)

### Endpoint Aktif: 19 total, semua lulus test
Auth (3) · Master Data: Cage, Breed, Feed (7) · Livestock (3) · Feed Consumption/FIFO (1) · Health Record (4) · Weight Log (1) · FCR Report (1)

### Modul Backend Selesai
- `AuthController`, `CageController`, `LivestockBreedController`, `FeedStockController`
- `LivestockController` + `LivestockService`
- `FeedConsumptionController` + `FeedConsumptionService` (FIFO)
- `HealthRecordController` + `HealthRecordService`
- `WeightLogController` + `WeightLogService`
- `FcrService`

### Kredensial & Data Seeder
| Role | Email | Password |
|---|---|---|
| Manager | manager@farm.com | password |
| Vet | vet@farm.com | password |
| Worker | worker@farm.com | password |

Data dummy: 3 ras (Simental, Limousin, Brahman), 2+ kandang, 2+ sapi (SP-001, SP-002), pakan & batch FIFO awal.

*(Detail lengkap: lihat `checkpoint-perencanaan-project-v6.md`)*

---

## 5. Desktop — Ringkasan Progres (Sprint 3)

### Selesai ✅
- Login (dengan fix field `access_token`)
- MainShellView — sidebar, routing UserControl, role-based menu, logout
- CageView — CRUD kandang penuh (jadi pola contoh modul selanjutnya)

### Belum ⏳
LivestockView, FeedView, HealthRecordView, QrGeneratorView, FcrReportView, DashboardView — semua masih skeleton kosong

### Pelajaran Penting dari Debugging
Konsistensi namespace (`SISapi_Desktop`) adalah sumber error paling sering selama Sprint 3 — sudah distandarkan, tapi tetap perlu kehati-hatian tiap membuat file baru.

*(Detail lengkap: lihat `checkpoint-desktop-v2.md`)*

---

## 6. Dokumentasi Pendukung — Status
| Dokumen | Status |
|---|---|
| ERD (9 tabel + penjelasan) | ✅ Selesai (.docx) |
| Sequence Diagram (8 fitur) | ✅ Selesai (.docx) |
| Use Case Diagram (3 aktor) | 🔄 Draft dibuat, belum difinalisasi jadi dokumen |
| Flowchart per modul | ⏳ Belum dibuat (opsional) |

---

## 7. Fitur — Status Implementasi per Platform

| Fitur | Backend | Desktop | Mobile |
|---|---|---|---|
| Auth (Login/Logout) | ✅ | ✅ | ⏳ |
| CRUD Kandang | ✅ | ✅ | — |
| CRUD Sapi | ✅ | ⏳ | — |
| CRUD Pakan + Stok | ✅ | ⏳ | — |
| Scan QR → Profil Sapi | ✅ | — | ⏳ |
| QR Generator | — | ⏳ | — |
| Input Timbangan | ✅ | — | ⏳ |
| Input Konsumsi Pakan (FIFO) | ✅ | — | ⏳ |
| Laporan Darurat (+ foto) | ✅ | — | ⏳ |
| Jadwal & Reminder Vaksinasi | ✅ | ⏳ | — |
| Dashboard (grafik, ringkasan) | ✅ (data) | ⏳ | — |
| Laporan FCR | ✅ | ⏳ | — |

---

## 8. Agenda Prioritas Saat Ini
1. **Desktop:** `LivestockView` + `LivestockService` (ikuti pola `CageView`)
2. **Desktop:** `FeedView`, `QrGeneratorView`, `HealthRecordView`, `FcrReportView`, `DashboardView` (urutan sesuai checkpoint Desktop)
3. Setelah Desktop selesai → mulai Sprint 3 Mobile (Kotlin)
4. Polish UI/UX di-defer ke akhir masing-masing platform

---

## 9. Pengingat Disiplin (Tetap Berlaku)
- Scope tidak dipotong — konsekuensinya jadwal harus benar-benar ketat, tanpa hari kosong berarti
- Checkpoint di-update tiap ada progres modul besar (bukan cuma mingguan) — supaya kalau ada keterlambatan lagi, cepat terdeteksi
- Minimal 3-4 hari terakhir (27-30 Sep) wajib dikhususkan untuk laporan TA & latihan sidang, bukan coding
- Backend selesai lebih cepat dari jadwal — momentum ini harus dijaga, jangan lengah di Desktop
