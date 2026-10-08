# Checkpoint Project & Development Reference (v8 — Keseluruhan)
## Si-Sapi — Tugas Akhir SMK

**Tanggal Update:** 6 September 2026
**Status Backend:** ✅ 100% Selesai (Sprint 1 & 2) — selesai 4 Sep
**Status Desktop:** ✅ 100% Selesai (Sprint 3) — selesai 6 Sep, 10 hari lebih cepat dari jadwal
**Status Mobile:** ⏳ Belum Dimulai

---

## 1. Judul & Identitas Project
| Item | Keterangan |
|---|---|
| Nama Aplikasi | **Si-Sapi** |
| Judul Laporan TA | *"Si-Sapi: Implementasi Arsitektur Client-Server pada Aplikasi Pemantauan dan Pelacakan Ternak Sapi Potong Berbasis QR Code"* |
| Studi Kasus | Peternakan Sapi Potong (khusus, bukan sapi perah) |
| Model Pengerjaan | Solo |
| Keputusan Scope | Tetap penuh, tidak dipotong |

---

## 2. Status Roadmap 4 Minggu — JAUH LEBIH CEPAT DARI RENCANA

| Minggu | Periode | Fokus | Status Aktual |
|---|---|---|---|
| 1 | 3–9 Sep | Backend | ✅ SELESAI 4 Sep (5 hari lebih cepat) |
| 2 | 10–16 Sep | Desktop (C# WPF) | ✅ **SELESAI 6 Sep (10 hari lebih cepat)** |
| 3 | 17–23 Sep | Mobile (Kotlin) | ⏳ Bisa mulai lebih awal — ada buffer besar |
| 4 | 24–30 Sep | Integrasi & Laporan TA | ⏳ Belum Mulai |

> **Insight penting:** dengan progres jauh di depan jadwal, ada waktu ekstra signifikan (berpotensi 1.5-2 minggu buffer) yang bisa dipakai untuk testing lebih dalam, penyempurnaan fitur, atau memulai laporan TA lebih awal — mengurangi risiko keterlambatan seperti yang sempat terjadi di awal project.

---

## 3. Tech Stack & Arsitektur
| Layer | Teknologi | Pola Arsitektur | Status |
|---|---|---|---|
| Backend/API | Laravel + MySQL | Service Layer | ✅ Selesai |
| Desktop | C# WPF | UserControl + Service Layer (tanpa MVVM) | ✅ Selesai |
| Mobile | Kotlin (Android Native) | Rencana: Retrofit/OkHttp | ⏳ Belum dimulai |

---

## 4. Backend — Ringkasan Final
19 endpoint aktif, semua lulus test sistematis (Postman). Modul: Auth, Master Data (Cage/Breed/Feed), Livestock, Feed Consumption (FIFO), Health Record, Weight Log, FCR Report.

*(Detail lengkap: `checkpoint-perencanaan-project-v6.md`)*

### Kredensial Seeder
| Role | Email | Password |
|---|---|---|
| Manager | manager@farm.com | password |
| Vet | vet@farm.com | password |
| Worker | worker@farm.com | password |

---

## 5. Desktop — Ringkasan Final
9 modul selesai: Login, MainShellView, CageView, LivestockView, FeedView, QrGeneratorView, HealthRecordView, FcrReportView, DashboardView.

**Catatan status:** level "developing/smoke test", belum testing sistematis per skenario — direncanakan di Sprint Integrasi.

**8 masalah debugging tercatat** — mayoritas soal kecocokan field JSON API vs model C#, dan konsistensi namespace. Pelajaran ini berlaku juga untuk Mobile nanti.

*(Detail lengkap: `checkpoint-desktop-v3.md`)*

---

## 6. Dokumentasi Pendukung — Status
| Dokumen | Status |
|---|---|
| ERD (9 tabel + penjelasan) | ✅ Selesai (.docx) |
| Sequence Diagram (8 fitur) | ✅ Selesai (.docx) |
| Use Case Diagram (3 aktor) | 🔄 Draft dibuat, belum difinalisasi jadi dokumen |
| Flowchart per modul | ⏳ Belum dibuat (opsional) |
| Screenshot/dokumentasi UI Desktop | ⏳ Belum dikumpulkan untuk laporan |

---

## 7. Fitur — Status Implementasi per Platform

| Fitur | Backend | Desktop | Mobile |
|---|---|---|---|
| Auth (Login/Logout) | ✅ | ✅ | ⏳ |
| CRUD Kandang | ✅ | ✅ | — |
| CRUD Sapi | ✅ | ✅ | — |
| CRUD Pakan + Stok (FIFO) | ✅ | ✅ | — |
| QR Generator | — | ✅ | — |
| Scan QR → Profil Sapi | ✅ | — | ⏳ |
| Input Timbangan | ✅ | — | ⏳ |
| Input Konsumsi Pakan | ✅ | — | ⏳ |
| Laporan Darurat (+ foto) | ✅ | 🔶 (tanpa foto, tanpa edit) | ⏳ |
| Jadwal & Reminder Vaksinasi | ✅ | ✅ (tampil di Dashboard) | — |
| Dashboard (ringkasan) | ✅ (data) | ✅ (tanpa grafik tren) | — |
| Laporan FCR | ✅ | ✅ (versi dasar) | — |

**Legenda:** ✅ Selesai · 🔶 Selesai versi sederhana · ⏳ Belum dikerjakan · — Tidak relevan di platform ini

---

## 8. Backlog Pengembangan Lanjutan (Opsional, Jika Ada Waktu Sisa)
- FCR: perbandingan ideal_fcr_range, grafik tren, filter tanggal
- Dashboard: grafik tren berat badan (LiveCharts/OxyPlot)
- HealthRecord: UI edit/update diagnosis untuk laporan darurat masuk
- QR Generator: cetak langsung ke printer

*(Ini kandidat isi BAB 5 "Saran Pengembangan" kalau tidak sempat dikerjakan)*

---

## 9. Agenda Prioritas Saat Ini
1. **Mulai Sprint Mobile (Kotlin)** — platform baru, belum ada pola sama sekali, waspada risiko technical debt baru
2. Pertimbangkan alokasi waktu buffer untuk testing sistematis Desktop atau mulai cicil laporan TA
3. Siapkan dokumentasi visual (screenshot tiap halaman Desktop) untuk lampiran laporan — lebih mudah dikumpulkan sekarang selagi masih fresh

---

## 10. Pengingat Disiplin
- Progres jauh di depan jadwal — pertahankan momentum, tapi jangan lengah karena Mobile adalah wilayah baru
- Scope tidak dipotong — tetap berlaku
- Minimal 3-4 hari terakhir wajib untuk laporan TA & latihan sidang, bukan coding
- Checkpoint di-update tiap progres modul besar
