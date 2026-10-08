# Checkpoint Perencanaan Project (v2)
## Smart Livestock Management System — Tugas Akhir SMK

---

## 1. Informasi Umum
| Item | Keterangan |
|---|---|
| Nama Project | Smart Livestock Management System |
| Studi Kasus | Peternakan Sapi Potong (khusus, bukan sapi perah) |
| Tujuan | Tugas Akhir/Skripsi SMK — membuktikan kelayakan nilai besar lewat scope berat yang **selesai utuh** |
| Model Pengerjaan | Solo |
| Durasi | 8 minggu |
| Arsitektur | Client-Server (1 backend, 2 client independen) |

---

## 2. Tech Stack Final
| Layer | Teknologi | Keterangan |
|---|---|---|
| Backend/API | Laravel + MySQL | Autentikasi via Sanctum, REST API |
| Desktop | C# WPF (konsumsi API, bukan akses DB langsung) | Zona nyaman developer — jadi tulang punggung demo |
| Mobile | Kotlin (Android Native) | Retrofit/OkHttp, lebih straightforward untuk kamera/scanner |

---

## 3. Batasan Masalah (penting untuk BAB 1 laporan)
- Sistem **hanya untuk sapi potong**, tidak mencakup sapi perah — karena sapi perah punya parameter perawatan berbeda (fokus higienitas & produksi susu, bukan kenaikan berat badan) yang memerlukan modul terpisah (`milking_logs`, tracking mastitis, KPI milk yield).
- Insight ini dicatat sebagai **saran pengembangan di BAB 5**, bukan diimplementasikan — demi menjaga scope tetap realistis untuk pengerjaan solo 8 minggu.
- FCR dihitung **per kandang**, bukan per individu sapi — didukung fakta lapangan bahwa praktik kandang koloni di Indonesia cenderung mengelompokkan sapi dengan ukuran/bobot seragam per kandang, sehingga asumsi homogenitas per kandang cukup valid.

---

## 4. Role & Hak Akses
| Role | Platform | Akses |
|---|---|---|
| Manager/Pemilik | Desktop | Full akses: dashboard, stok, analitik, manajemen user |
| Dokter Hewan (Vet) | **Desktop & Mobile** | Desktop: kerja administratif/terjadwal (jadwal vaksin, analisis riwayat medis, laporan). Mobile: kerja reaktif/lapangan (respon laporan darurat, scan cepat, input diagnosis di tempat) |
| Pekerja Kandang | Mobile | Input pakan harian, timbang sapi, lapor gejala sakit |

> Catatan teknis: karena vet dobel akses, backend API harus konsisten melayani role `vet` di endpoint terkait medis — desktop dan mobile pakai API yang sama, cuma UI beda.

---

## 5. ERD — Struktur Database (9 Tabel)

| Tabel | Kolom Kunci | Fungsi Singkat |
|---|---|---|
| `users` | id, name, email, password, role, phone | Identitas & otorisasi (manager/vet/worker) |
| `cages` | id, name, capacity, current_population, location | Pengelompokan operasional sapi |
| `livestocks` | id, tag_number (unique), cage_id (FK), breed_id (FK), gender, birth_date, entry_date, initial_weight, status | Profil individu sapi, basis pencarian via QR |
| `livestock_breeds` *(baru)* | id, breed_name, ideal_fcr_range | Jenis/ras sapi + acuan FCR ideal |
| `weight_logs` | id, livestock_id (FK), weight, date, worker_id (FK) | Histori penimbangan, basis grafik tren |
| `health_records` | id, livestock_id (FK), type (vaccination/treatment/emergency_report), diagnosis, treatment, photo_path, next_due_date, handled_by (FK), date | Rekam medis multiguna |
| `feed_stocks` | id, feed_name, unit | Master jenis pakan |
| `feed_stock_batches` *(baru)* | id, feed_id (FK), quantity_in, quantity_remaining, entry_date | Mesin logika FIFO |
| `feed_consumptions` | id, cage_id (FK), feed_id (FK), amount_used, date, worker_id (FK) | Log konsumsi pakan harian |

**Relasi utama:**
- `cages` (1) → `livestocks` (N)
- `livestock_breeds` (1) → `livestocks` (N)
- `livestocks` (1) → `weight_logs` (N), `health_records` (N)
- `feed_stocks` (1) → `feed_stock_batches` (N), `feed_consumptions` (N)
- `users` (1) → `weight_logs`, `health_records`, `feed_consumptions` (N) — sebagai pencatat/penanggung jawab

**Cara hitung FCR:**
```
FCR = Total pakan dikonsumsi (kg) per kandang / Kenaikan berat badan (kg) per kandang
```

**Logika FIFO:** saat `feed_consumptions` baru dibuat, sistem mengurangi `quantity_remaining` dari batch di `feed_stock_batches` dengan `entry_date` paling lama terlebih dahulu.

---

## 6. Alur Pemakaian Aplikasi (Skenario)

1. **Sapi baru masuk** → Manager input di desktop → sistem generate `tag_number` → cetak QR → tempel Ear Tag fisik
2. **Rutinitas harian pekerja** → mobile → catat pakan per kandang → FIFO potong stok otomatis di background
3. **Penimbangan berkala** → scan QR di mobile → input berat baru → tersimpan sebagai baris baru → muncul di grafik tren desktop
4. **Sapi sakit** → scan QR + foto + form gejala di mobile (`emergency_report`) → vet terima & follow-up (diagnosis, treatment) via mobile/desktop
5. **Jadwal vaksinasi** → vet input `next_due_date` di desktop → cron job cek harian → reminder muncul saat jatuh tempo
6. **Evaluasi manager** → dashboard desktop: grafik populasi, mortality rate, tren berat, laporan FCR per kandang, buku besar stok pakan

---

## 7. List Fitur per Platform (dengan Prioritas)

### 🔴 WAJIB (Core — demi kelulusan & demo layak)
**Backend:** Auth + role-based access · CRUD API livestocks/cages/feed_stocks · Endpoint scan by tag_number · Endpoint POST weight & feed consumption
**Desktop:** Login · CRUD Sapi/Kandang/Pegawai/Pakan · Dashboard grafik dasar · QR Code generator
**Mobile:** Login · Scanner QR → profil sapi · Input timbangan · Input pakan harian

### 🟡 PENTING TAPI BISA DISEDERHANAKAN DULU
- FCR: mulai dari rumus sederhana per kandang
- FIFO: mulai dari pengurangan stok biasa, sempurnakan pakai batch kalau waktu ada
- Dashboard: mulai 1-2 grafik dasar, tambah kalau sempat

### 🟢 BOLEH DIKORBANKAN DULUAN (kalau minggu 6-7 mepet)
- Cron job reminder vaksinasi otomatis (bisa manual dulu)
- Foto di pelaporan darurat (bisa tanpa foto dulu)
- Riwayat medis detail per individu (versi list sederhana dulu)
- Manajemen user lengkap di desktop (hardcode 3 role dulu)

---

## 8. Roadmap 8 Minggu

| Minggu | Fokus | Output |
|---|---|---|
| 1 | Backend: Fondasi | Setup Laravel, migration (9 tabel), seeder, auth API |
| 2 | Backend: Core API | CRUD lengkap, FCR, FIFO, upload foto, dokumentasi Postman |
| 3-4 | Desktop (WPF) | Login, CRUD UI, QR generator, dashboard grafik, rekam medis |
| 5-6 | Mobile (Kotlin) | Login, scanner QR, input timbangan, input pakan |
| 7 | Mobile lanjutan + polish | Laporan darurat, polish UI, cron reminder (jika sempat) |
| 8 | Integrasi & Deployment | End-to-end test, bug fixing, deploy backend, build .exe & .apk, bahan sidang |

---

## 9. Status Checkpoint
- [ ] Minggu 1 selesai
- [ ] Minggu 2 selesai
- [ ] Minggu 3-4 selesai
- [ ] Minggu 5-6 selesai
- [ ] Minggu 7 selesai
- [ ] Minggu 8 selesai

---

## 10. Keputusan Terbuka / Belum Difinalkan
*(kosong — semua keputusan besar sudah difinalkan per checkpoint ini)*
