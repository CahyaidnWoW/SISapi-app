# Checkpoint Master — Project & Dokumentasi (v9)
## Si-Sapi — Tugas Akhir SMK

**Tanggal Update:** 11 September 2026
**Deadline:** Akhir September 2026 (~3 minggu tersisa)

---

## 1. Identitas Project
| Item | Keterangan |
|---|---|
| Nama Aplikasi | **Si-Sapi** (sudah punya logo resmi — biru navy, siluet sapi + bel kalung) |
| Judul Laporan | *"Si-Sapi: Implementasi Arsitektur Client-Server pada Aplikasi Pemantauan dan Pelacakan Ternak Sapi Potong Berbasis QR Code"* |
| Studi Kasus | Peternakan Sapi Potong (khusus, bukan sapi perah) |
| Model Pengerjaan | Solo |
| Keputusan Scope | Tetap penuh, tidak dipotong |

---

## 2. STATUS CODING

### Backend (Laravel) — ✅ 100% SELESAI
19 endpoint aktif, semua lulus test sistematis (Postman). Modul: Auth, Master Data (Cage/Breed/Feed), Livestock, Feed Consumption (FIFO), Health Record, Weight Log, FCR Report. Pola arsitektur: Service Layer.

### Desktop (C# WPF) — ✅ 100% SELESAI
9 modul: Login, MainShellView, CageView, LivestockView, FeedView, QrGeneratorView, HealthRecordView, FcrReportView, DashboardView.
Status testing: level "developing/smoke test" — belum blackbox testing sistematis.
8 masalah debugging tercatat (mayoritas soal kecocokan field JSON API vs model C#, konsistensi namespace).

### Mobile (Kotlin) — ⏳ BELUM DIMULAI
Ditunda untuk fokus penuh ke dokumentasi dulu (keputusan sadar per 11 Sep).

---

## 3. STATUS DOKUMENTASI TEKNIS (UML/Diagram)

| Dokumen | Status | File |
|---|---|---|
| Use Case Diagram | ✅ Selesai (Desktop & Mobile terpisah) | `usecase-diagram-sisapi.docx` |
| ERD | ✅ Selesai (9 tabel + penjelasan) | `erd-sisapi.docx` |
| Class Diagram | ✅ Selesai (9 kelas Model) | `class-diagram-sisapi.docx` |
| Activity Diagram | ✅ Selesai (13 diagram: 8 Desktop + 5 Mobile rencana) | `activity-diagram-sisapi.docx` |
| Sequence Diagram (detail) | ✅ Selesai (8 diagram, alur View→Service→Controller→Model→DB) | `sequence-diagram-detail-sisapi.docx` |

**Urutan penyajian di BAB III (3.2 Pemodelan Sistem):** Use Case → ERD → Class Diagram → Activity Diagram → Sequence Diagram (pelengkap)

---

## 4. STATUS LAPORAN TA (`Laporan_Projek.docx`)

### BAB I: Pendahuluan — ✅ SELESAI
Latar Belakang, Rumusan Masalah (5 poin), Batasan Masalah (4 kategori), Tujuan (6 poin), Manfaat Projek — lengkap dan matang, sudah beberapa kali direvisi.

### BAB II: Landasan Teori & Spesifikasi Teknis — ✅ SELESAI
Gambaran Umum Sistem, Spesifikasi Teknologi (tabel + 9 subbab penjelasan termasuk FCR & FIFO), Peralatan & Lingkungan Pengembangan (software + hardware spek: Lenovo ThinkPad T460).

### BAB III: Perancangan Sistem — 🔄 SEBAGIAN BESAR SELESAI
- ✅ 3.1 Analisis Kebutuhan Sistem — Kebutuhan Fungsional (tabel 15 fitur x 3 role) + Kebutuhan Non-Fungsional (9 kategori, termasuk poin Service Layer di "Maintainability")
- 🔄 3.2 Pemodelan Sistem — Use Case, ERD, Class Diagram, Activity Diagram sudah dimasukkan dengan narasi
  - **Catatan follow-up (didiskusikan dengan pembimbing):** Use Case & Activity Diagram baru menampilkan versi Desktop, belum termasuk Mobile (karena belum dikerjakan) — perlu keputusan apakah tetap begitu atau tambah penjelasan eksplisit
  - Sequence Diagram belum dimasukkan ke BAB III — perlu dicek apakah akan ditambahkan
  - Penomoran gambar activity diagram (8 gambar dalam 1 nomor "Gambar 3.4") disarankan dipecah jadi sub-nomor
- ⏳ 3.3 Perancangan Antarmuka (Wireframe/Mockup) — belum dibuat

### BAB IV & V — ⏳ Belum dimulai (menunggu Mobile + testing lebih matang)

---

## 5. KELENGKAPAN BERKAS LAIN

| Berkas | Status |
|---|---|
| Logo Aplikasi | ✅ Selesai |
| Manual Book/Panduan Pengguna | ⏳ Belum dibuat |
| Poster Proyek | ⏳ Belum dibuat |
| Slide Presentasi Sidang | ⏳ Belum dibuat |
| Tabel Blackbox Testing Desktop | ⏳ Belum dibuat |

---

## 6. PEMETAAN KE PROGRESS TRACKER FISIK (dari Pembimbing)

| Item Checklist | Status |
|---|---|
| Pengajuan Judul & Proposal | ✅ BAB I selesai, siap ACC |
| Analisis Kebutuhan Sistem | ✅ Selesai (3.1) |
| Perancangan Basis Data (ERD, Class Diagram, Migration Schema) | ✅ ERD & Class Diagram selesai; Migration Schema tinggal lampirkan kode |
| Perancangan Antarmuka (Wireframe/UI Mockup) | ⏳ Belum dibuat |
| Setup Environment & Database | ✅ Selesai |
| Pengembangan Fitur Utama | ✅ Backend & Desktop selesai; Mobile belum |
| Pengembangan Fitur Pendukung | 🔶 Role Management ✅, File Upload ✅ (backend saja), Notification ⏳ belum |
| Refactoring & Polish | ⏳ Sengaja di-defer |
| Pengujian Sistem | 🔶 Backend teruji sistematis; Desktop baru smoke test |
| Deployment/Hosting | ⏳ Belum dikerjakan |
| BAB I: Pendahuluan | ✅ Selesai |
| BAB II: Landasan Teori | ✅ Selesai |
| BAB III: Analisis & Perancangan | 🔄 Hampir selesai, ada 3 follow-up kecil |
| BAB IV: Implementasi & Pengujian | ⏳ Belum |
| BAB V: Penutup | ⏳ Belum |
| Kelengkapan Berkas | ⏳ Belum (Manual Book, Poster, Slide) |

---

## 7. AGENDA SELANJUTNYA
1. **Konsultasi ke pembimbing** — bahas 3 follow-up BAB III (cakupan Mobile di Use Case/Activity, posisi Sequence Diagram, penomoran gambar)
2. Setelah dapat arahan pembimbing → putuskan lanjut ke:
   - Buat Wireframe/Mockup (3.3) dari screenshot Desktop yang sudah jalan, ATAU
   - Mulai coding Mobile (Sprint yang tertunda), ATAU
   - Ajukan BAB I-III untuk ACC dulu sebelum lanjut
3. BAB IV baru bisa ditulis lengkap setelah Mobile berjalan + testing sistematis Desktop dilakukan

---

## 8. PENGINGAT PENTING
- Deadline keseluruhan project tetap akhir September — dokumentasi sudah menyita cukup banyak waktu, perlu keseimbangan dengan sisa coding Mobile
- Siapkan jawaban soal kronologi "coding dulu baru dokumentasi" kalau ditanya pembimbing/penguji
- Backend & Desktop selesai jauh lebih cepat dari jadwal awal — modal buffer ini sebagian sudah terpakai untuk dokumentasi, pastikan masih cukup untuk Mobile + testing + finalisasi laporan
