# Checkpoint Project & Development Reference (v6)
## Si-Sapi — Tugas Akhir SMK

**Tanggal Update:** 4 September 2026
**Status Backend:** ✅ 100% SELESAI & TERVERIFIKASI (Sprint 1 & Sprint 2 tuntas)
**Fokus Selanjutnya:** Sprint 3 — Desktop Client (C# WPF)

---

## 1. Status Checkpoint & Roadmap Pemulihan

| Minggu | Periode | Fokus | Status |
|---|---|---|---|
| 1 | 3–9 Sep | Backend: Tuntaskan seluruh API & Transaksi | ✅ **SELESAI (4 Sep, on-time)** |
| 2 | 10–16 Sep | Desktop (C# WPF): Login, CRUD Master, QR Generator, Grafik | ⏳ Belum Mulai |
| 3 | 17–23 Sep | Mobile (Kotlin): Login, Scanner QR, Input Timbangan/Pakan/Laporan | ⏳ Belum Mulai |
| 4 | 24–30 Sep | Integrasi & Polishing (3–4 hari terakhir murni laporan TA & sidang) | ⏳ Belum Mulai |

> Backend tuntas **on-time** sesuai rencana darurat Minggu 1 — ini penting: artinya buffer keterlambatan dari HUT Sekolah/Pramuka sudah berhasil dikompensasi di minggu ini. Disiplin yang sama harus dijaga di Minggu 2 & 3.

---

## 2. Environment & Kredensial Pengujian

### Base URL API
- **Local / Desktop (WPF):** `http://127.0.0.1:8000/api`
- **Android Emulator (Kotlin):** `http://10.0.2.2:8000/api`
- **Storage / Gambar:** `http://127.0.0.1:8000/storage/`

### Default Headers
```http
Accept: application/json
Authorization: Bearer <TOKEN_SANCTUM>
```

### Akun Dummy (dari Seeder)
| Role | Email | Password |
|---|---|---|
| Manager | manager@farm.com | password |
| Vet | vet@farm.com | password |
| Worker | worker@farm.com | password |

### Data Dummy dari Seeder
- **Ras Sapi:** Simental, Limousin, Brahman
- **Kandang:** Kandang A, Kandang B
- **Sapi:** SP-001, SP-002 (lengkap dengan histori timbangan awal)
- **Pakan & Batch FIFO awal:** tersedia untuk testing logika FIFO

---

## 3. Tech Stack & Arsitektur
| Layer | Teknologi | Keterangan |
|---|---|---|
| Backend/API | Laravel + MySQL | Sanctum (token auth), pola **Service Layer** |
| Desktop | C# WPF | Konsumsi REST API (bukan akses DB langsung) |
| Mobile | Kotlin (Android Native) | Retrofit/OkHttp |

---

## 4. Daftar Endpoint API — SEMUA TUNTAS ✅

| Method | Endpoint | Modul | Status |
|---|---|---|---|
| `POST` | `/api/login` | Auth | ✅ PASSED |
| `GET` | `/api/me` | Auth | ✅ PASSED |
| `POST` | `/api/logout` | Auth | ✅ PASSED |
| `GET` | `/api/cages` | Master Data | ✅ PASSED |
| `POST` | `/api/cages` | Master Data | ✅ PASSED |
| `GET` | `/api/cages/{id}` | Master Data | ✅ PASSED |
| `GET` | `/api/breeds` | Master Data | ✅ PASSED |
| `POST` | `/api/breeds` | Master Data | ✅ PASSED |
| `GET` | `/api/feeds` | Master Data | ✅ PASSED |
| `POST` | `/api/feeds/batches` | Master Data | ✅ PASSED |
| `GET` | `/api/livestocks` | Livestock | ✅ PASSED |
| `POST` | `/api/livestocks` | Livestock | ✅ PASSED |
| `GET` | `/api/livestocks/{tagNumber}` | Livestock | ✅ PASSED |
| `POST` | `/api/cages/{id}/feed` | Feed Consumption (FIFO) | ✅ PASSED |
| `POST` | `/api/livestocks/{tagNumber}/health-report` | Health Record | ✅ PASSED |
| `GET` | `/api/livestocks/{tagNumber}/health-records` | Health Record | ✅ PASSED |
| `PATCH` | `/api/health-records/{id}` | Health Record | ✅ PASSED |
| `GET` | `/api/health-records/due` | Health Record | ✅ PASSED |
| `POST` | `/api/livestocks/{tagNumber}/weight` | Weight Log | ✅ PASSED |
| `GET` | `/api/cages/{id}/fcr` | FCR Report | ✅ PASSED |

**19 endpoint aktif, semua lulus test.**

---

## 5. Bug & Perbaikan yang Sudah Ditangani (Log Debugging)
Dicatat sebagai bahan lampiran BAB implementasi (bukti proses debugging):

1. **`FeedStockController@index`** — `BadMethodCallException`, nama relasi di controller (`feedStockBatches`) tidak cocok model (`batches`) → disesuaikan.
2. **`FeedConsumptionController@store`** — validasi `cage_id required` gagal karena `{id}` dari URL tidak otomatis masuk body → diperbaiki dengan `$request->merge(['cage_id' => $id])`.
3. **`WeightLogService@recordWeightByTag`** — kolom `worker_id` tidak terisi karena key array salah nama (`recorded_by` bukan `worker_id`) → disesuaikan.

---

## 6. Dokumentasi Pendukung
- [x] ERD lengkap (9 tabel + penjelasan kolom) — format `.docx`
- [x] Sequence Diagram — 8 fitur utama, format `.docx`
- [x] Use Case Diagram — 3 aktor (Manager, Vet, Worker)
- [ ] Flowchart per modul — opsional, belum dibuat

---

## 7. Sprint 2 — RESMI TUNTAS ✅
- [x] `LivestockController` — CRUD sapi, scan by tag, auto-generate tag, update populasi kandang
- [x] `FeedConsumptionController` — logic FIFO lintas batch, transaction-safe
- [x] `HealthRecordController` — multi-type record (vaccination/treatment/emergency_report), upload foto
- [x] `WeightLogController` — histori timbangan per sapi
- [x] `FcrService` — kalkulasi FCR per kandang

---

## 8. Agenda Selanjutnya — Sprint 3: Desktop (C# WPF)
1. Setup project WPF baru, struktur folder (Views, ViewModels/Services, Models)
2. Buat service layer HTTP client (`HttpClient`) untuk konsumsi API — termasuk penyimpanan Bearer Token setelah login
3. Login screen → konsumsi `POST /api/login`
4. CRUD UI: Sapi, Kandang, Pakan (konsumsi endpoint Master Data & Livestock yang sudah teruji)
5. QR Code generator (library QRCoder) — generate dari `tag_number`
6. Dashboard: minimal 1 grafik (populasi atau tren berat)

---

## 9. Pengingat Disiplin
- Backend selesai on-time — pertahankan ritme ini di Minggu 2
- Checkpoint mingguan wajib dicek di akhir minggu — kalau ada tanda keterlambatan, ambil keputusan cepat
- Minimal 3-4 hari terakhir (27-30 Sep) dikhususkan untuk laporan TA & latihan sidang, bukan coding
